package com.milaboratory.migec2.core.assemble.misc;

import com.milaboratory.migec2.core.assemble.entity.Consensus;
import com.milaboratory.migec2.core.assemble.processor.Assembler;
import com.milaboratory.migec2.core.io.entity.Mig;

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
