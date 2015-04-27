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
package com.milaboratory.oncomigec.misc;

import com.milaboratory.core.sequence.NucleotideSQPair;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Overlapper implements Serializable {
    private final int overlapFuzzySize, overlapSeedSize, maxConsMms;
    private final boolean allowPartialOverlap;
    private final double maxOverlapMismatchRatio;
    private AtomicLong overlappedCount = new AtomicLong(),
            readThroughCount = new AtomicLong(),
            totalCount = new AtomicLong();

    public Overlapper(boolean allowPartialOverlap,
                      int overlapFuzzySize, int overlapSeedSize, int maxConsMms,
                      double maxOverlapMismatchRatio) {
        this.allowPartialOverlap = allowPartialOverlap;
        this.overlapFuzzySize = overlapFuzzySize;
        this.overlapSeedSize = overlapSeedSize;
        this.maxConsMms = maxConsMms;
        this.maxOverlapMismatchRatio = maxOverlapMismatchRatio;
    }

    public Overlapper() {
        this(true, 15, 5, 2, 0.1); // for default checkout output
    }

    public long getOverlappedCount() {
        return overlappedCount.get();
    }

    public long getTotalCount() {
        return totalCount.get();
    }

    public long getReadThroughCount() {
        return readThroughCount.get();
    }

    public double getOverlapEfficiency() {
        return overlappedCount.get() / (double) totalCount.get();
    }

    public double getReadthroughRate() {
        return readThroughCount.get() / (double) overlappedCount.get();
    }

    public OverlapResult overlap(NucleotideSQPair sqPair1, NucleotideSQPair sqPair2) {
        String seq1 = sqPair1.getSequence().toString(),
                seq2 = sqPair2.getSequence().toString(),
                qual1 = sqPair1.getQuality().toString(),
                qual2 = sqPair2.getQuality().toString();

        totalCount.incrementAndGet();
        int l1 = seq1.length(), l2 = seq2.length();

        int maxOffset = Math.min(seq2.length() - overlapSeedSize - (allowPartialOverlap ? 0 : overlapFuzzySize) + 1,
                seq2.length() / 2);

        for (int y = 0; y < maxOffset; y++) {
            if (y + overlapSeedSize > seq2.length())
                break; // No more seeds

            String kmer = seq2.substring(y, y + overlapSeedSize);
            Pattern pattern = Pattern.compile(kmer);
            Matcher matcher = pattern.matcher(seq1);

            // Find best match
            while (matcher.find()) {
                int x = matcher.start();
                // If seed matches
                if (x >= 0) {
                    // Start fuzzy align - allowing two consequent mismatches here
                    boolean alignedAll = true;
                    int nConsMms = 0, nMms = 0, actualFuzzyOverlapSize = overlapFuzzySize;

                    for (int i = 0; i < overlapFuzzySize; i++) {
                        int posInR1 = x + overlapSeedSize + i, posInR2 = y + overlapSeedSize + i;
                        if (posInR1 + 1 > seq1.length() || posInR2 + 1 > seq2.length()) {
                            actualFuzzyOverlapSize = i + 1;
                            alignedAll = false;
                            break;        // went to end of r1
                        }
                        if (seq1.charAt(posInR1) != seq2.charAt(posInR2)) {
                            nMms++;
                            if (++nConsMms >= maxConsMms)
                                break;    // several consequent mismatches
                        } else {
                            nConsMms = 0; // zero counter
                        }
                    }

                    boolean readThrough = l2 - y < l1 - x;

                    if (nConsMms < maxConsMms &&
                            ((allowPartialOverlap && !readThrough) || alignedAll) &&
                            (nMms / (double) actualFuzzyOverlapSize) <= maxOverlapMismatchRatio) {
                        // Determine whether normal overlap or readthrough
                        StringBuilder overlappedSeq = new StringBuilder(),
                                overlappedQual = new StringBuilder();

                        overlappedCount.incrementAndGet();

                        // Normal overlap
                        if (!readThrough) {
                            // - skipped
                            // = as is
                            // ~ best taken
                            // * seed (just for reference)
                            //          
                            //                             p3
                            //      0 ------y*~~~~~~~~~~~===== l2  seq2, j=0..l2
                            //  0 ==========x*~~~~~~~~~~~ l1       seq1, i=0..l1
                            //       p1          p2

                            // p1
                            int i = 0;
                            for (; i < x; i++) {
                                overlappedSeq.append(seq1.charAt(i));
                                overlappedQual.append(qual1.charAt(i));
                            }

                            // p2
                            int j = 0;
                            for (; i < l1; i++) {
                                j = y + i - x;
                                if (qual1.charAt(i) > qual2.charAt(j)) {
                                    overlappedSeq.append(seq1.charAt(i));
                                    overlappedQual.append(qual1.charAt(i));
                                } else {
                                    overlappedSeq.append(seq2.charAt(j));
                                    overlappedQual.append(qual2.charAt(j));
                                }
                            }

                            // p3
                            j++;
                            for (; j < l2; j++) {
                                overlappedSeq.append(seq2.charAt(j));
                                overlappedQual.append(qual2.charAt(j));
                            }
                        }
                        // Read-through 
                        else {
                            readThrough = true;
                            readThroughCount.incrementAndGet();

                            // - skipped
                            // = as is
                            // ~ best taken
                            //          
                            //        p1        p2
                            //   0 =======y*~~~~~~~~~~~ l2        seq2, j=0..l2
                            //      0 ----x*~~~~~~~~~~~====== l1  seq1, i=0..l1
                            //                           p3

                            // p1
                            int j = 0;
                            for (; j < y; j++) {
                                overlappedSeq.append(seq2.charAt(j));
                                overlappedQual.append(qual2.charAt(j));
                            }

                            // p2
                            int i = 0;
                            for (; j < l2; j++) {
                                i = x + j - y;
                                if (qual1.charAt(i) > qual2.charAt(j)) {
                                    overlappedSeq.append(seq1.charAt(i));
                                    overlappedQual.append(qual1.charAt(i));
                                } else {
                                    overlappedSeq.append(seq2.charAt(j));
                                    overlappedQual.append(qual2.charAt(j));
                                }
                            }

                            // p3
                            i++;
                            for (; i < l1; i++) {
                                overlappedSeq.append(seq1.charAt(i));
                                overlappedQual.append(qual1.charAt(i));
                            }
                        }

                        return new OverlapResult(
                                readThrough,
                                new NucleotideSQPair(
                                        overlappedSeq.toString(),
                                        overlappedQual.toString()),
                                x, y);
                    }
                }
            }
        }

        return new OverlapResult(false, null, -1, -1);
    }

    public class OverlapResult {
        private final NucleotideSQPair sqPair;
        private final boolean readThrough;
        private final int x, y;

        public OverlapResult(boolean readThrough,
                             NucleotideSQPair sqPair,
                             int x, int y) {
            this.readThrough = readThrough;
            this.sqPair = sqPair;
            this.x = x;
            this.y = y;
        }

        public boolean readThrough() {
            return readThrough;
        }

        public boolean overlapped() {
            return sqPair != null;
        }

        public NucleotideSQPair getSQPair() {
            return sqPair;
        }

        public int getOffset1() {
            return readThrough ? y - x : 0;
        }

        public int getOffset2() {
            return readThrough ? 0 : x - y;
        }
    }
}
