package com.milaboratory.oncomigec.core.mapping.alignment;

import com.milaboratory.oncomigec.core.mapping.alignment.LocalAlignmentEvaluator;
import com.milaboratory.oncomigec.core.mapping.alignment.Aligner;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;

public abstract class AlignerFactory<AlignerType extends Aligner> {
    protected ReferenceLibrary referenceLibrary;
    protected LocalAlignmentEvaluator localAlignmentEvaluator;

    protected AlignerFactory(ReferenceLibrary referenceLibrary) {
        this(referenceLibrary, new LocalAlignmentEvaluator());
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
