package com.milaboratory.migec2.core.assemble.entity;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.migec2.util.QualityHistogram;

public interface Consensus {
    public int size();

    public int fullSize();

    public NucleotideSequence getUmi();

    public String formattedSequence();

    public QualityHistogram getQualityHistogram();
}
