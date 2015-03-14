package com.milaboratory.oncomigec.core.align.kmer;

import com.milaboratory.oncomigec.core.genomic.Reference;

public class KMerFinderResult {
    private final double information;
    private final Reference bestHit;

    public KMerFinderResult(double information,  Reference bestHit) {
        this.information = information;
        this.bestHit = bestHit;
    }

    public double getInformation() {
        return information;
    }

    public Reference getBestHit() {
        return bestHit;
    }
}
