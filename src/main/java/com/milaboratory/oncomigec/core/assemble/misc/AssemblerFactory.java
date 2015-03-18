package com.milaboratory.oncomigec.core.assemble.misc;

import com.milaboratory.oncomigec.ReadSpecific;
import com.milaboratory.oncomigec.core.assemble.entity.Consensus;
import com.milaboratory.oncomigec.core.assemble.processor.Assembler;
import com.milaboratory.oncomigec.core.io.entity.Mig;

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
