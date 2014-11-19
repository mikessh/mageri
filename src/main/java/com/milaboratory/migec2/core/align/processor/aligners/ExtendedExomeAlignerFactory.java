package com.milaboratory.migec2.core.align.processor.aligners;

import com.milaboratory.migec2.core.align.processor.AlignerFactory;
import com.milaboratory.migec2.core.align.reference.ReferenceLibrary;

public class ExtendedExomeAlignerFactory implements AlignerFactory<ExtendedExomeAligner> {
    private final int k;
    private final LocalAlignmentEvaluator localAlignmentEvaluator;

    public ExtendedExomeAlignerFactory() {
        this(11, LocalAlignmentEvaluator.STRICT);
    }

    public ExtendedExomeAlignerFactory(int k, LocalAlignmentEvaluator localAlignmentEvaluator) {
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
