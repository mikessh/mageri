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

import com.antigenomics.mageri.core.assemble.SConsensus;
import com.antigenomics.mageri.core.genomic.ReferenceLibrary;
import com.antigenomics.mageri.core.mapping.alignment.Aligner;
import com.antigenomics.mageri.core.mapping.alignment.AlignmentResult;
import com.antigenomics.mageri.core.mutations.MutationArray;
import com.milaboratory.core.sequence.NucleotideSQPair;
import com.antigenomics.mageri.core.mapping.alignment.ExtendedKmerAligner;

public final class SConsensusAligner extends ConsensusAligner<SConsensus, SAlignedConsensus> {

    public SConsensusAligner(Aligner aligner, ConsensusAlignerParameters parameters) {
        super(aligner, parameters);
    }

    public SConsensusAligner(Aligner aligner) {
        super(aligner, ConsensusAlignerParameters.DEFAULT);
    }

    public SConsensusAligner(ReferenceLibrary referenceLibrary) {
        super(new ExtendedKmerAligner(referenceLibrary), ConsensusAlignerParameters.DEFAULT);
    }

    @Override
    public SAlignedConsensus align(SConsensus consensus) {
        NucleotideSQPair consensusSQPair = consensus.getConsensusSQPair();
        AlignmentResult alignmentResult = aligner.align(consensusSQPair.getSequence());

        MutationArray mutations = alignmentResult != null ? extractMutations(alignmentResult, consensus) : null;

        return new SAlignedConsensus(consensus.getSample(), consensus.getUmi(),
                consensusSQPair, alignmentResult, mutations);
    }

    @Override
    public boolean isPairedEnd() {
        return false;
    }
}
