package com.milaboratory.oncomigec.core.consalign.misc;

import com.milaboratory.oncomigec.core.align.processor.AlignerFactory;
import com.milaboratory.oncomigec.core.assemble.entity.SConsensus;
import com.milaboratory.oncomigec.core.consalign.processor.SConsensusAligner;

public final class SConsensusAlignerFactory extends ConsensusAlignerFactory<SConsensus> {

    public SConsensusAlignerFactory(AlignerFactory alignerFactory, ConsensusAlignerParameters parameters) {
        super(alignerFactory, parameters);
    }

    public SConsensusAlignerFactory(AlignerFactory alignerFactory) {
        this(alignerFactory, ConsensusAlignerParameters.DEFAULT);
    }

    @Override
    public SConsensusAligner create() {
        return new SConsensusAligner(alignerFactory.create(), parameters);
    }

    @Override
    public boolean isPairedEnd() {
        return false;
    }
}
