package com.milaboratory.oncomigec.core.mapping;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.oncomigec.core.assemble.SConsensus;
import com.milaboratory.oncomigec.core.mapping.alignment.Aligner;
import com.milaboratory.oncomigec.core.mapping.alignment.AlignmentResult;
import com.milaboratory.oncomigec.core.mutations.MutationArray;

public final class SConsensusAligner extends ConsensusAligner<SConsensus> {

    public SConsensusAligner(Aligner aligner, ConsensusAlignerParameters parameters) {
        super(aligner, parameters);
    }

    public SConsensusAligner(Aligner aligner) {
        super(aligner, ConsensusAlignerParameters.DEFAULT);
    }

    @Override
    public AlignedConsensus align(SConsensus consensus) {
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
