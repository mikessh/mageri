package com.milaboratory.oncomigec.core.consalign.processor;

import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.oncomigec.core.align.entity.PAlignmentResult;
import com.milaboratory.oncomigec.core.align.entity.SAlignmentResult;
import com.milaboratory.oncomigec.core.align.processor.Aligner;
import com.milaboratory.oncomigec.core.align.reference.Reference;
import com.milaboratory.oncomigec.core.assemble.entity.PConsensus;
import com.milaboratory.oncomigec.core.assemble.entity.SConsensus;
import com.milaboratory.oncomigec.core.consalign.entity.AlignedConsensus;
import com.milaboratory.oncomigec.core.consalign.misc.ConsensusAlignerParameters;
import com.milaboratory.oncomigec.core.consalign.mutations.MutationsExtractor;
import com.milaboratory.oncomigec.core.mutations.MigecMutationsCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PConsensusAligner extends ConsensusAligner<PConsensus> {

    public PConsensusAligner(Aligner aligner, ConsensusAlignerParameters parameters) {
        super(aligner, parameters);
    }

    public PConsensusAligner(Aligner aligner) {
        super(aligner, ConsensusAlignerParameters.DEFAULT);
    }

    @Override
    public AlignedConsensus align(PConsensus pConsensus) {
        Map<Reference, MigecMutationsCollection> majorMutationsByReference = new HashMap<>();
        Map<Reference, Map<Integer, Integer>> minorMutationsByReference = new HashMap<>();

        SConsensus consensus1 = pConsensus.getConsensus1(),
                consensus2 = pConsensus.getConsensus2();

        PAlignmentResult pAlignmentResult = aligner.align(consensus1.getConsensusSQPair().getSequence(),
                consensus2.getConsensusSQPair().getSequence());

        // Drop if failed to align
        if (pAlignmentResult == null)
            return null;

        SAlignmentResult alignmentResult1 = pAlignmentResult.getResult1(),
                alignmentResult2 = pAlignmentResult.getResult2();

        // Extract all mutations from first mate
        for (int i = 0; i < alignmentResult1.getReferences().size(); i++) {
            Reference reference1 = alignmentResult1.getReferences().get(i);
            MigecMutationsCollection majorMutations = MigecMutationsCollection.EMPTY(reference1);
            Map<Integer, Integer> minorMutations = new HashMap<>();

            if (!reference1.isDeNovo()) {
                LocalAlignment localAlignment1 = alignmentResult1.getAlignments().get(i);
                MutationsExtractor mutationsExtractor = new MutationsExtractor(localAlignment1,
                        reference1, consensus1, parameters);

                majorMutations = mutationsExtractor.calculateMajorMutations();
                minorMutations = mutationsExtractor.calculateMinorMutations();
            }

            majorMutationsByReference.put(reference1, majorMutations);
            minorMutationsByReference.put(reference1, minorMutations);
        }

        // Append all mutations from second mate
        for (int i = 0; i < alignmentResult2.getReferences().size(); i++) {
            Reference reference2 = alignmentResult2.getReferences().get(i);
            MigecMutationsCollection majorMutations = majorMutationsByReference.get(reference2);
            Map<Integer, Integer> minorMutations = minorMutationsByReference.get(reference2);

            if (majorMutations == null) {
                majorMutations = MigecMutationsCollection.EMPTY(reference2);
                minorMutations = new HashMap<>();
            }

            if (!reference2.isDeNovo()) {
                LocalAlignment localAlignment2 = alignmentResult2.getAlignments().get(i);
                MutationsExtractor mutationsExtractor = new MutationsExtractor(localAlignment2,
                        reference2, consensus2, parameters);
                // TODO: test mutations don't overlap
                majorMutations.append(mutationsExtractor.calculateMajorMutations());
                Map<Integer, Integer> additionalMinorMutations = mutationsExtractor.calculateMinorMutations();

                for (Map.Entry<Integer, Integer> minorMutationEntry : additionalMinorMutations.entrySet()) {
                    Integer count = minorMutations.get(minorMutationEntry.getKey());
                    if (count == null)
                        minorMutations.put(minorMutationEntry.getKey(), minorMutationEntry.getValue());
                    else
                        minorMutations.put(minorMutationEntry.getKey(), minorMutationEntry.getValue() + count);
                }
            }

            majorMutationsByReference.put(reference2, majorMutations);
            minorMutationsByReference.put(reference2, minorMutations);
        }

        // Append coverage
        alignerReferenceLibrary.appendCoverage(alignmentResult1, consensus1,
                parameters.backAlignDroppedReads() ? pConsensus.getConsensus1().fullSize() :
                        pConsensus.getConsensus1().size()
        );
        alignerReferenceLibrary.appendCoverage(alignmentResult2, consensus2,
                parameters.backAlignDroppedReads() ? pConsensus.getConsensus2().fullSize() :
                        pConsensus.getConsensus2().size()
        );

        // Append mutations to global container
        List<MigecMutationsCollection> majorMutationsList = new ArrayList<>();
        for (Reference reference : majorMutationsByReference.keySet()) {
            MigecMutationsCollection majorMutations = majorMutationsByReference.get(reference);
            // TODO: re-implement, mutations shouldn't overlap
            alignerReferenceLibrary.appendMutations(reference,
                    majorMutations, minorMutationsByReference.get(reference),
                    parameters.backAlignDroppedReads() ? pConsensus.fullSize() : pConsensus.size());
            majorMutationsList.add(majorMutations);
        }

        return new AlignedConsensus(majorMutationsList,
                pAlignmentResult.getReferences(), pAlignmentResult.getRanges(),
                parameters.backAlignDroppedReads() ? pConsensus.fullSize() : pConsensus.size());
    }

    @Override
    public boolean isPairedEnd() {
        return true;
    }
}
