package com.milaboratory.migec2.core.io.misc.index;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.migec2.core.io.misc.ReadInfo;

public class IndexingInfo {
    private final ReadInfo readInfo;
    private final String sampleName;
    private final NucleotideSequence umi;
    private final boolean indexed;

    public static IndexingInfo BLANK = new IndexingInfo();

    private IndexingInfo() {
        this.readInfo = null;
        this.sampleName = null;
        this.umi = null;
        this.indexed = false;
    }

    public IndexingInfo(ReadInfo readInfo, String sampleName, NucleotideSequence umi) {
        this.readInfo = readInfo;
        this.sampleName = sampleName;
        this.umi = umi;
        this.indexed = true;
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

    public boolean isIndexed() {
        return indexed;
    }
}
