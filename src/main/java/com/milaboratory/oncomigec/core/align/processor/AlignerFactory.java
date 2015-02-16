package com.milaboratory.oncomigec.core.align.processor;

import com.milaboratory.oncomigec.core.align.reference.ReferenceLibrary;

public interface AlignerFactory <T extends Aligner> {
    public T fromReferenceLibrary(ReferenceLibrary referenceLibrary);
}
