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

package com.antigenomics.mageri.core.output;

import com.antigenomics.mageri.core.genomic.GenomicInfo;
import com.antigenomics.mageri.core.mapping.PAlignedConsensus;
import com.antigenomics.mageri.core.mapping.alignment.AlignmentResult;
import com.antigenomics.mageri.core.mutations.Indel;
import com.antigenomics.mageri.core.mutations.MutationArray;
import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.antigenomics.mageri.core.mapping.SAlignedConsensus;
import com.antigenomics.mageri.core.mutations.Insertion;
import com.antigenomics.mageri.core.mutations.Mutation;

public final class SamUtil {
    public static final int DUMMY_INT = 0;
    public static final byte DUMMY_BYTE = (byte) 0;
    public static final String DUMMY_STRING = "*";

    private SamUtil() {


    }

    public static final int
            BLANK_FLAG = 0x0,
            MULTIPLE_SEGMENTS_FLAG = 0x1,
            ALL_ALIGNED_FLAG = 0x2,
            UNMAPPED_FLAG = 0x4,
            NEXT_UNMAPPED_FLAG = 0x8,
            RC_FLAG = 0x10,
            NEXT_RC_FLAG = 0x20,
            FIRST_SEGMENT_FLAG = 0x40,
            LAST_SEGMENT_FLAG = 0x80,
            SECONDARY_ALIGNMENT_FLAG = 0x100,
            BAD_MAPPING_FLAG = 0x200,
            DUPLICATE_FLAG = 0x400,
            CHIMERIC_ALIGNMENT_FLAG = 0x800;

    public static String SOFT_CLIP_CIGAR = "S", HARD_CLIP_CIGAR = "H",
            INSERTION_CIGAR = "I", DELETION_CIGAR = "D", MATCH_CIGAR = "M";

    public static String createCigarString(AlignmentResult alignmentResult,
                                           MutationArray mutations) {
        if (alignmentResult == null)
            return DUMMY_STRING;

        int queryLen = alignmentResult.getQuery().size();
        LocalAlignment localAlignment = alignmentResult.getAlignment();
        StringBuilder cigar = new StringBuilder();

        int s5 = localAlignment.getSequence2Range().getFrom(),
                s3 = queryLen - localAlignment.getSequence2Range().getTo();

        // 5' soft-clipping of consensus bases
        if (s5 > 0)
            cigar.append(s5).append(SOFT_CLIP_CIGAR);

        // Mutations are in absolute reference coordinates
        int prevPos = localAlignment.getSequence1Range().getFrom();

        for (Mutation mutation : mutations.getMutations()) {
            if (mutation instanceof Indel) {
                int delta = mutation.getStart() - prevPos;
                if (delta > 0) {
                    cigar.append(delta).append(MATCH_CIGAR);
                }
                cigar.append(mutation.getLength()).append(mutation instanceof Insertion ?
                        INSERTION_CIGAR : DELETION_CIGAR);
                prevPos = mutation.getEnd(); // same as start for I, start+length for D
            }
        }

        // Remaining matches
        int delta = localAlignment.getSequence1Range().getTo() - prevPos;
        if (delta > 0) {
            cigar.append(delta).append(MATCH_CIGAR);
        }

        // 3' soft-clipping
        if (s3 > 0)
            cigar.append(s3).append(SOFT_CLIP_CIGAR);

        return cigar.toString();
    }

    public static SamSegmentRecord create(NucleotideSequence umi,
                                          NucleotideSQPair consensusSQPair,
                                          AlignmentResult alignmentResult,
                                          MutationArray mutations) {
        String name = umi.toString();
        if (alignmentResult == null) {
            String sequence = consensusSQPair.getSequence().toString(),
                    quality = consensusSQPair.getQuality().toString();
            return new SamSegmentRecord(name, sequence, quality);
        } else {
            GenomicInfo genomicInfo = alignmentResult.getReference().getGenomicInfo();

            if (genomicInfo.getContig().skipInSamAndVcf()) {
                return null;
            }

            String chrom = genomicInfo.getChrom();
            int pos = genomicInfo.getStart() + // move BED (0b) to SAM (1b)
                    alignmentResult.getAlignment().getSequence1Range().getFrom() + 1;

            String cigar = createCigarString(alignmentResult, mutations);

            if (alignmentResult.isReverseComplement()) {
                consensusSQPair = consensusSQPair.getRC();
            }

            String sequence = consensusSQPair.getSequence().toString(),
                    quality = consensusSQPair.getQuality().toString();

            return new SamSegmentRecord(name,
                    (alignmentResult.isReverseComplement() ? RC_FLAG : BLANK_FLAG) |
                            (alignmentResult.isGood() ? BLANK_FLAG : BAD_MAPPING_FLAG),
                    chrom, pos,
                    alignmentResult.getScore(), cigar,
                    sequence, quality
            );
        }
    }

    public static SamRecord create(SAlignedConsensus alignedConsensus) {
        SamSegmentRecord samSegmentRecord = create(alignedConsensus.getUmi(),
                alignedConsensus.getConsensusSQPair(),
                alignedConsensus.getAlignmentResult(),
                alignedConsensus.getMutations());

        if (samSegmentRecord == null) {
            return null;
        }

        return new SamRecord(samSegmentRecord);
    }

    public static SamRecord create(PAlignedConsensus alignedConsensus) {
        SamSegmentRecord samSegmentRecord1 = create(alignedConsensus.getUmi(),
                alignedConsensus.getConsensusSQPair1(),
                alignedConsensus.getAlignmentResult1(),
                alignedConsensus.getMutations1()),
                samSegmentRecord2 = create(alignedConsensus.getUmi(),
                        alignedConsensus.getConsensusSQPair2(),
                        alignedConsensus.getAlignmentResult2(),
                        alignedConsensus.getMutations2());

        if (samSegmentRecord1 == null || samSegmentRecord2 == null) {
            return null;
        }

        return new SamRecord(samSegmentRecord1, samSegmentRecord2);
    }
}
