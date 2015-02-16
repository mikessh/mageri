package com.milaboratory.oncomigec.core.assemble.misc;

import com.milaboratory.oncomigec.core.assemble.entity.SConsensus;
import com.milaboratory.oncomigec.core.assemble.processor.SAssembler;
import com.milaboratory.oncomigec.core.io.entity.SMig;

public final class SAssemblerFactory extends AssemblerFactory<SConsensus, SMig> {

    public SAssemblerFactory(AssemblerParameters parameters) {
        super(parameters);
    }

    public SAssemblerFactory() {
        super(AssemblerParameters.DEFAULT);
    }

    @Override
    public SAssembler create() {
        return new SAssembler(parameters);
    }
}
