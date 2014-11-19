package com.milaboratory.migec2.core.align.processor.aligners;

import com.milaboratory.core.sequence.alignment.KAlignerParameters;
import com.milaboratory.migec2.core.align.processor.AlignerFactory;
import com.milaboratory.migec2.core.align.reference.ReferenceLibrary;

public class SimpleExomeAlignerFactory implements AlignerFactory<SimpleExomeAligner> {
    private final KAlignerParameters parameters;
    private final LocalAlignmentEvaluator localAlignmentEvaluator;

    public SimpleExomeAlignerFactory() {
        this(KAlignerParameters.getByName("strict"), LocalAlignmentEvaluator.STRICT);
    }

    public SimpleExomeAlignerFactory(KAlignerParameters parameters,
                                     LocalAlignmentEvaluator localAlignmentEvaluator) {
        this.parameters = parameters;
        this.localAlignmentEvaluator = localAlignmentEvaluator;
    }

    @Override
    public SimpleExomeAligner fromReferenceLibrary(ReferenceLibrary referenceLibrary) {
        return new SimpleExomeAligner(referenceLibrary, parameters, localAlignmentEvaluator);
    }

    public KAlignerParameters getParameters() {
        return parameters;
    }

    public LocalAlignmentEvaluator getLocalAlignmentEvaluator() {
        return localAlignmentEvaluator;
    }
}
