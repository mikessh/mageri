package com.milaboratory.oncomigec.core.align.processor.aligners;

import com.milaboratory.core.sequence.alignment.AffineGapAlignmentScoring;
import com.milaboratory.core.sequence.alignment.LocalAligner;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.align.entity.SAlignmentResult;
import com.milaboratory.oncomigec.core.align.kmer.KMerFinder;
import com.milaboratory.oncomigec.core.align.kmer.KMerFinderResult;
import com.milaboratory.oncomigec.core.align.processor.Aligner;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;

public class ExtendedExomeAligner extends Aligner {
    private final KMerFinder kMerFinder;
    private final LocalAlignmentEvaluator localAlignmentEvaluator;

    public ExtendedExomeAligner(ReferenceLibrary referenceLibrary) {
        this(referenceLibrary, 11, new LocalAlignmentEvaluator());
    }

    public ExtendedExomeAligner(ReferenceLibrary referenceLibrary, int k,
                                LocalAlignmentEvaluator localAlignmentEvaluator) {
        this(new KMerFinder(referenceLibrary, k), localAlignmentEvaluator);
    }

    public ExtendedExomeAligner(KMerFinder kMerFinder,
                                LocalAlignmentEvaluator localAlignmentEvaluator) {
        super(kMerFinder.getReferenceLibrary());
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

        return new SAlignmentResult(alignment, reference);
    }
}
