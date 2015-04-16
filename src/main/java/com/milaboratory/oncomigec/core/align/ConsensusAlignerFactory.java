package com.milaboratory.oncomigec.core.align;

import com.milaboratory.oncomigec.misc.ReadSpecific;
import com.milaboratory.oncomigec.core.assemble.Consensus;

public abstract class ConsensusAlignerFactory<T extends Consensus> implements ReadSpecific {
    protected final AlignerFactory alignerFactory;
    protected final ConsensusAlignerParameters parameters;

    protected ConsensusAlignerFactory(AlignerFactory alignerFactory, ConsensusAlignerParameters parameters) {
        this.alignerFactory = alignerFactory;
        this.parameters = parameters;
    }

    public abstract ConsensusAligner<T> create();

    public ConsensusAlignerParameters getParameters() {
        return parameters;
    }
}
