package com.milaboratory.oncomigec.core.align.processor.aligners;

import com.milaboratory.oncomigec.core.align.processor.AlignerFactory;
import com.milaboratory.oncomigec.core.align.reference.ReferenceLibrary;

public class ExtendedExomeAlignerFactory implements AlignerFactory<ExtendedExomeAligner> {
    private final int k;
    private final LocalAlignmentEvaluator localAlignmentEvaluator;

    public ExtendedExomeAlignerFactory() {
        this(LocalAlignmentEvaluator.STRICT, 11);
    }

    public ExtendedExomeAlignerFactory(LocalAlignmentEvaluator localAlignmentEvaluator) {
        this(localAlignmentEvaluator, 11);
    }

    public ExtendedExomeAlignerFactory(LocalAlignmentEvaluator localAlignmentEvaluator, int k) {
        this.k = k;
        this.localAlignmentEvaluator = localAlignmentEvaluator;
    }

    @Override
    public ExtendedExomeAligner fromReferenceLibrary(ReferenceLibrary referenceLibrary) {
        return new ExtendedExomeAligner(referenceLibrary, k, localAlignmentEvaluator);
    }

    public int getK() {
        return k;
    }

    public LocalAlignmentEvaluator getLocalAlignmentEvaluator() {
        return localAlignmentEvaluator;
    }
}
