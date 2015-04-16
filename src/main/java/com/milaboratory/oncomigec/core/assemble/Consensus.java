package com.milaboratory.oncomigec.core.assemble;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.milaboratory.oncomigec.misc.ReadSpecific;

import java.io.Serializable;

public interface Consensus<ReadType extends SequencingRead> extends Serializable, ReadSpecific {
    public int getAssembledSize();

    public int getTrueSize();

    public NucleotideSequence getUmi();

    public ReadType asRead();
}
