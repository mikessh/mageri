package com.milaboratory.oncomigec.core.consalign.misc;

import com.milaboratory.oncomigec.core.ReadSpecific;
import com.milaboratory.oncomigec.core.align.processor.AlignerFactory;
import com.milaboratory.oncomigec.core.assemble.entity.Consensus;
import com.milaboratory.oncomigec.core.consalign.processor.ConsensusAligner;

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
