package com.milaboratory.migec2.core.assemble.misc;

import com.milaboratory.migec2.core.assemble.entity.SConsensus;
import com.milaboratory.migec2.core.assemble.processor.SAssembler;
import com.milaboratory.migec2.core.io.entity.SMig;

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
