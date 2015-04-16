package com.milaboratory.oncomigec.core.input.index;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;

public class IndexingInfo {
    private final ReadInfo readInfo;
    private final String sampleName; // TODO: IMPORTANT, change from string to ref or int
    private final NucleotideSequence umi;

    public IndexingInfo(ReadInfo readInfo, String sampleName, NucleotideSequence umi) {
        this.readInfo = readInfo;
        this.sampleName = sampleName;
        this.umi = umi;
    }

    public ReadInfo getReadInfo() {
        return readInfo;
    }

    public String getSampleName() {
        return sampleName;
    }

    public NucleotideSequence getUmi() {
        return umi;
    }
}
