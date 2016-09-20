/*
 * Copyright 2014-2016 Mikhail Shugay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.antigenomics.mageri.core.assemble;

import com.antigenomics.mageri.core.input.PreprocessorParameters;
import com.antigenomics.mageri.core.input.SMig;
import com.antigenomics.mageri.core.input.index.Read;
import com.antigenomics.mageri.core.mapping.alignment.AlignmentScoring;
import com.antigenomics.mageri.misc.QualityDefaults;
import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.alignment.AffineGapAlignmentScoring;
import com.milaboratory.core.sequence.alignment.LocalAligner;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequenceBuilder;
import com.milaboratory.core.sequence.quality.SequenceQualityPhred;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class SAssembler extends Assembler<SConsensus, SMig> {
    protected final double minorFreqThreshold;
    protected final AssemblerParameters parameters;
    private final AtomicLong readsDroppedShortCounter = new AtomicLong(),
            readsDroppedErrorsCounter = new AtomicLong(),
            readsRescuedCounter = new AtomicLong();

    public SAssembler() {
        this(PreprocessorParameters.DEFAULT, AssemblerParameters.DEFAULT);
    }

    public SAssembler(PreprocessorParameters preprocessorParameters,
                      AssemblerParameters parameters) {
        this.minorFreqThreshold = Math.pow(10.0, -(double) preprocessorParameters.getGoodQualityThreshold() / 10.0);
        this.parameters = parameters;
    }

    private NucleotideSequence getCoreSeq(NucleotideSequence seq, int offset) {
        int mid = seq.size() / 2;
        return seq.getRange(mid - parameters.getAnchorRegion() - offset,
                mid + parameters.getAnchorRegion() + 1 - offset);
    }

    @Override
    public SConsensus assemble(SMig mig) {
        // Update counters
        readsTotal.addAndGet(mig.size());
        migsTotal.incrementAndGet();

        /////////////////////////////////////
        // Main algorithm -- quite complex //
        /////////////////////////////////////

        // Step 1: collect core regions with different offsets to determine most frequent one
        // 1.1 Count core k-mer frequency via hash
        Map<NucleotideSequence, int[]> coreSeqDataMap = new HashMap<>();
        NucleotideSequence coreSeq = null;
        int[] coreSeqData;
        for (Read read : mig.getReads()) {
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

        // 1.2 Determine best core sequence
        coreSeqData = new int[2];
        for (Map.Entry<NucleotideSequence, int[]> coreSeqEntry : coreSeqDataMap.entrySet()) {
            if (coreSeqEntry.getValue()[0] > coreSeqData[0] ||
                    (coreSeqEntry.getValue()[0] == coreSeqData[0] && coreSeqEntry.getValue()[1] < coreSeqData[1])) {
                coreSeq = coreSeqEntry.getKey();
                coreSeqData = coreSeqEntry.getValue();
            }
        }
        NucleotideSequence bestCoreSeq = coreSeq;

        // Step 2: Find optimal position of reads against the core & append to pwm
        //         discard in case there are too much mismatches
        List<ReadWithOffset> assembledReads = new ArrayList<>();
        List<Read> droppedReads = new ArrayList<>();
        for (Read read : mig.getReads()) {
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
                    assembledReads.add(new ReadWithOffset(read, bestOffset));
                } else {
                    // drop due to too much errors
                    readsDroppedErrorsCounter.incrementAndGet();

                    if (parameters.doCqsRescue()) {
                        droppedReads.add(read);
                    }
                }
            } else {
                // drop too short
                readsDroppedShortCounter.incrementAndGet();
            }
        }

        // Check if this MIG should be skipped as a high number of reads does not fit core k-mer 
        int n = assembledReads.size();
        double droppedReadsRatio = 1.0 - n / (double) mig.size();

        if (droppedReadsRatio >= parameters.getMaxDroppedReadsRatio()) {
            return null;
        }

        // Step 3: process consensus
        // Step 3.1: Select region to construct PWM, append reads to PWM

        double[][] pwm = new double[4][0];
        int[][] exactPwm = new int[4][0];

        PwmBoundaries pwmBoundaries = fillPwmAndRecomputeOffsets(assembledReads, pwm, exactPwm);

        // Step 3.2a: try to do CQS rescue
        if (parameters.doCqsRescue()) {
            NucleotideSequence rawConsensus = constructConsensus(pwm, n, false).consensusSQPair.getSequence();

            // Drop reads that have more than two consequent mismatch / > 50% mismatches
            int filteredReads = filterReadsForCqsRescue(assembledReads, droppedReads, rawConsensus, pwmBoundaries, pwm, exactPwm);
            n -= filteredReads;

            int rescuedReads = runCqsRescue(rawConsensus, droppedReads, pwm, exactPwm);
            readsRescuedCounter.addAndGet(rescuedReads);
            n += rescuedReads;

            if (1.0 - (double) n / mig.size() >= parameters.getMaxDroppedReadsRatioAfterRescue()) {
                return null;
            }
        }

        // Step 3.2: Calculate consensus sequence, CQS quality score and minors
        ConsensusAndTrimmingInfo consensusAndTrimmingInfo = constructConsensus(pwm, n,
                parameters.performQualityTrimming());

        if (consensusAndTrimmingInfo.trimmedBasesRatio > parameters.getMaxTrimmedConsensusBasesRatio()) {
            return null;
        }

        // Search for minors
        int minorCountThreshold = (int) (minorFreqThreshold * n);
        Set<Integer> minors = new HashSet<>();
        for (int k = consensusAndTrimmingInfo.goodSeqStart; k < consensusAndTrimmingInfo.goodSeqEnd; k++) {
            byte from = consensusAndTrimmingInfo.consensusSQPair
                    .getSequence()
                    .codeAt(k - consensusAndTrimmingInfo.goodSeqStart);
            for (byte l = 0; l < 4; l++) {
                if (l != from && exactPwm[l][k] > minorCountThreshold) {
                    minors.add(Mutations.createSubstitution(k - consensusAndTrimmingInfo.goodSeqStart, from, l));
                }
            }
        }

        // That's it!

        migsAssembled.incrementAndGet();
        readsAssembled.addAndGet(n);

        SConsensus consensus = new SConsensus(mig.getSample(), mig.getUmi(), consensusAndTrimmingInfo.consensusSQPair,
                minors, n, mig.size());

        if (storeConsensuses)
            consensusList.add(consensus);

        return consensus;
    }

    private PwmBoundaries fillPwmAndRecomputeOffsets(List<ReadWithOffset> assembledReads,
                                                     double[][] pwm, int[][] exactPwm) {
        // Compute PWM size
        int X = 0, Y = 0, n = assembledReads.size();

        if (parameters.greedyExtend()) {
            for (ReadWithOffset readWithOffset : assembledReads) {
                X = Math.max(readWithOffset.x, X);
                Y = Math.max(readWithOffset.y, X);
            }
        } else {
            for (ReadWithOffset readWithOffset : assembledReads) {
                X += readWithOffset.x;
                Y += readWithOffset.y;
            }
            X /= n;
            Y /= n;
        }

        // Re-initialize PWM
        PwmBoundaries pwmBoundaries = new PwmBoundaries(X, Y);

        for (int i = 0; i < 4; i++) {
            pwm[i] = new double[pwmBoundaries.pwmLen];
            exactPwm[i] = new int[pwmBoundaries.pwmLen];
        }

        for (ReadWithOffset readWithOffset : assembledReads) {
            // Calculate offsets to PWM
            int xDelta = pwmBoundaries.getPwmOffset(readWithOffset),
                    yDelta = Y - readWithOffset.y;
            readWithOffset.from = xDelta < 0 ? -xDelta : 0;
            readWithOffset.to = readWithOffset.l + (yDelta < 0 ? yDelta : 0);

            // Update pwm
            for (int k = readWithOffset.from; k < readWithOffset.to; k++) {
                int pwmPos = xDelta + k;
                byte code = readWithOffset.read.getSequence().codeAt(k);
                pwm[code][pwmPos]++;

                if (readWithOffset.read.goodQuality(k)) {
                    exactPwm[code][pwmPos]++;
                }
            }
        }

        return new PwmBoundaries(X, Y);
    }

    private ConsensusAndTrimmingInfo constructConsensus(double[][] pwm, int n,
                                                        boolean performQualityTrimming) {
        int pwmLen = pwm[0].length;
        NucleotideSequenceBuilder consensusSequence = new NucleotideSequenceBuilder(pwmLen);
        byte[] consensusQuality = new byte[pwmLen];
        int goodSeqStart = 0;

        for (int k = 0; k < pwmLen; k++) {
            byte mostFreqLetter = 0;
            double maxLetterFreq = 0;
            for (byte l = 0; l < 4; l++) {
                double freq = pwm[l][k];
                if (maxLetterFreq < freq) {
                    maxLetterFreq = freq;
                    mostFreqLetter = l;
                }
            }
            consensusSequence.setCode(k, mostFreqLetter);

            byte cqs = (byte) Math.max(QualityDefaults.PH33_MIN_QUAL,
                    Math.min(QualityDefaults.PH33_MAX_QUAL,
                            40 * ((maxLetterFreq / (double) n - 0.25) / 0.75)
                    )
            );
            consensusQuality[k] = cqs;

            // Quality trimming - 5' end
            if (cqs <= QualityDefaults.PH33_BAD_QUAL && performQualityTrimming) {
                if (goodSeqStart == k) {
                    goodSeqStart++;
                }
            }
        }

        NucleotideSQPair consensusSQPair = new NucleotideSQPair(consensusSequence.create(),
                new SequenceQualityPhred(consensusQuality));

        // Quality trimming - 3' end
        int goodSeqEnd = pwmLen;
        if (performQualityTrimming) {
            for (; goodSeqEnd >= goodSeqStart; goodSeqEnd--) {
                if (consensusQuality[goodSeqEnd - 1] > QualityDefaults.PH33_BAD_QUAL)
                    break;
            }

            consensusSQPair = consensusSQPair.getRange(goodSeqStart, goodSeqEnd);
        } else {
            goodSeqStart = 0;
        }

        double trimmedBasesRatio = (double) (goodSeqStart + pwmLen - goodSeqEnd) / pwmLen;

        return new ConsensusAndTrimmingInfo(consensusSQPair, goodSeqStart, goodSeqEnd, trimmedBasesRatio);
    }

    private int filterReadsForCqsRescue(List<ReadWithOffset> assembledReads,
                                        List<Read> droppedReads, NucleotideSequence rawConsensus,
                                        PwmBoundaries pwmBoundaries,
                                        double[][] pwm, int[][] exactPwm) {
        int filteredReads = 0;

        for (ReadWithOffset readWithOffset : assembledReads) {
            int consequentMms = 0, totalMms = 0;
            for (int k = readWithOffset.from; k < readWithOffset.to; k++) {
                int posInConsensus = pwmBoundaries.getPwmOffset(readWithOffset) + k;

                if (readWithOffset.read.getSequence().codeAt(k) != rawConsensus.codeAt(posInConsensus)) {
                    consequentMms++;
                    totalMms++;
                } else {
                    consequentMms = 0;
                }

                if (consequentMms > 2)
                    break;
            }

            if (consequentMms > 2 || (double) totalMms / (readWithOffset.to - readWithOffset.from) > 0.5) {
                droppedReads.add(readWithOffset.read);

                // Remove read from PWM
                for (int k = readWithOffset.from; k < readWithOffset.to; k++) {
                    int posInConsensus = pwmBoundaries.getPwmOffset(readWithOffset) + k;

                    byte code = readWithOffset.read.getSequence().codeAt(k);
                    pwm[code][posInConsensus]--;

                    if (readWithOffset.read.goodQuality(k)) {
                        exactPwm[code][posInConsensus]--;
                    }
                }

                filteredReads++;
            }
        }

        return filteredReads;
    }

    private int runCqsRescue(NucleotideSequence rawConsensus, List<Read> droppedReads, double[][] pwm, int[][] exactPwm) {
        int rescuedReads = 0;

        AffineGapAlignmentScoring scoring = new AlignmentScoring().asInternalScoring();

        for (Read droppedRead : droppedReads) {
            LocalAlignment alignment = LocalAligner.align(scoring, rawConsensus, droppedRead.getSequence());

            int[] mutations = alignment.getMutations(); // consensus -> read mutations

            double alignedBasesRatio = (double) (alignment.getSequence2Range().length() - mutations.length) /
                    droppedRead.length();

            if (alignedBasesRatio >= parameters.getMinMatchedBasesInRealignedReadRatio()) {
                int alignmentStartInConsensus = alignment.getSequence1Range().getFrom(),
                        alignmentStartInRead = alignment.getSequence2Range().getFrom();

                // Add result of a perfect read alignment
                for (int i = alignment.getSequence1Range().getFrom(); i < alignment.getSequence1Range().getTo(); i++) {
                    pwm[rawConsensus.codeAt(i)][i]++;
                }

                // Adjust PWM scores according to alignment
                for (int i = 0; i < mutations.length; ++i) {
                    int mutation = mutations[i];
                    if (Mutations.isSubstitution(mutation)) {
                        // Add substitution to PWM
                        int nt = Mutations.getTo(mutation),
                                pos = Mutations.getPosition(mutation);

                        int posInConsensus = alignmentStartInConsensus + pos,
                                posInRead = alignmentStartInRead +
                                        // position after applying mutations, i.e. relative position in read
                                        Mutations.convertPosition(mutations, pos);

                        pwm[nt][posInConsensus]++;

                        if (droppedRead.goodQuality(posInRead)) {
                            exactPwm[nt][posInConsensus]++;
                        }

                        // Adjust back
                        pwm[rawConsensus.codeAt(i)][posInConsensus]--;
                    }
                }
                rescuedReads++;
            }
        }

        return rescuedReads;
    }

    private static class ReadWithOffset {
        final Read read;
        final int bestOffset, x, y, l;
        int from, to;

        ReadWithOffset(Read read, int bestOffset) {
            this.read = read;
            this.bestOffset = bestOffset;

            this.l = read.length();
            int mid = l / 2;
            this.x = mid - bestOffset;
            this.y = l - x;
        }
    }

    private static class PwmBoundaries {
        final int X, Y, pwmLen;

        PwmBoundaries(int X, int Y) {
            this.X = X;
            this.Y = Y;
            this.pwmLen = X + Y;
        }

        int getPwmOffset(ReadWithOffset readWithOffset) {
            return X - readWithOffset.x;
        }
    }

    private static class ConsensusAndTrimmingInfo {
        final NucleotideSQPair consensusSQPair;
        final int goodSeqStart, goodSeqEnd;
        final double trimmedBasesRatio;

        ConsensusAndTrimmingInfo(NucleotideSQPair consensusSQPair, int goodSeqStart,
                                 int goodSeqEnd, double trimmedBasesRatio) {
            this.consensusSQPair = consensusSQPair;
            this.goodSeqStart = goodSeqStart;
            this.goodSeqEnd = goodSeqEnd;
            this.trimmedBasesRatio = trimmedBasesRatio;
        }
    }

    @Override
    public long getReadsRescuedR1() {
        return readsRescuedCounter.get();
    }

    @Override
    public long getReadsRescuedR2() {
        return 0;
    }

    @Override
    public long getReadsDroppedShortR1() {
        return readsDroppedShortCounter.get();
    }

    @Override
    public long getReadsDroppedErrorR1() {
        return readsDroppedErrorsCounter.get();
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
