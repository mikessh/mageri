/*
 * Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)
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
package com.milaboratory.oncomigec.core.consalign.processor;

import com.milaboratory.core.sequence.NucleotideSQPair;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsensusOverlapper implements Serializable {
    private final int overlapFuzzySize, overlapSeedSize, maxConsMms;
    private final boolean allowPartialOverlap;
    private final double maxOverlapMismatchRatio;
    private AtomicLong overlappedCount = new AtomicLong(), totalCount = new AtomicLong();

    public ConsensusOverlapper(boolean allowPartialOverlap,
                               int overlapFuzzySize, int overlapSeedSize, int maxConsMms,
                               double maxOverlapMismatchRatio) {
        this.allowPartialOverlap = allowPartialOverlap;
        this.overlapFuzzySize = overlapFuzzySize;
        this.overlapSeedSize = overlapSeedSize;
        this.maxConsMms = maxConsMms;
        this.maxOverlapMismatchRatio = maxOverlapMismatchRatio;
    }

    public ConsensusOverlapper() {
        this(true, 15, 5, 2, 0.1); // for default checkout output
    }

    public long getOverlappedCount() {
        return overlappedCount.get();
    }

    public long getTotalCount() {
        return totalCount.get();
    }

    public double getOverlapEfficiency() {
        return overlappedCount.get() / (double) totalCount.get();
    }

    public OverlapResult overlap(NucleotideSQPair read1, NucleotideSQPair read2) {
        String seq1 = read1.getSequence().toString(),
                seq2 = read2.getSequence().toString(),
                qual1 = read1.getQuality().toString(),
                qual2 = read2.getQuality().toString();

        totalCount.incrementAndGet();

        for (int i = 0; i < seq2.length() - overlapSeedSize - (allowPartialOverlap ? 0 : overlapFuzzySize) + 1; i++) {
            if (i + overlapSeedSize > seq2.length())
                break; // too short

            String kmer = seq2.substring(i, i + overlapSeedSize);
            Pattern pattern = Pattern.compile(kmer);
            Matcher matcher = pattern.matcher(seq1);

            // Find last match
            int position;
            while (matcher.find()) {
                position = matcher.start();
                if (position >= 0) {
                    // Start fuzzy align - allowing two consequent mismatches here
                    boolean alignedAll = true;
                    int nConsMms = 0, nMms = 0, actualFuzzyOverlapSize = overlapFuzzySize;

                    for (int j = 0; j < overlapFuzzySize; j++) {
                        int posInR1 = position + overlapSeedSize + j, posInR2 = i + overlapSeedSize + j;
                        if (posInR1 + 1 > seq1.length() || posInR2 + 1 > seq2.length()) {
                            actualFuzzyOverlapSize = j + 1;
                            alignedAll = false;
                            break;     // went to end of r1
                        }
                        if (seq1.charAt(posInR1) != seq2.charAt(posInR2)) {
                            nMms++;
                            if (++nConsMms >= maxConsMms)
                                break;  // several consequent mismatches
                        } else {
                            nConsMms = 0; // zero counter
                        }
                    }

                    if (nConsMms < maxConsMms &&
                            (allowPartialOverlap || alignedAll) &&
                            (nMms / (double) actualFuzzyOverlapSize) <= maxOverlapMismatchRatio) {
                        // Take best qual nts
                        StringBuilder nrSeq1 = new StringBuilder(seq1.substring(0, position)),
                                nrQual1 = new StringBuilder(qual1.substring(0, position)),
                                nrSeq2 = new StringBuilder(),
                                nrQual2 = new StringBuilder();

                        // First half
                        int pos2 = i - 1, overlapHalfSz = (seq1.length() - position) / 2;

                        for (int j = position; j < position + overlapHalfSz; j++) {
                            pos2++;

                            if (pos2 >= seq2.length()) {
                                // should not happen
                                return new OverlapResult(overlapHalfSz, OverlapType.Bad, i, read1, read2);
                            }

                            if (qual1.charAt(j) > qual2.charAt(pos2)) {
                                nrSeq1.append(seq1.charAt(j));
                                nrQual1.append(qual1.charAt(j));
                            } else {
                                nrSeq1.append(seq2.charAt(pos2));
                                nrQual1.append(qual2.charAt(pos2));
                            }
                        }

                        // Second half
                        for (int j = position + overlapHalfSz; j < seq1.length(); j++) {
                            pos2++;

                            if (pos2 + 1 > seq2.length()) {
                                StringBuilder masterSeq = nrSeq1.append(nrSeq2),
                                        masterQual = nrQual1.append(nrQual2);
                                overlapHalfSz = masterSeq.length() / 2;

                                // readthrough
                                return new OverlapResult(overlapHalfSz,
                                        OverlapType.ReadThrough, i,
                                        new NucleotideSQPair(masterSeq.substring(0, overlapHalfSz),
                                                masterQual.substring(0, overlapHalfSz)),
                                        new NucleotideSQPair(masterSeq.substring(overlapHalfSz),
                                                masterQual.substring(overlapHalfSz))
                                );
                            }

                            if (qual1.charAt(j) > qual2.charAt(pos2)) {
                                nrSeq2.append(seq1.charAt(j));
                                nrQual2.append(qual1.charAt(j));
                            } else {
                                nrSeq2.append(seq2.charAt(pos2));
                                nrQual2.append(qual2.charAt(pos2));
                            }
                        }

                        for (int j = pos2 + 1; j < seq2.length(); j++) {
                            // fill the remainder
                            nrSeq2.append(seq2.charAt(j));
                            nrQual2.append(qual2.charAt(j));
                        }

                        overlappedCount.incrementAndGet();

                        return new OverlapResult(overlapHalfSz, OverlapType.Normal, i,
                                new NucleotideSQPair(nrSeq1.toString(), nrQual1.toString()),
                                new NucleotideSQPair(nrSeq2.toString(), nrQual2.toString())
                        );
                    }
                }
            }
        }

        return new OverlapResult(-1, OverlapType.None, -1, read1, read2);
    }

    public class OverlapResult {
        private final int overlapHalfSz;
        private final NucleotideSQPair consensus1, consensus2;
        private final OverlapType overlapType;
        private final int offset;

        public OverlapResult(int overlapHalfSz, OverlapType overlapType, int offset,
                             NucleotideSQPair consensus1, NucleotideSQPair consensus2) {
            this.overlapHalfSz = overlapHalfSz;
            this.overlapType = overlapType;
            this.consensus1 = consensus1;
            this.consensus2 = consensus2;
            this.offset = offset;
        }

        public int getOverlapHalfSz() {
            return overlapHalfSz;
        }

        public OverlapType getOverlapType() {
            return overlapType;
        }

        public boolean isOverlapped() {
            return overlapType != OverlapType.None;
        }

        public NucleotideSQPair getConsensus1() {
            return consensus1;
        }

        public NucleotideSQPair getConsensus2() {
            return consensus2;
        }

        public int getOffset() {
            return offset;
        }
    }

    public static enum OverlapType {
        Normal, ReadThrough, None, Bad
    }
}
