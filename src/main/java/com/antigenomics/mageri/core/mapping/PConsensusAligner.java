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

package com.antigenomics.mageri.core.mapping;

import com.antigenomics.mageri.core.assemble.PConsensus;
import com.antigenomics.mageri.core.assemble.SConsensus;
import com.antigenomics.mageri.core.mapping.alignment.Aligner;
import com.antigenomics.mageri.core.mapping.alignment.AlignmentResult;
import com.antigenomics.mageri.core.mutations.MutationArray;
import com.antigenomics.mageri.misc.Overlapper;
import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.antigenomics.mageri.core.genomic.ReferenceLibrary;
import com.antigenomics.mageri.core.mapping.alignment.ExtendedKmerAligner;

import java.util.HashSet;
import java.util.Set;

public final class PConsensusAligner extends ConsensusAligner<PConsensus, AlignedConsensus> {
    private final Overlapper overlapper = new Overlapper();

    public PConsensusAligner(Aligner aligner, ConsensusAlignerParameters parameters) {
        super(aligner, parameters);
    }

    public PConsensusAligner(Aligner aligner) {
        super(aligner, ConsensusAlignerParameters.DEFAULT);
    }

    public PConsensusAligner(ReferenceLibrary referenceLibrary) {
        super(new ExtendedKmerAligner(referenceLibrary), ConsensusAlignerParameters.DEFAULT);
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
