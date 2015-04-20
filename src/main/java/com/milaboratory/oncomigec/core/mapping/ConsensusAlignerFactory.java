package com.milaboratory.oncomigec.core.mapping;

import com.milaboratory.oncomigec.core.ReadSpecific;
import com.milaboratory.oncomigec.core.assemble.Consensus;
import com.milaboratory.oncomigec.core.mapping.alignment.AlignerFactory;

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
