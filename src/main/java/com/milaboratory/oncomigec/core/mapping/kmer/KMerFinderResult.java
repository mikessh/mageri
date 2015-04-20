package com.milaboratory.oncomigec.core.mapping.kmer;

import com.milaboratory.oncomigec.core.genomic.Reference;

public class KMerFinderResult {
    private final double information, score;
    private final boolean reverseComplement;
    private final Reference hit;

    public KMerFinderResult(double information, double score,
                            Reference hit, boolean reverseComplement) {
        this.information = information;
        this.score = score;
        this.hit = hit;
        this.reverseComplement = reverseComplement;
    }

    public double getInformation() {
        return information;
    }

    public double getScore() {
        return score;
    }

    public Reference getHit() {
        return hit;
    }

    public boolean isReverseComplement() {
        return reverseComplement;
    }
}