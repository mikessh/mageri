package com.milaboratory.migec2.datasim.model;

public class PCRResult {
    private final long[] nucleotideCounts = new long[4];
    private final long referenceCount, totalCount;
    private final byte referenceCode;

    public PCRResult(long[] nucleotideCounts, byte referenceCode) {
        long totalCount = 0;
        for (int i = 0; i < 4; i++) {
            this.nucleotideCounts[i] += nucleotideCounts[i];
            totalCount += nucleotideCounts[i];
        }
        this.referenceCode = referenceCode;
        this.totalCount = totalCount;
        this.referenceCount = nucleotideCounts[referenceCode];
    }

    public boolean failed() {
        return totalCount == 0;
    }

    public double errorRate() {
        return 1.0 - referenceCount / (double) totalCount;
    }
}
