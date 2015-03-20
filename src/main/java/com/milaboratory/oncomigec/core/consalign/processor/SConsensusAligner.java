package com.milaboratory.oncomigec.core.consalign.processor;

import com.milaboratory.oncomigec.core.align.entity.SAlignmentResult;
import com.milaboratory.oncomigec.core.align.processor.Aligner;
import com.milaboratory.oncomigec.core.assemble.entity.SConsensus;
import com.milaboratory.oncomigec.core.consalign.entity.AlignedConsensus;
import com.milaboratory.oncomigec.core.consalign.misc.ConsensusAlignerParameters;
import com.milaboratory.oncomigec.core.mutations.MigecMutationsCollection;

import java.util.Arrays;

public final class SConsensusAligner extends ConsensusAligner<SConsensus> {

    public SConsensusAligner(Aligner aligner, ConsensusAlignerParameters parameters) {
        super(aligner, parameters);
    }

    public SConsensusAligner(Aligner aligner) {
        super(aligner, ConsensusAlignerParameters.DEFAULT);
    }

    @Override
    public AlignedConsensus align(SConsensus consensus) {
        SAlignmentResult alignmentResult = aligner.align(consensus.getConsensusSQPair().getSequence());

        // Drop if failed to align
        if (alignmentResult == null)
            return null;

        int migSize = parameters.backAlignDroppedReads() ? consensus.fullSize() : consensus.size();

        MigecMutationsCollection majorMutations = update(alignmentResult, consensus);

        return new AlignedConsensus(majorMutations,
                alignmentResult.getReference(), 
                Arrays.asList(alignmentResult.getRange()),
                migSize);
    }

    @Override
    public boolean isPairedEnd() {
        return false;
    }
}
