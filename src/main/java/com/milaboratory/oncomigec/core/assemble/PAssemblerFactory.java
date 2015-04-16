package com.milaboratory.oncomigec.core.assemble;

import com.milaboratory.oncomigec.core.input.PMig;

public final class PAssemblerFactory extends AssemblerFactory<PConsensus, PMig> {
    public PAssemblerFactory(AssemblerParameters parameters) {
        super(parameters);
    }

    public PAssemblerFactory() {
    }

    @Override
    public PAssembler create() {
        return new PAssembler(new SAssembler(parameters), new SAssembler(parameters));
    }

    @Override
    public boolean isPairedEnd() {
        return true;
    }
}
