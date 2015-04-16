package com.milaboratory.oncomigec.core.align;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.oncomigec.core.align.sequence.Aligner;
import com.milaboratory.oncomigec.core.align.sequence.AlignmentResult;
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
            overlapped.incrementAndGet();
            NucleotideSQPair consensusSQPair = overlapResult.getSQPair();
            AlignmentResult alignmentResult = aligner.align(consensusSQPair.getSequence());

            if (alignmentResult == null) {
                failedOverlapped.incrementAndGet();
                return null;
            }

            // Prepare minors accordingly
            Set<Integer> minors = new HashSet<>();

            for (int minor : consensus1.getMinors()) {
                minors.add(Mutations.move(minor, overlapResult.getOffset1()));
            }

            for (int minor : consensus2.getMinors()) {
                minors.add(Mutations.move(minor, overlapResult.getOffset2()));
            }

            MutationArray majorMutations = update(alignmentResult, consensusSQPair, minors);

            return new AlignedConsensus(consensus1.getUmi(),
                    majorMutations, alignmentResult, consensusSQPair);
        } else {
            MutationArray mutations1 = null, mutations2 = null;

            AlignmentResult alignmentResult1 = aligner.align(consensusSQPair1.getSequence()),
                    alignmentResult2 = aligner.align(consensusSQPair2.getSequence());

            if (alignmentResult1 != null) {
                failedR1.incrementAndGet();
                mutations1 = update(alignmentResult1, consensus1);
            }
            if (alignmentResult2 != null) {
                failedR2.incrementAndGet();
                mutations2 = update(alignmentResult2, consensus2);
            }

            if (alignmentResult1 == null && alignmentResult2 == null) {
                return null;
            }

            AlignedConsensus alignedConsensus = new AlignedConsensus(consensus1.getUmi(),
                    mutations1, mutations2,
                    alignmentResult1, alignmentResult2,
                    consensusSQPair1, consensusSQPair2);

            if (alignedConsensus.chimeric()) {
                chimericMigs.incrementAndGet();
            }

            return alignedConsensus;
        }
    }

    @Override
    public boolean isPairedEnd() {
        return true;
    }
}
