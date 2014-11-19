package com.milaboratory.migec2.core.consalign.misc;

import com.milaboratory.migec2.core.align.processor.AlignerFactoryWithReference;
import com.milaboratory.migec2.core.assemble.entity.Consensus;
import com.milaboratory.migec2.core.consalign.processor.ConsensusAligner;

public abstract class ConsensusAlignerFactory<T extends Consensus> {
    protected final AlignerFactoryWithReference alignerFactory;
    protected final ConsensusAlignerParameters parameters;

    protected ConsensusAlignerFactory(AlignerFactoryWithReference alignerFactory, ConsensusAlignerParameters parameters) {
        this.alignerFactory = alignerFactory;
        this.parameters = parameters;
    }

    public abstract ConsensusAligner<T> create();

    public ConsensusAlignerParameters getParameters() {
        return parameters;
    }
}
