package com.milaboratory.oncomigec.core.align.processor.aligners;

import com.milaboratory.oncomigec.core.align.kmer.KMerFinder;
import com.milaboratory.oncomigec.core.align.processor.AlignerFactory;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;

public class ExtendedExomeAlignerFactory extends AlignerFactory<ExtendedExomeAligner> {
    private int k = KMerFinder.DEFAULT_K;
    private KMerFinder kMerFinder = new KMerFinder(new ReferenceLibrary(), k);
    
    public ExtendedExomeAlignerFactory(ReferenceLibrary referenceLibrary) {
        super(referenceLibrary);
    }

    public ExtendedExomeAlignerFactory(ReferenceLibrary referenceLibrary,
                                       LocalAlignmentEvaluator localAlignmentEvaluator) {
        super(referenceLibrary, localAlignmentEvaluator);
    }

    public ExtendedExomeAlignerFactory(ReferenceLibrary referenceLibrary,
                                       LocalAlignmentEvaluator localAlignmentEvaluator, int k) {
        super(referenceLibrary, localAlignmentEvaluator);
        setK(k);
    }

    @Override
    public ExtendedExomeAligner create() {
        return new ExtendedExomeAligner(kMerFinder, localAlignmentEvaluator);
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
