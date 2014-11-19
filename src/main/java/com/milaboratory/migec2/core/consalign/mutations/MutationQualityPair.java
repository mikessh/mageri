package com.milaboratory.migec2.core.consalign.mutations;

public class MutationQualityPair {
    private final int mutationCode;
    private final byte quality;

    public MutationQualityPair(int mutationCode, byte quality) {
        this.mutationCode = mutationCode;
        this.quality = quality;
    }

    public int getMutationCode() {
        return mutationCode;
    }

    public byte getQuality() {
        return quality;
    }
}
