package com.milaboratory.oncomigec.core.assemble.misc;

import com.milaboratory.oncomigec.core.assemble.entity.Consensus;
import com.milaboratory.oncomigec.core.assemble.processor.Assembler;
import com.milaboratory.oncomigec.core.io.entity.Mig;

public abstract class AssemblerFactory<T extends Consensus, V extends Mig> {
    protected final AssemblerParameters parameters;

    protected AssemblerFactory(AssemblerParameters parameters) {
        this.parameters = parameters;
    }

    public abstract Assembler<T, V> create();

    public AssemblerParameters getParameters() {
        return parameters;
    }
}
