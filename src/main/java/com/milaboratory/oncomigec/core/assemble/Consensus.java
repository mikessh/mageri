package com.milaboratory.oncomigec.core.assemble;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.milaboratory.oncomigec.core.Mig;
import com.milaboratory.oncomigec.pipeline.analysis.Sample;

public abstract class Consensus<ReadType extends SequencingRead> extends Mig {
    public Consensus(Sample sample, NucleotideSequence umi) {
        super(sample, umi);
    }

    @Override
    public int size() {
        return 1;
    }
    
    public abstract int getAssembledSize();

    public abstract int getTrueSize();

    public abstract ReadType asRead();
}
