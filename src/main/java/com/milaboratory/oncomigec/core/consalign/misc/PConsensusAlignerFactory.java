package com.milaboratory.oncomigec.core.consalign.misc;

import com.milaboratory.oncomigec.core.align.processor.AlignerFactory;
import com.milaboratory.oncomigec.core.assemble.entity.PConsensus;
import com.milaboratory.oncomigec.core.consalign.processor.PConsensusAligner;

public final class PConsensusAlignerFactory extends ConsensusAlignerFactory<PConsensus> {

    public PConsensusAlignerFactory(AlignerFactory alignerFactory, ConsensusAlignerParameters parameters) {
        super(alignerFactory, parameters);
    }

    public PConsensusAlignerFactory(AlignerFactory alignerFactory) {
        this(alignerFactory, ConsensusAlignerParameters.DEFAULT);
    }

    @Override
    public PConsensusAligner create() {
        return new PConsensusAligner(alignerFactory.create(), parameters);
    }

    @Override
    public boolean isPairedEnd() {
        return true;
    }
}
