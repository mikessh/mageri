package com.milaboratory.migec2.core.align.kmer;

import com.milaboratory.migec2.core.align.reference.Reference;

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
