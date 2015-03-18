package com.milaboratory.oncomigec.core.align.processor;

import com.milaboratory.oncomigec.core.align.processor.aligners.LocalAlignmentEvaluator;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;

public abstract class AlignerFactory<AlignerType extends Aligner> {
    protected ReferenceLibrary referenceLibrary;
    protected LocalAlignmentEvaluator localAlignmentEvaluator;

    protected AlignerFactory(ReferenceLibrary referenceLibrary) {
        this(referenceLibrary, LocalAlignmentEvaluator.STRICT);
    }

    protected AlignerFactory(ReferenceLibrary referenceLibrary,
                             LocalAlignmentEvaluator localAlignmentEvaluator) {
        this.referenceLibrary = referenceLibrary;
        this.localAlignmentEvaluator = localAlignmentEvaluator;
    }

    public ReferenceLibrary getReferenceLibrary() {
        return referenceLibrary;
    }

    public void setReferenceLibrary(ReferenceLibrary referenceLibrary) {
        this.referenceLibrary = referenceLibrary;
    }

    public LocalAlignmentEvaluator getLocalAlignmentEvaluator() {
        return localAlignmentEvaluator;
    }

    public void setLocalAlignmentEvaluator(LocalAlignmentEvaluator localAlignmentEvaluator) {
        this.localAlignmentEvaluator = localAlignmentEvaluator;
    }

    public abstract AlignerType create();
}
