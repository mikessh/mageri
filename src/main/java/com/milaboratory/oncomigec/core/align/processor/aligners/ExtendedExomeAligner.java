package com.milaboratory.oncomigec.core.align.processor.aligners;

import com.milaboratory.core.sequence.Range;
import com.milaboratory.core.sequence.alignment.AffineGapAlignmentScoring;
import com.milaboratory.core.sequence.alignment.LocalAligner;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.align.entity.PAlignmentResult;
import com.milaboratory.oncomigec.core.align.entity.SAlignmentResult;
import com.milaboratory.oncomigec.core.align.kmer.KMerFinder;
import com.milaboratory.oncomigec.core.align.kmer.KMerFinderResult;
import com.milaboratory.oncomigec.core.align.processor.Aligner;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;

import java.util.ArrayList;
import java.util.List;

public class ExtendedExomeAligner implements Aligner {
    private final KMerFinder kMerFinder;
    private final LocalAlignmentEvaluator localAlignmentEvaluator;
    //private final AtomicInteger noHitCounter, badAlignmentCounter;

    public ExtendedExomeAligner(ReferenceLibrary referenceLibrary) {
        this(referenceLibrary, 11, new LocalAlignmentEvaluator());
    }

    public ExtendedExomeAligner(ReferenceLibrary referenceLibrary, int k,
                                LocalAlignmentEvaluator localAlignmentEvaluator) {
        this.kMerFinder = new KMerFinder(referenceLibrary, k);
        this.localAlignmentEvaluator = localAlignmentEvaluator;
    }

    public ExtendedExomeAligner(KMerFinder kMerFinder,
                                LocalAlignmentEvaluator localAlignmentEvaluator) {
        this.kMerFinder = kMerFinder;
        this.localAlignmentEvaluator = localAlignmentEvaluator;
    }

    @Override
    public SAlignmentResult align(NucleotideSequence sequence) {
        KMerFinderResult hit = kMerFinder.find(sequence);

        if (hit == null)
            return null;

        Reference reference = hit.getBestHit();
        LocalAlignment alignment = LocalAligner.align(AffineGapAlignmentScoring.getNucleotideBLASTScoring(),
                reference.getSequence(), sequence);

        if (!localAlignmentEvaluator.isGood(alignment, reference.getSequence(), sequence))
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

        return new PAlignmentResult(result1, result2);
    }

    @Override
    public ReferenceLibrary getReferenceLibrary() {
        return kMerFinder.getReferenceLibrary();
    }
}
