package com.milaboratory.oncomigec.core.assemble;

import com.milaboratory.oncomigec.core.input.SMig;

public final class SAssemblerFactory extends AssemblerFactory<SConsensus, SMig> {
    public SAssemblerFactory(AssemblerParameters parameters) {
        super(parameters);
    }

    public SAssemblerFactory() {
    }

    @Override
    public SAssembler create() {
        return new SAssembler(parameters);
    }

    @Override
    public boolean isPairedEnd() {
        return false;
    }
}
