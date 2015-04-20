package com.milaboratory.oncomigec.core.mapping.alignment;

import com.milaboratory.core.sequence.alignment.AffineGapAlignmentScoring;
import com.milaboratory.core.sequence.alignment.LocalAligner;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.mapping.kmer.KMerFinder;
import com.milaboratory.oncomigec.core.mapping.kmer.KMerFinderResult;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;

public class ExtendedKmerAligner extends Aligner {
    private final KMerFinder kMerFinder;
    private LocalAlignmentEvaluator localAlignmentEvaluator;

    public ExtendedKmerAligner(ReferenceLibrary referenceLibrary) {
        this(referenceLibrary, 11, new LocalAlignmentEvaluator());
    }

    public ExtendedKmerAligner(ReferenceLibrary referenceLibrary, int k,
                               LocalAlignmentEvaluator localAlignmentEvaluator) {
        this(new KMerFinder(referenceLibrary, k), localAlignmentEvaluator);
    }

    public ExtendedKmerAligner(KMerFinder kMerFinder,
                               LocalAlignmentEvaluator localAlignmentEvaluator) {
        super(kMerFinder.getReferenceLibrary());
        this.kMerFinder = kMerFinder;
        this.localAlignmentEvaluator = localAlignmentEvaluator;
    }

    @Override
    public AlignmentResult align(NucleotideSequence sequence) {
        KMerFinderResult result = kMerFinder.find(sequence);

        if (result == null) {
            // No primary hit
            return null;
        }

        Reference reference = result.getHit();

        boolean rc = result.isReverseComplement();

        if (rc) {
            // account for RC hits
            sequence = sequence.getReverseComplement();
        }

        LocalAlignment alignment = LocalAligner.align(AffineGapAlignmentScoring.getNucleotideBLASTScoring(),
                reference.getSequence(), sequence);

        if (alignment == null) {
            // No local alignment
            return null;
        }

        boolean good = localAlignmentEvaluator.isGood(alignment, reference.getSequence(), sequence);

        return new AlignmentResult(sequence, reference, alignment, rc, result.getScore(), good);
    }

    public KMerFinder getkMerFinder() {
        return kMerFinder;
    }

    public LocalAlignmentEvaluator getLocalAlignmentEvaluator() {
        return localAlignmentEvaluator;
    }

    public void setLocalAlignmentEvaluator(LocalAlignmentEvaluator localAlignmentEvaluator) {
        this.localAlignmentEvaluator = localAlignmentEvaluator;
    }
}
