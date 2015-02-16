package com.milaboratory.oncomigec.core.assemble.misc;

import com.milaboratory.oncomigec.core.assemble.entity.PConsensus;
import com.milaboratory.oncomigec.core.assemble.processor.PAssembler;
import com.milaboratory.oncomigec.core.assemble.processor.SAssembler;
import com.milaboratory.oncomigec.core.io.entity.PMig;

public final class PAssemblerFactory extends AssemblerFactory<PConsensus, PMig> {

    public PAssemblerFactory(AssemblerParameters parameters) {
        super(parameters);
    }

    public PAssemblerFactory() {
        super(AssemblerParameters.DEFAULT);
    }

    @Override
    public PAssembler create() {
        return new PAssembler(new SAssembler(parameters), new SAssembler(parameters));
    }
}
