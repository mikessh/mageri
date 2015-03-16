package com.milaboratory.oncomigec.core.assemble.entity;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.milaboratory.oncomigec.util.QualityHistogram;

import java.io.Serializable;

public interface Consensus extends Serializable {
    public int size();

    public int fullSize();

    public NucleotideSequence getUmi();

    public String formattedSequence();

    public QualityHistogram getQualityHistogram();
    
    public SequencingRead asRead();
}
