package com.milaboratory.oncomigec.core.assemble;

import com.milaboratory.oncomigec.misc.ReadSpecific;
import com.milaboratory.oncomigec.core.input.Mig;

public abstract class AssemblerFactory<T extends Consensus, V extends Mig> implements ReadSpecific {
    protected final AssemblerParameters parameters;

    protected AssemblerFactory(AssemblerParameters parameters) {
        this.parameters = parameters;
    }

    protected AssemblerFactory() {
        this(AssemblerParameters.DEFAULT);
    }

    public abstract Assembler<T, V> create();

    public AssemblerParameters getParameters() {
        return parameters;
    }
}
