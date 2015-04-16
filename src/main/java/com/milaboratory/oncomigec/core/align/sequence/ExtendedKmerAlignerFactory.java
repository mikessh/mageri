package com.milaboratory.oncomigec.core.align.sequence;

import com.milaboratory.oncomigec.core.align.kmer.KMerFinder;
import com.milaboratory.oncomigec.core.align.AlignerFactory;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;

public class ExtendedKmerAlignerFactory extends AlignerFactory<ExtendedKmerAligner> {
    private int k = KMerFinder.DEFAULT_K;
    private KMerFinder kMerFinder = new KMerFinder(new ReferenceLibrary(), k);

    public ExtendedKmerAlignerFactory(ReferenceLibrary referenceLibrary) {
        super(referenceLibrary);
        setReferenceLibrary(referenceLibrary);
    }

    public ExtendedKmerAlignerFactory(ReferenceLibrary referenceLibrary,
                                      LocalAlignmentEvaluator localAlignmentEvaluator) {
        super(referenceLibrary, localAlignmentEvaluator);
        setReferenceLibrary(referenceLibrary);
    }

    public ExtendedKmerAlignerFactory(ReferenceLibrary referenceLibrary,
                                      LocalAlignmentEvaluator localAlignmentEvaluator, int k) {
        super(referenceLibrary, localAlignmentEvaluator);
        setK(k);
    }

    @Override
    public ExtendedKmerAligner create() {
        return new ExtendedKmerAligner(kMerFinder, localAlignmentEvaluator);
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
        this.kMerFinder = new KMerFinder(referenceLibrary, k);
    }

    @Override
    public void setReferenceLibrary(ReferenceLibrary referenceLibrary) {
        this.referenceLibrary = referenceLibrary;
        this.kMerFinder = new KMerFinder(referenceLibrary, k);
    }

    public LocalAlignmentEvaluator getLocalAlignmentEvaluator() {
        return localAlignmentEvaluator;
    }

    public void setLocalAlignmentEvaluator(LocalAlignmentEvaluator localAlignmentEvaluator) {
        this.localAlignmentEvaluator = localAlignmentEvaluator;
    }
}
