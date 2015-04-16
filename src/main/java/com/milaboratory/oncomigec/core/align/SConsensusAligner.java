package com.milaboratory.oncomigec.core.align;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.oncomigec.core.align.sequence.Aligner;
import com.milaboratory.oncomigec.core.align.sequence.AlignmentResult;
import com.milaboratory.oncomigec.core.assemble.SConsensus;
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

        // Drop if failed to align
        if (alignmentResult == null)
            return null;

        MutationArray majorMutations = update(alignmentResult, consensus);

        return new AlignedConsensus(consensus.getUmi(),
                majorMutations, alignmentResult,
                consensusSQPair);
    }

    @Override
    public boolean isPairedEnd() {
        return false;
    }
}
