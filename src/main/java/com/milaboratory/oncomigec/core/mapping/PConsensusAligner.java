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

package com.milaboratory.oncomigec.core.mapping;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.oncomigec.core.mapping.alignment.Aligner;
import com.milaboratory.oncomigec.core.mapping.alignment.AlignmentResult;
import com.milaboratory.oncomigec.core.assemble.PConsensus;
import com.milaboratory.oncomigec.core.assemble.SConsensus;
import com.milaboratory.oncomigec.core.mutations.MutationArray;
import com.milaboratory.oncomigec.misc.Overlapper;

import java.util.HashSet;
import java.util.Set;

public final class PConsensusAligner extends ConsensusAligner<PConsensus> {
    private final Overlapper overlapper = new Overlapper();

    public PConsensusAligner(Aligner aligner, ConsensusAlignerParameters parameters) {
        super(aligner, parameters);
    }

    public PConsensusAligner(Aligner aligner) {
        super(aligner, ConsensusAlignerParameters.DEFAULT);
    }

    @Override
    public AlignedConsensus align(PConsensus pConsensus) {
        SConsensus consensus1 = pConsensus.getConsensus1(),
                consensus2 = pConsensus.getConsensus2();

        NucleotideSQPair consensusSQPair1 = consensus1.getConsensusSQPair(),
                consensusSQPair2 = consensus2.getConsensusSQPair();

        // Make sure consensuses do not overlap to escape from mutation merging hell
        Overlapper.OverlapResult overlapResult = overlapper.overlap(
                consensusSQPair1, consensusSQPair2);

        if (overlapResult.overlapped()) {
            NucleotideSQPair consensusSQPair = overlapResult.getSQPair();
            AlignmentResult alignmentResult = aligner.align(consensusSQPair.getSequence());
            MutationArray majorMutations = null;

            if (alignmentResult != null) {
                // Prepare minors accordingly
                Set<Integer> minors = new HashSet<>();

                for (int minor : consensus1.getMinors()) {
                    minors.add(Mutations.move(minor, overlapResult.getOffset1()));
                }

                for (int minor : consensus2.getMinors()) {
                    minors.add(Mutations.move(minor, overlapResult.getOffset2()));
                }

                majorMutations = extractMutations(alignmentResult, consensusSQPair, minors);
            }

            return new SAlignedConsensus(consensus1.getSample(), consensus1.getUmi(), consensusSQPair,
                    alignmentResult, majorMutations);
        } else {
            AlignmentResult alignmentResult1 = aligner.align(consensusSQPair1.getSequence()),
                    alignmentResult2 = aligner.align(consensusSQPair2.getSequence());

            MutationArray mutations1 = alignmentResult1 == null ?
                    null : extractMutations(alignmentResult1, consensus1),
                    mutations2 = alignmentResult2 == null ?
                            null : extractMutations(alignmentResult2, consensus2);

            return new PAlignedConsensus(consensus1.getSample(), consensus1.getUmi(),
                    consensusSQPair1, consensusSQPair2,
                    alignmentResult1, alignmentResult2,
                    mutations1, mutations2);
        }
    }

    @Override
    public boolean isPairedEnd() {
        return true;
    }
}
