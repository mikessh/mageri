package com.milaboratory.oncomigec.core.align.processor;

import com.milaboratory.oncomigec.core.align.reference.ReferenceLibrary;

public class AlignerFactoryWithReference<T extends Aligner> {
    private final ReferenceLibrary referenceLibrary;
    private final AlignerFactory<T> alignerFactory;

    public AlignerFactoryWithReference(ReferenceLibrary referenceLibrary, AlignerFactory<T> alignerFactory) {
        this.referenceLibrary = referenceLibrary;
        this.alignerFactory = alignerFactory;
    }

    public T create() {
        return alignerFactory.fromReferenceLibrary(referenceLibrary);
    }
}
