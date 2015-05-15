/*
 * Copyright (c) 2014-2015, Bolotin Dmitry, Chudakov Dmitry, Shugay Mikhail
 * (here and after addressed as Inventors)
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact the Inventors using one of the following
 * email addresses: chudakovdm@mail.ru, chudakovdm@gmail.com
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */

package com.milaboratory.oncomigec.core.assemble;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequenceBuilder;
import com.milaboratory.core.sequence.quality.SequenceQualityPhred;
import com.milaboratory.oncomigec.core.input.SMig;
import com.milaboratory.oncomigec.core.input.index.Read;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.milaboratory.oncomigec.misc.QualityDefaults.*;

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
    public SConsensus assemble(SMig mig) {
        // TODO: IMPORTANT re-alignment
        // Update counters
        readsTotal.addAndGet(mig.size());

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
        int X = 0, Y = 0;
        List<Integer> xArr = new ArrayList<>(mig.getReads().size());
        List<Read> assembledReads = new ArrayList<>();
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
                    int l = read.length(), mid = l / 2;
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
                    // drop due to too much errors
                    readsDroppedErrors.incrementAndGet();
                }
            } else {
                // drop too short
                readsDroppedShort.incrementAndGet();
            }
        }

        // Check if this MIG should be skipped as a high number of reads does not fit core k-mer 
        int n = assembledReads.size();

        if (1.0 - n / (double) mig.size() >= parameters.getMaxDroppedReadsRatio()) {
            return null;
        } else {
            migsTotal.incrementAndGet();
        }


        // Step 3: process consensus
        // Step 3.1: Select region to construct PWM, append reads to PWM
        if (!parameters.greedyExtend()) {
            X /= n;
            Y /= n;
        }

        int pwmLen = X + Y;
        double[][] pwm = new double[pwmLen][4];
        int[][] exactPwm = new int[pwmLen][4];

        for (int i = 0; i < n; i++) {
            Read read = assembledReads.get(i);

            // Calculate offsets to PWM
            int l = read.getSequence().size(), x = xArr.get(i), y = l - x,
                    xDelta = X - x, yDelta = Y - y;
            int from = xDelta < 0 ? -xDelta : 0, to = l + (yDelta < 0 ? yDelta : 0);

            // Update pwm
            for (int k = from; k < to; k++) {
                int pwmPos = xDelta + k;
                byte code = read.getSequence().codeAt(k);
                pwm[pwmPos][code]++;

                if (read.goodQuality(k)) {
                    exactPwm[pwmPos][code]++;
                }
            }
        }

        // Step 3.2: Calculate consensus sequence, CQS quality score and minors
        NucleotideSequenceBuilder consensusSequence = new NucleotideSequenceBuilder(pwmLen);
        byte[] consensusQuality = new byte[pwmLen];
        int goodSeqStart = 0;

        for (int k = 0; k < pwmLen; k++) {
            byte mostFreqLetter = 0;
            double maxLetterFreq = 0;
            for (byte l = 0; l < 4; l++) {
                double freq = pwm[k][l];
                if (maxLetterFreq < freq) {
                    maxLetterFreq = freq;
                    mostFreqLetter = l;
                }
            }
            consensusSequence.setCode(k, mostFreqLetter);
            /*
            Math.max(PH33_MIN_QUAL, Math.min(PH33_MAX_QUAL, -10 * Math.log10(nonMajorFreq)))
             */
            byte cqs = (byte) Math.max(PH33_MIN_QUAL,
                    Math.min(PH33_MAX_QUAL,
                            40 * ((maxLetterFreq / (double) n - 0.25) / 0.75)
                    )
            );
            consensusQuality[k] = cqs;

            // Quality trimming - 5' end
            if (cqs <= PH33_BAD_QUAL && parameters.performQualityTrimming()) {
                if (goodSeqStart == k) {
                    goodSeqStart++;
                }
            }
        }

        NucleotideSQPair consensusSQPair = new NucleotideSQPair(consensusSequence.create(),
                new SequenceQualityPhred(consensusQuality));

        // Quality trimming - 3' end
        int goodSeqEnd = pwmLen;
        if (parameters.performQualityTrimming()) {
            for (; goodSeqEnd >= goodSeqStart; goodSeqEnd--) {
                if (consensusQuality[goodSeqEnd - 1] > PH33_BAD_QUAL)
                    break;
            }

            consensusSQPair = consensusSQPair.getRange(goodSeqStart, goodSeqEnd);
        }

        // Search for minors
        Set<Integer> minors = new HashSet<>();
        for (int k = goodSeqStart; k < goodSeqEnd; k++) {
            byte from = consensusSQPair.getSequence().codeAt(k - goodSeqStart);
            for (byte l = 0; l < 4; l++) {
                if (l != from && exactPwm[k][l] > 0) {
                    minors.add(Mutations.createSubstitution(k - goodSeqStart, from, l));
                }
            }
        }

        // That's it!

        migsAssembled.incrementAndGet();
        readsAssembled.addAndGet(assembledReads.size());

        SConsensus consensus = new SConsensus(mig.getSample(), mig.getUmi(), consensusSQPair,
                minors, n, mig.size());

        if (storeConsensuses)
            consensusList.add(consensus);

        return consensus;
        

        /*
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
        }*/
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
