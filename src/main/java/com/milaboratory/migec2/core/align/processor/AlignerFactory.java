package com.milaboratory.migec2.core.align.processor;

import com.milaboratory.migec2.core.align.reference.ReferenceLibrary;

public interface AlignerFactory <T extends Aligner> {
    public T fromReferenceLibrary(ReferenceLibrary referenceLibrary);
}
