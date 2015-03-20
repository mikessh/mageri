package com.milaboratory.oncomigec.core.consalign.processor;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.oncomigec.core.align.entity.PAlignmentResult;
import com.milaboratory.oncomigec.core.align.processor.Aligner;
import com.milaboratory.oncomigec.core.assemble.entity.PConsensus;
import com.milaboratory.oncomigec.core.assemble.entity.SConsensus;
import com.milaboratory.oncomigec.core.consalign.entity.AlignedConsensus;
import com.milaboratory.oncomigec.core.consalign.misc.ConsensusAlignerParameters;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.mutations.MigecMutationsCollection;

import java.util.Arrays;

public final class PConsensusAligner extends ConsensusAligner<PConsensus> {
    private final ConsensusOverlapper consensusOverlapper = new ConsensusOverlapper();

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

        // Make sure consensuses do not overlap to escape from mutation merging hell
        ConsensusOverlapper.OverlapResult overlapResult = consensusOverlapper.overlap(consensus1.getConsensusSQPair(),
                consensus2.getConsensusSQPair());

        NucleotideSQPair trimmedConsensus1 = overlapResult.getConsensus1(),
                trimmedConsensus2 = overlapResult.getConsensus2();

        PAlignmentResult pAlignmentResult = aligner.align(trimmedConsensus1.getSequence(),
                trimmedConsensus2.getSequence());

        // Drop if failed to align
        if (pAlignmentResult == null)
            return null;

        // Chimeric not allowed here
        if (pAlignmentResult.isChimeric()) {
            chimericMigs.incrementAndGet();
            return null;
        }

        Reference reference = pAlignmentResult.getResult1().getReference();
        MigecMutationsCollection majorMutations = MigecMutationsCollection.EMPTY(reference);

        majorMutations.append(update(pAlignmentResult.getResult1(), consensus1, trimmedConsensus1, true));
        majorMutations.append(update(pAlignmentResult.getResult2(), consensus2, trimmedConsensus2, false));


        return new AlignedConsensus(majorMutations,
                reference,
                Arrays.asList(pAlignmentResult.getResult1().getRange(),
                        pAlignmentResult.getResult2().getRange()),
                parameters.backAlignDroppedReads() ? pConsensus.fullSize() : pConsensus.size());
    }

    @Override
    public boolean isPairedEnd() {
        return true;
    }
}
