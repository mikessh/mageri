package com.milaboratory.oncomigec.core.consalign.processor;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.oncomigec.core.align.entity.SAlignmentResult;
import com.milaboratory.oncomigec.core.align.processor.Aligner;
import com.milaboratory.oncomigec.core.assemble.entity.SConsensus;
import com.milaboratory.oncomigec.core.consalign.entity.AlignedConsensus;
import com.milaboratory.oncomigec.core.consalign.misc.ConsensusAlignerParameters;
import com.milaboratory.oncomigec.core.consalign.mutations.MutationsExtractor;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.mutations.MigecMutationsCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SConsensusAligner extends ConsensusAligner<SConsensus> {

    public SConsensusAligner(Aligner aligner, ConsensusAlignerParameters parameters) {
        super(aligner, parameters);
    }

    public SConsensusAligner(Aligner aligner) {
        super(aligner, ConsensusAlignerParameters.DEFAULT);
    }

    @Override
    public AlignedConsensus align(SConsensus consensus) {
        Map<Reference, MigecMutationsCollection> majorMutationsByReference = new HashMap<>();

        NucleotideSQPair consensusSQPair = consensus.getConsensusSQPair();

        SAlignmentResult alignmentResult = aligner.align(consensusSQPair.getSequence());

        // Drop if failed to align
        if (alignmentResult == null)
            return null;

        int migSize = parameters.backAlignDroppedReads() ? consensus.fullSize() : consensus.size();

        // Extract all mutations
        List<MigecMutationsCollection> majorMutationsList = new ArrayList<>();
        for (int i = 0; i < alignmentResult.getReferences().size(); i++) {
            Reference reference = alignmentResult.getReferences().get(i);
            MigecMutationsCollection majorMutations = MigecMutationsCollection.EMPTY(reference);
            Map<Integer, Integer> minorMutations = new HashMap<>();

            LocalAlignment localAlignment = alignmentResult.getAlignments().get(i);
            MutationsExtractor mutationsExtractor = new MutationsExtractor(localAlignment,
                    reference, consensus, parameters);

            majorMutations = mutationsExtractor.calculateMajorMutations();
            minorMutations = mutationsExtractor.calculateMinorMutations();

            majorMutationsByReference.put(reference, majorMutations);

            // Append mutations to global container
            alignerReferenceLibrary.appendMutations(reference, majorMutations, minorMutations, migSize);
            majorMutationsList.add(majorMutations);
        }

        // Append coverage
        alignerReferenceLibrary.appendCoverage(alignmentResult, consensusSQPair.getQuality(), migSize);

        return new AlignedConsensus(majorMutationsList,
                alignmentResult.getReferences(), alignmentResult.getRanges(), migSize);
    }

    @Override
    public boolean isPairedEnd() {
        return false;
    }
}
