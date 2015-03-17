package com.milaboratory.oncomigec.core.assemble.entity;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.milaboratory.oncomigec.ReadSpecific;

import java.io.Serializable;

public interface Consensus<ReadType extends SequencingRead> extends Serializable, ReadSpecific {
    public int size();

    public int fullSize();

    public NucleotideSequence getUmi();

    public ReadType asRead();

    public byte getMinQual();

    public byte getMaxQual();

    public byte getAvgQual();
}
