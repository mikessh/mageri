package com.antigenomics.mageri.core.variant.model;

import com.antigenomics.mageri.core.output.VcfUtil;

public class VariantQuality {
    private final ErrorRateEstimate errorRateEstimate;
    private final double score;

    public VariantQuality(ErrorRateEstimate errorRateEstimate, double score) {
        this.errorRateEstimate = errorRateEstimate;
        this.score = Double.isInfinite(score) ? VcfUtil.MAX_QUAL : (score < 0 ? VcfUtil.UNDEF_QUAL : score);
    }

    public ErrorRateEstimate getErrorRateEstimate() {
        return errorRateEstimate;
    }

    public double getScore() {
        return score;
    }
}
