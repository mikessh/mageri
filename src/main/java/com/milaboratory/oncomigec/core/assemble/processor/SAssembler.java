package com.milaboratory.oncomigec.core.assemble.processor;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequenceBuilder;
import com.milaboratory.core.sequence.quality.SequenceQualityPhred;
import com.milaboratory.oncomigec.core.assemble.entity.SConsensus;
import com.milaboratory.oncomigec.core.assemble.misc.AssemblerParameters;
import com.milaboratory.oncomigec.core.io.entity.SMig;
import com.milaboratory.oncomigec.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public final class SAssembler extends Assembler<SConsensus, SMig> {
    private final AssemblerParameters parameters;
    private final AtomicLong readsDroppedShort = new AtomicLong(), readsDroppedErrors = new AtomicLong();

    public SAssembler() {
        this.parameters = AssemblerParameters.DEFAULT;
    }

    public SAssembler(AssemblerParameters parameters) {
        this.parameters = parameters;
    }

    private NucleotideSequence getCoreSeq(NucleotideSequence seq, int offset) {
        int mid = seq.size() / 2;
        return seq.getRange(mid - parameters.getAnchorRegion() - offset,
                mid + parameters.getAnchorRegion() + 1 - offset);
    }

    @Override
    public SConsensus assemble(SMig sMig) {
        // Update counters
        migsTotal.incrementAndGet();
        readsTotal.addAndGet(sMig.size());

        // Main algorithm -- quite complex
        List<NucleotideSQPair> assembledReads = new ArrayList<>(),
                droppedReads = new ArrayList<>();

        // Step 1: collect core regions with different offsets to determine most frequent one
        // Count frequency via hash
        Map<NucleotideSequence, int[]> coreSeqDataMap = new HashMap<>();
        NucleotideSequence coreSeq = null;
        int[] coreSeqData;
        for (NucleotideSQPair read : sMig.getReads()) {
            if (read.getSequence().size() > parameters.getMinReadSize())
                for (int offset = -parameters.getOffsetRange(); offset <= parameters.getOffsetRange(); offset++) {
                    coreSeq = getCoreSeq(read.getSequence(), offset);
                    coreSeqData = coreSeqDataMap.get(coreSeq);
                    if (coreSeqData == null)
                        coreSeqDataMap.put(coreSeq, coreSeqData = new int[2]);
                    coreSeqData[0]++;
                    coreSeqData[1] += Math.abs(offset);
                }
        }

        // determine best core
        coreSeqData = new int[2];
        for (Map.Entry<NucleotideSequence, int[]> coreSeqEntry : coreSeqDataMap.entrySet()) {
            if (coreSeqEntry.getValue()[0] > coreSeqData[0] ||
                    (coreSeqEntry.getValue()[0] == coreSeqData[0] && coreSeqEntry.getValue()[1] < coreSeqData[1])) {
                coreSeq = coreSeqEntry.getKey();
                coreSeqData = coreSeqEntry.getValue();
            }
        }
        NucleotideSequence bestCoreSeq = coreSeq;

        // Step 2: For all reads find optimal position against the core & append to pwm; discard if too much mms
        int X = 0, Y = 0;

        List<Integer> xArr = new ArrayList<>(sMig.getReads().size());
        for (NucleotideSQPair read : sMig.getReads()) {
            if (read.getSequence().size() > parameters.getMinReadSize()) {
                // 2.1 Determine best offset vs core
                int bestOffset = 0, bestOffsetMMs = parameters.getAnchorRegion();
                for (int offset = -parameters.getOffsetRange(); offset <= parameters.getOffsetRange(); offset++) {
                    int offsetMMs = 0;
                    coreSeq = getCoreSeq(read.getSequence(), offset);
                    if (coreSeq.equals(bestCoreSeq)) {
                        bestOffset = offset;
                        bestOffsetMMs = 0;
                        break;  // keep match
                    } else {
                        for (int i = 0; i < coreSeq.size(); i++)
                            if (coreSeq.codeAt(i) != bestCoreSeq.codeAt(i))
                                offsetMMs++;

                        if (offsetMMs < bestOffsetMMs) {
                            bestOffsetMMs = offsetMMs;
                            bestOffset = offset;
                        }
                    }
                }

                // 2.2 Keep if more than 'maxMMs' per 'anchorRegion'
                if (bestOffsetMMs <= parameters.getMaxMMs()) {
                    int l = read.size(), mid = l / 2;
                    int x = mid - bestOffset, y = l - x;
                    
                    if (parameters.greedyExtend()) {
                        X = Math.max(x, X);
                        Y = Math.max(y, X);
                    } else {
                        X += x;
                        Y += y;
                    }

                    xArr.add(x);

                    assembledReads.add(read);
                } else {
                    droppedReads.add(read); // drop due to too much errors
                    readsDroppedErrors.incrementAndGet();
                }
            } else {
                droppedReads.add(read); // drop too short
                readsDroppedShort.incrementAndGet();
            }
        }

        if (droppedReads.size() / (double) sMig.size() >= parameters.getMaxDroppedReadsRatio())
            return null;

        if (!parameters.greedyExtend()) {
            X /= assembledReads.size();
            Y /= assembledReads.size();
        }

        // Step 3: process consensus
        // Step 3.1: Select region to construct PWM, append reads to PWM
        // Longest possible pwm is built
        int pwmLen = X + Y;
        double[][] pwm = new double[pwmLen][4];

        List<NucleotideSQPair> postAssembledReads = null;

        if (parameters.doCqsRescue())
            postAssembledReads = new ArrayList<>();

        for (int i = 0; i < assembledReads.size(); i++) {
            NucleotideSQPair sqPair = assembledReads.get(i);

            // Calculate offsets to PWM
            int l = sqPair.getSequence().size(), x = xArr.get(i), y = l - x,
                    xDelta = X - x, yDelta = Y - y;
            int from = xDelta < 0 ? -xDelta : 0, to = l + (yDelta < 0 ? yDelta : 0);

            // Append N's to bounds of sequence or trim it to fit PWM
            StringBuilder sbSeq = new StringBuilder(), sbQual = new StringBuilder();
            for (int k = 0; k < xDelta; k++) {
                sbSeq.append("N");
                sbQual.append("#");
            }

            sbSeq.append(sqPair.getSequence().getRange(from, to).toString());
            sbQual.append(sqPair.getQuality().getRange(from, to).toString());

            for (int k = 0; k < yDelta; k++) {
                sbSeq.append("N");
                sbQual.append("#");
            }

            sqPair = new NucleotideSQPair(sbSeq.toString(), sbQual.toString());

            // Reads with Ns appended to fit PWM will be used later in case of CQS rescue
            if (parameters.doCqsRescue())
                postAssembledReads.add(sqPair);

            // Two modes of pwm scoring
            for (int k = 0; k < pwmLen; k++) {
                byte qual = sqPair.getQuality().value(k);
                double increment = parameters.qualityWeightedMode() ? qual : 1.0;

                if (qual >= Util.PH33_BAD_QUAL)
                    pwm[k][sqPair.getSequence().codeAt(k)] += increment;
                else
                    for (int m = 0; m < 4; m++)
                        pwm[k][m] += increment / 4.0;
            }
        }

        // Step 3.1a: CQS rescue (optional)
        List<NucleotideSQPair> finalAssembledReads = new ArrayList<>();

        if (parameters.doCqsRescue()) {
            // Re-calculate consensus
            NucleotideSequenceBuilder consensusSequenceBuilder = new NucleotideSequenceBuilder(pwmLen);
            for (int k = 0; k < pwmLen; k++) {
                byte mostFreqLetter = 0;
                double maxLetterFreq = 0;
                for (byte l = 0; l < 4; l++) {
                    if (maxLetterFreq < pwm[k][l]) {
                        maxLetterFreq = pwm[k][l];
                        mostFreqLetter = l;
                    }
                }
                consensusSequenceBuilder.setCode(k, mostFreqLetter);
            }
            NucleotideSequence consensusSequence = consensusSequenceBuilder.create();

            // Re-check assembled reads
            for (int i = 0; i < assembledReads.size(); i++) {
                NucleotideSQPair readSQPair = postAssembledReads.get(i);
                NucleotideSequence readSequence = readSQPair.getSequence();

                // Move from core region boundary to left and right
                int leftBound = pwmLen / 2 - parameters.getAnchorRegion() - parameters.getOffsetRange(),
                        rightBound = pwmLen / 2 + parameters.getAnchorRegion() + parameters.getOffsetRange(),
                        range = Math.min(leftBound, pwmLen - rightBound) - 3; // dont consider last 3 nts

                // Check for X consequent mismatches, i.e. indel check
                int leftConsequentMMs = 0, rightConsequentMMs = 0;
                for (int j = 0; j < range; j++) {
                    int left = leftBound - j, right = rightBound + j;
                    if (consensusSequence.codeAt(left) != readSequence.codeAt(left))
                        if (++leftConsequentMMs > parameters.getMaxConsequentMMs())
                            break;
                    if (consensusSequence.codeAt(right) != readSequence.codeAt(right))
                        if (++rightConsequentMMs > parameters.getMaxConsequentMMs())
                            break;
                }

                if (Math.max(leftConsequentMMs, rightConsequentMMs) > parameters.getMaxConsequentMMs()) {
                    // Add to dropped
                    droppedReads.add(assembledReads.get(i));
                    // Remove from PWM
                    for (int k = 0; k < pwmLen; k++) {
                        byte qual = readSQPair.getQuality().value(k);
                        double increment = parameters.qualityWeightedMode() ? qual : 1.0;

                        if (qual >= Util.PH33_BAD_QUAL)
                            pwm[k][readSQPair.getSequence().codeAt(k)] -= increment;
                        else
                            for (int m = 0; m < 4; m++)
                                pwm[k][m] -= increment / 4.0;
                    }
                } else {
                    // Keep read
                    finalAssembledReads.add(assembledReads.get(i));
                }
            }
        } else {
            // Just keep all assembled reads
            finalAssembledReads = assembledReads;
        }

        // Step 3.2: Calculate new quality
        NucleotideSequenceBuilder consensusSequence = new NucleotideSequenceBuilder(pwmLen);
        byte[] consensusQuality = new byte[pwmLen];
        int goodSeqStart = 0;
        for (int k = 0; k < pwmLen; k++) {
            byte mostFreqLetter = 0;
            double maxLetterFreq = 0, letterFreqSum = 0;
            for (byte l = 0; l < 4; l++) {
                if (maxLetterFreq < pwm[k][l]) {
                    maxLetterFreq = pwm[k][l];
                    mostFreqLetter = l;
                }
                letterFreqSum += pwm[k][l];
            }
            consensusSequence.setCode(k, mostFreqLetter);
            byte cqs = Util.percentageToCqs(maxLetterFreq / letterFreqSum);
            consensusQuality[k] = cqs;

            if (cqs <= Util.PH33_BAD_QUAL && parameters.performQualityTrimming()) {
                if (goodSeqStart == k) {
                    goodSeqStart++;
                }
            }
        }

        NucleotideSQPair consensusSQPair = new NucleotideSQPair(consensusSequence.create(),
                new SequenceQualityPhred(consensusQuality));

        // quality trimming
        if (parameters.performQualityTrimming()) {
            int goodSeqEnd = pwmLen;
            for (; goodSeqEnd >= goodSeqStart; goodSeqEnd--) {
                if (consensusQuality[goodSeqEnd - 1] > Util.PH33_BAD_QUAL)
                    break;
            }

            consensusSQPair = consensusSQPair.getRange(goodSeqStart, goodSeqEnd);
        }

        // That's it!

        migsAssembled.incrementAndGet();
        readsAssembled.addAndGet(assembledReads.size());

        SConsensus consensus = new SConsensus(sMig.getUmi(), consensusSQPair,
                finalAssembledReads, droppedReads);

        if (storeConsensuses)
            consensusList.add(consensus);

        return consensus;
    }


    @Override
    public long getReadsDroppedShortR1() {
        return readsDroppedShort.get();
    }

    @Override
    public long getReadsDroppedErrorR1() {
        return readsDroppedErrors.get();
    }

    @Override
    public long getReadsDroppedShortR2() {
        return 0;
    }

    @Override
    public long getReadsDroppedErrorR2() {
        return 0;
    }

    @Override
    public boolean isPairedEnd() {
        return false;
    }
}
