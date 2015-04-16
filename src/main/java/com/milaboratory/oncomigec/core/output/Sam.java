/*
 * Copyright 2013-2015 Mikhail Shugay (mikhail.shugay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last modified on 13.4.2015 by mikesh
 */

package com.milaboratory.oncomigec.core.output;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.align.sequence.AlignmentResult;
import com.milaboratory.oncomigec.core.align.AlignedConsensus;
import com.milaboratory.oncomigec.core.genomic.GenomicInfo;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.core.mutations.Indel;
import com.milaboratory.oncomigec.core.mutations.Insertion;
import com.milaboratory.oncomigec.core.mutations.Mutation;
import com.milaboratory.oncomigec.core.mutations.MutationArray;

public class Sam {
    private static final String DUMMY_STR = "*";
    private static final int DUMMY_INT = 0;

    private final ReferenceLibrary referenceLibrary;

    public Sam(ReferenceLibrary referenceLibrary) {
        this.referenceLibrary = referenceLibrary;
    }

    public String toSamRecord(AlignedConsensus alignedConsensus) {
        return alignedConsensus.isPairedEnd() ?
                getSamRecordPaired(alignedConsensus) :
                getSamRecordSingle(alignedConsensus);

        /*
        String samLines = "";
        //alignment.getSequence2Range().length()
        if (alignedConsensus.isPairedEnd()) {
            if (alignedConsensus.firstMateAligned()) {
                samLines += toSamRecord(alignedConsensus.getUmi(),
                        alignedConsensus.getConsensusSQPair1(),
                        alignedConsensus.getMajorMutations1(),
                        alignedConsensus.getAlignmentResult1(),
                        getFlagPaired())
            }
        }*/
    }

    private static String getSamRecordPaired(AlignedConsensus alignedConsensus) {

    }

    private static String getSamRecordSingle(AlignedConsensus alignedConsensus) {
        int flag = 0;
        if (!alignedConsensus.aligned()) {
            flag |= 0x4;
        } else if (alignedConsensus.getAlignmentResult1().isReverseComplement()) {
            flag |= 0x10;
        }

    }

    private static int getFlagPaired(AlignedConsensus alignedConsensus) {
        int flag = 0x1;
        if (alignedConsensus.aligned()) {
            flag |= 0x2;
        }
        return flag;
    }

    private static int getFlagPaired(AlignedConsensus alignedConsensus, boolean firstMate) {
        int flag = getFlagPaired(alignedConsensus);
        if (firstMate) {
            flag |= 0x40 | getFlagSingle(alignedConsensus);
            if (!alignedConsensus.secondMateAligned()) {
                flag |= 0x8;
            } else if (alignedConsensus.getAlignmentResult2().isReverseComplement()) {
                // NOTE
                // the reverse complement flag only tells that the SEQ and QUAL fields are in reverse complement
                // CIGAR string, etc, stays the same
                flag |= 0x20;
            }
        } else {
            flag |= 0x80;
            if (!alignedConsensus.secondMateAligned()) {
                flag |= 0x4;
            } else {
                if (alignedConsensus.getAlignmentResult2().isReverseComplement()) {
                    flag |= 0x10;
                }
                if (alignedConsensus.chimeric()) {
                    flag |= 0x800;
                }
            }
        }
        return flag;
    }

    private static int getFlagSingle(AlignedConsensus alignedConsensus) {
        int flag = 0;
        if (!alignedConsensus.firstMateAligned()) {
            flag |= 0x4;
        } else if (alignedConsensus.getAlignmentResult1().isReverseComplement()) {
            flag |= 0x10;
        }
        return flag;
    }

    private String toSamRecord(NucleotideSequence umi,
                               int flag,
                               String rName,
                               int pos,
                               int flag, String rNext, String pNext, int tlen) {
        GenomicInfo genomicInfo = alignmentResult.getReference().getGenomicInfo();
        LocalAlignment alignment = alignmentResult.getAlignment();

        StringBuilder samRecord = new StringBuilder();

        String cigar = getCigarString(consensusSQPair.size(), alignment, mutations);

        samRecord.append(umi.toString()).append("\t").                  // QNAME
                append(flag).append("\t").                              // FLAG
                append(genomicInfo.getChrom()).append("\t").            // RNAME
                append(alignment.getSequence1Range().getFrom() +        // POS
                genomicInfo.getFrom() + 1).append("\t").                // move BED (0b) to SAM (1b)
                append(alignmentResult.getMapqScore()).append("\t").    // MAPQ
                append(cigar).append("\t").                             // CIGAR
                append(rNext).append("\t").                             // RNEXT
                append(pNext).append("\t").                             // PNEXT
                append(tlen).append("\t").                              // TLEN
                append(consensusSQPair.getSequence()).append("\t").     // SEQ
                append(consensusSQPair.getQuality());                   // QUAL

        return samRecord.toString();
    }

    private String toSamRecord(NucleotideSequence umi,
                               NucleotideSQPair consensusSQPair,
                               MutationArray mutations,
                               AlignmentResult alignmentResult,
                               int flag, String rNext, String pNext, int tlen) {
        GenomicInfo genomicInfo = alignmentResult.getReference().getGenomicInfo();
        LocalAlignment alignment = alignmentResult.getAlignment();

        StringBuilder samRecord = new StringBuilder();

        String cigar = getCigarString(consensusSQPair.size(), alignment, mutations);

        samRecord.append(umi.toString()).append("\t").                  // QNAME
                append(flag).append("\t").                              // FLAG
                append(genomicInfo.getChrom()).append("\t").            // RNAME
                append(alignment.getSequence1Range().getFrom() +        // POS
                genomicInfo.getFrom() + 1).append("\t").                // move BED (0b) to SAM (1b)
                append(alignmentResult.getScore()).append("\t").    // MAPQ
                append(cigar).append("\t").                             // CIGAR
                append(rNext).append("\t").                             // RNEXT
                append(pNext).append("\t").                             // PNEXT
                append(tlen).append("\t").                              // TLEN
                append(consensusSQPair.getSequence()).append("\t").     // SEQ
                append(consensusSQPair.getQuality());                   // QUAL

        return samRecord.toString();
    }

    private static int getPos(AlignmentResult alignmentResult) {
        GenomicInfo genomicInfo = alignmentResult.getReference().getGenomicInfo();
        LocalAlignment alignment = alignmentResult.getAlignment();
        return alignment.getSequence1Range().getFrom() +
                genomicInfo.getFrom() + 1;
    }

    private static String getCigarString(int queryLen,
                                         LocalAlignment localAlignment,
                                         MutationArray mutations) {
        StringBuilder cigar = new StringBuilder();

        int s5 = localAlignment.getSequence2Range().getFrom(),
                s3 = queryLen - localAlignment.getSequence2Range().getTo();

        // 5' soft-clipping of consensus bases
        if (s5 > 0)
            cigar.append(s5).append("S");

        // Mutations are in absolute reference coordinates
        int prevPos = localAlignment.getSequence1Range().getFrom();

        for (Mutation mutation : mutations.getMutations()) {
            if (mutation instanceof Indel) {
                int delta = mutation.getStart() - prevPos;
                if (delta > 0) {
                    cigar.append(delta).append("M");
                }
                cigar.append(mutation.getLength()).append(mutation instanceof Insertion ? "I" : "D");
                prevPos = mutation.getEnd(); // same as start for I, start+length for D
            }
        }

        // Remaining matches
        int delta = localAlignment.getSequence1Range().getTo() - prevPos;
        if (delta > 0) {
            cigar.append(delta).append("M");
        }

        // 3' soft-clipping
        if (s3 > 0)
            cigar.append(s3).append("S");

        return cigar.toString();
    }

    private static class Genomic {
        private final String referenceName;
        private final int referencePosition;

        public static final Genomic DUMMY = new Genomic(DUMMY_STR, DUMMY_INT);

        private Genomic(String referenceName, int referencePosition) {
            this.referenceName = referenceName;
            this.referencePosition = referencePosition;
        }

        public Genomic(AlignmentResult alignmentResult) {
            GenomicInfo genomicInfo = alignmentResult.getReference().getGenomicInfo();
            LocalAlignment alignment = alignmentResult.getAlignment();
            this.referenceName = genomicInfo.getChrom();
            this.referencePosition = alignment.getSequence1Range().getFrom() +
                    genomicInfo.getFrom() + 1;
        }

        public String getReferenceName() {
            return referenceName;
        }

        public int getReferencePosition() {
            return referencePosition;
        }
    }

    private static class Flag {
        private final AlignedConsensus alignedConsensus;
        private final int baseFlag;

        public Flag(AlignedConsensus alignedConsensus) {
            this.alignedConsensus = alignedConsensus;
            int flag = alignedConsensus.isPairedEnd() ? 0x1 : 0;
            if (alignedConsensus.allAligned()) {
                flag |= 0x2;
            }
            this.baseFlag = flag;
        }

        private int getFlag(AlignmentResult alignmentResult) {
            int flag = 0x40;
            if (!alignmentResult.isGood()) {
                flag |= 0x4;
            } else if (alignmentResult.isReverseComplement()) {
                flag |= 0x10;
            }
            return flag;
        }

        public int getFlag() {
            return getFlag(true);
        }

        public int getFlag(boolean firstMate) {
            int flag = baseFlag;
            flag |= firstMate ?
                    getFlag(alignedConsensus.getAlignmentResult1()) :
                    getFlag(alignedConsensus.getAlignmentResult2()) << 1;
            if (!firstMate && alignedConsensus.chimeric()) {
                flag |= 0x800;
            }
            return flag;
        }
    }
}
