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
package com.milaboratory.migec2.preproc.misc;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequencing.read.PSequencingRead;
import com.milaboratory.core.sequencing.read.PSequencingReadImpl;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.core.sequencing.read.SSequencingReadImpl;

import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadOverlapper {
    private final int maxOffset, mmOverlapSz, k;
    private final boolean orientedOverlap;
    private AtomicLong overlappedCount = new AtomicLong(), totalCount = new AtomicLong();

    public ReadOverlapper(boolean orientedOverlap, int maxOffset, int mmOverlapSz, int k) {
        this.orientedOverlap = orientedOverlap;
        this.maxOffset = maxOffset;
        this.mmOverlapSz = mmOverlapSz;
        this.k = k;
    }

    public ReadOverlapper(boolean orientedOverlap) {
        this(orientedOverlap, 5, 10, 5);
    }

    public ReadOverlapper() {
        this(true, 5, 10, 5); // for default checkout output
    }

    public AtomicLong getOverlappedCount() {
        return overlappedCount;
    }

    public AtomicLong getTotalCount() {
        return totalCount;
    }

    public OverlapResult overlap(PSequencingRead readPair) {
        SSequencingRead read1 = readPair.getSingleRead(0), read2 = readPair.getSingleRead(1);
        //if (performIlluminaRC)
        //    read2 = new SSequencingReadImpl(read2.getDescription(),
        //            read2.getData().getRC(), read2.id());

        OverlapResult result = overlap(read1, read2);

        if (!orientedOverlap && !result.isOverlapped()) {
            OverlapResult result2 = overlap(read2, read1);
            if (result2.isOverlapped())
                return result2;
        }
        return result;
    }

    private OverlapResult overlap(SSequencingRead read1, SSequencingRead read2) {
        String seq1 = read1.getData().getSequence().toString(),
                seq2 = read2.getData().getSequence().toString(),
                qual1 = read1.getData().getQuality().toString(),
                qual2 = read2.getData().getQuality().toString();

        totalCount.incrementAndGet();

        for (int i = 0; i < maxOffset; i++) {
            if (i + k > seq2.length())
                break; // too short

            String kmer = seq2.substring(i, i + k);
            Pattern pattern = Pattern.compile(kmer);
            Matcher matcher = pattern.matcher(seq1);

            // Find last match
            int position;
            while (matcher.find()) {
                position = matcher.start();
                if (position >= 0) {
                    // Start fuzzy align - allowing two consequent mismatches here
                    int nmm = 0;
                    for (int j = 0; j < mmOverlapSz; j++) {
                        int posInR1 = position + k + j, posInR2 = i + k + j;
                        if (posInR1 + 1 > seq1.length())
                            break;  // went to end of r1, all fine
                        if (seq1.charAt(posInR1) != seq2.charAt(posInR2)) {
                            if (++nmm > 1)
                                break;  // two consequent mismatches
                        } else {
                            nmm = 0; // zero counter
                        }
                    }
                    if (nmm < 2) {
                        // Take best qual nts
                        StringBuilder nrSeq1 = new StringBuilder(seq1.substring(0, position)),
                                nrQual1 = new StringBuilder(qual1.substring(0, position)),
                                nrSeq2 = new StringBuilder(),
                                nrQual2 = new StringBuilder();

                        // First half
                        int pos2 = i - 1, overlapHalfSz = (seq1.length() - position) / 2;
                        for (int j = position; j < position + overlapHalfSz; j++) {
                            pos2++;
                            if (pos2 + 1 == seq2.length())
                                // should not happen
                                return new OverlapResult(-2, false, new PSequencingReadImpl(read1, read2));

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
                            if (pos2 + 1 == seq2.length())
                                // should not happen
                                return new OverlapResult(-2, false, new PSequencingReadImpl(read1, read2));

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
                        return new OverlapResult(overlapHalfSz, true,
                                new PSequencingReadImpl(
                                        new SSequencingReadImpl(read1.getDescription(),
                                                new NucleotideSQPair(nrSeq1.toString(), nrQual1.toString()),
                                                read1.id()),
                                        new SSequencingReadImpl(read2.getDescription(),
                                                new NucleotideSQPair(nrSeq2.toString(), nrQual2.toString()),
                                                read2.id())));
                    }
                }
            }
        }

        return new OverlapResult(-1, false, new PSequencingReadImpl(read1, read2));
    }

    public class OverlapResult {
        private final int overlapHalfSz;
        private final boolean overlapped;
        private final PSequencingRead readPair;

        public OverlapResult(int overlapHalfSz, boolean overlapped, PSequencingRead readPair) {
            this.overlapHalfSz = overlapHalfSz;
            this.overlapped = overlapped;
            this.readPair = readPair;
        }

        public int getOverlapHalfSz() {
            return overlapHalfSz;
        }

        public boolean isOverlapped() {
            return overlapped;
        }

        public PSequencingRead getReadPair() {
            return readPair;
        }
    }
}
