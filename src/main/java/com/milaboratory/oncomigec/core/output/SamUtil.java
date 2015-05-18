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

package com.milaboratory.oncomigec.core.output;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.genomic.GenomicInfo;
import com.milaboratory.oncomigec.core.mapping.PAlignedConsensus;
import com.milaboratory.oncomigec.core.mapping.SAlignedConsensus;
import com.milaboratory.oncomigec.core.mapping.alignment.AlignmentResult;
import com.milaboratory.oncomigec.core.mutations.Indel;
import com.milaboratory.oncomigec.core.mutations.Insertion;
import com.milaboratory.oncomigec.core.mutations.Mutation;
import com.milaboratory.oncomigec.core.mutations.MutationArray;

public final class SamUtil {
    public static final int DUMMY_INT = 0;
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
                    (int) alignmentResult.getScore(), cigar,
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
