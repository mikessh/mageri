package com.milaboratory.migec2.core.consalign.misc;

import com.milaboratory.migec2.core.align.processor.AlignerFactoryWithReference;
import com.milaboratory.migec2.core.assemble.entity.SConsensus;
import com.milaboratory.migec2.core.consalign.processor.SConsensusAligner;

public final class SConsensusAlignerFactory extends ConsensusAlignerFactory<SConsensus> {

    public SConsensusAlignerFactory(AlignerFactoryWithReference alignerFactory, ConsensusAlignerParameters parameters) {
        super(alignerFactory, parameters);
    }

    public SConsensusAlignerFactory(AlignerFactoryWithReference alignerFactory) {
        this(alignerFactory, ConsensusAlignerParameters.DEFAULT);
    }

    @Override
    public SConsensusAligner create() {
        return new SConsensusAligner(alignerFactory.create(), parameters);
    }
}
