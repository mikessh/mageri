package com.milaboratory.migec2.core.align.processor.aligners;

import com.milaboratory.core.sequence.Range;
import com.milaboratory.core.sequence.alignment.AffineGapAlignmentScoring;
import com.milaboratory.core.sequence.alignment.LocalAligner;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.migec2.core.align.entity.PAlignmentResult;
import com.milaboratory.migec2.core.align.entity.SAlignmentResult;
import com.milaboratory.migec2.core.align.kmer.KMerFinder;
import com.milaboratory.migec2.core.align.kmer.KMerFinderResult;
import com.milaboratory.migec2.core.align.processor.Aligner;
import com.milaboratory.migec2.core.align.reference.Reference;
import com.milaboratory.migec2.core.align.reference.ReferenceLibrary;

import java.util.ArrayList;
import java.util.List;

public class ExtendedExomeAligner implements Aligner {
    private final KMerFinder finder;
    private final ReferenceLibrary referenceLibrary;
    private final LocalAlignmentEvaluator localAlignmentEvaluator;

    public ExtendedExomeAligner(ReferenceLibrary referenceLibrary) {
        this(referenceLibrary, 11, LocalAlignmentEvaluator.STRICT);
    }

    public ExtendedExomeAligner(ReferenceLibrary referenceLibrary, int k,
                                LocalAlignmentEvaluator localAlignmentEvaluator) {
        this.finder = new KMerFinder(referenceLibrary, k);
        this.referenceLibrary = referenceLibrary;
        this.localAlignmentEvaluator = localAlignmentEvaluator;
    }

    @Override
    public SAlignmentResult align(NucleotideSequence sequence) {
        KMerFinderResult hit = finder.find(sequence);

        if (hit == null)
            return null;

        Reference reference = hit.getBestHit();
        LocalAlignment alignment = LocalAligner.align(AffineGapAlignmentScoring.getNucleotideBLASTScoring(),
                reference.getSequence(), sequence);

        if (!localAlignmentEvaluator.isGood(alignment, sequence))
            return null;

        List<LocalAlignment> alignmentBlocks = new ArrayList<>();
        alignmentBlocks.add(alignment);

        List<Reference> referenceIds = new ArrayList<>();
        referenceIds.add(reference);

        List<Range> rangeList = new ArrayList<>();
        rangeList.add(new Range(0, reference.getSequence().size())); // unused here

        return new SAlignmentResult(alignmentBlocks, referenceIds, rangeList);
    }

    @Override
    public PAlignmentResult align(NucleotideSequence sequence1, NucleotideSequence sequence2) {
        SAlignmentResult result1 = align(sequence1), result2 = align(sequence2);

        if (result1 == null || result2 == null ||
                !result1.getReferences().get(0).equals(result2.getReferences().get(0))) // chimeras not allowed here
            return null;

        if (result1.getAlignments().get(0).getSequence1Range().intersectsWith(
                result2.getAlignments().get(0).getSequence1Range()
        ))
            return null; // by convention reads cannot overlap, this also fixes issues from indels at read junction

        return new PAlignmentResult(result1, result2);
    }

    @Override
    public ReferenceLibrary getReferenceLibrary() {
        return referenceLibrary;
    }
}
