package com.milaboratory.oncomigec.core.align.processor.aligners;

import com.milaboratory.core.sequence.alignment.KAlignerParameters;
import com.milaboratory.oncomigec.core.align.processor.AlignerFactory;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;

public class SimpleExomeAlignerFactory extends AlignerFactory<SimpleExomeAligner> {
    private KAlignerParameters parameters = SimpleExomeAligner.DEFAULT_PARAMS;

    public SimpleExomeAlignerFactory(ReferenceLibrary referenceLibrary) {
        super(referenceLibrary);
    }

    public SimpleExomeAlignerFactory(ReferenceLibrary referenceLibrary,
                                     LocalAlignmentEvaluator localAlignmentEvaluator) {
        super(referenceLibrary, localAlignmentEvaluator);
    }

    public SimpleExomeAlignerFactory(ReferenceLibrary referenceLibrary,
                                     LocalAlignmentEvaluator localAlignmentEvaluator,
                                     KAlignerParameters parameters) {
        super(referenceLibrary, localAlignmentEvaluator);
        this.parameters = parameters;
    }

    @Override
    public SimpleExomeAligner create() {
        return new SimpleExomeAligner(referenceLibrary, parameters, localAlignmentEvaluator);
    }

    public KAlignerParameters getParameters() {
        return parameters;
    }

    public void setParameters(KAlignerParameters parameters) {
        this.parameters = parameters;
    }
}
