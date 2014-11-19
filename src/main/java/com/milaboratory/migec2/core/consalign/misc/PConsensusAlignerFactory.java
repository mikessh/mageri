package com.milaboratory.migec2.core.consalign.misc;

import com.milaboratory.migec2.core.align.processor.AlignerFactoryWithReference;
import com.milaboratory.migec2.core.assemble.entity.PConsensus;
import com.milaboratory.migec2.core.consalign.processor.PConsensusAligner;

public final class PConsensusAlignerFactory extends ConsensusAlignerFactory<PConsensus> {

    public PConsensusAlignerFactory(AlignerFactoryWithReference alignerFactory, ConsensusAlignerParameters parameters) {
        super(alignerFactory, parameters);
    }

    public PConsensusAlignerFactory(AlignerFactoryWithReference alignerFactory) {
        this(alignerFactory, ConsensusAlignerParameters.DEFAULT);
    }

    @Override
    public PConsensusAligner create() {
        return new PConsensusAligner(alignerFactory.create(), parameters);
    }
}
