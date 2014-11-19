package com.milaboratory.migec2.core.consalign.processor;

import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.migec2.core.align.entity.SAlignmentResult;
import com.milaboratory.migec2.core.align.processor.Aligner;
import com.milaboratory.migec2.core.align.reference.Reference;
import com.milaboratory.migec2.core.assemble.entity.SConsensus;
import com.milaboratory.migec2.core.consalign.entity.AlignedConsensus;
import com.milaboratory.migec2.core.consalign.misc.ConsensusAlignerParameters;
import com.milaboratory.migec2.core.consalign.mutations.MutationsExtractor;
import com.milaboratory.migec2.core.mutations.MigecMutationsCollection;

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

        SAlignmentResult alignmentResult = aligner.align(consensus.getConsensusSQPair().getSequence());

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

            if (!reference.isDeNovo()) {
                LocalAlignment localAlignment = alignmentResult.getAlignments().get(i);
                MutationsExtractor mutationsExtractor = new MutationsExtractor(localAlignment,
                        reference, consensus, parameters);

                majorMutations = mutationsExtractor.calculateMajorMutations();
                minorMutations = mutationsExtractor.calculateMinorMutations();

                mutationsExtractor.updateVariantSizeStatistics(variantSizeLibrary);
            }

            majorMutationsByReference.put(reference, majorMutations);

            // Append mutations to global container
            alignerReferenceLibrary.appendMutations(reference, majorMutations, minorMutations, migSize);
            majorMutationsList.add(majorMutations);
        }

        // Append coverage
        alignerReferenceLibrary.appendCoverage(alignmentResult, consensus, migSize);

        return new AlignedConsensus(majorMutationsList,
                alignmentResult.getReferences(), alignmentResult.getRanges(), migSize);
    }
}
