package com.milaboratory.oncomigec.core.assemble;

import com.milaboratory.oncomigec.core.Mig;
import com.milaboratory.oncomigec.core.ReadSpecific;

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
