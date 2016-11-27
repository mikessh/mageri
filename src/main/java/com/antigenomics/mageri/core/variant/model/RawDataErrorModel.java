/*
 * Copyright 2014-2016 Mikhail Shugay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.antigenomics.mageri.core.variant.model;

import com.antigenomics.mageri.core.mapping.MutationsTable;
import com.antigenomics.mageri.core.mutations.Mutation;
import com.antigenomics.mageri.core.mutations.Substitution;
import com.antigenomics.mageri.core.output.VcfUtil;
import com.milaboratory.core.sequence.mutations.Mutations;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.BinomialDistribution;
import org.apache.commons.math.distribution.BinomialDistributionImpl;

public class RawDataErrorModel implements ErrorModel {
    private final MutationsTable mutationsTable;

    public RawDataErrorModel(MutationsTable mutationsTable) {
        this.mutationsTable = mutationsTable;
    }

    @Override
    public ErrorRateEstimate computeErrorRate(Mutation mutation) {
        int code = ((Substitution) mutation).getCode();

        return computeErrorRate(Mutations.getPosition(code), 0, Mutations.getTo(code));
    }

    @Override
    public ErrorRateEstimate computeErrorRate(int pos, int from, int to) {
        double cqs = mutationsTable.getMeanCqs(pos, to);

        return new ErrorRateEstimate(Math.pow(10.0, -cqs / 10.0) / 3);
    }

    @Override
    public VariantQuality computeQuality(int majorCount, int coverage, Mutation mutation) {
        ErrorRateEstimate errorRateEstimate = computeErrorRate(mutation);
        double score = computeErrorScore(majorCount, coverage, errorRateEstimate.getErrorRate());
        return new VariantQuality(errorRateEstimate, score);
    }

    @Override
    public VariantQuality computeQuality(int majorCount, int coverage, int pos, int from, int to) {
        ErrorRateEstimate errorRateEstimate = computeErrorRate(pos, from, to);
        double score = computeErrorScore(majorCount, coverage, errorRateEstimate.getErrorRate());
        return new VariantQuality(errorRateEstimate, score);
    }

    private static double computeErrorScore(int majorCount, int total, double errorRate) {
        if (majorCount == 0) {
            return 0;
        }

        BinomialDistribution binomialDistribution = new BinomialDistributionImpl(total,
                errorRate);

        double score;
        try {
            score = -10 * Math.log10(1.0 - binomialDistribution.cumulativeProbability(majorCount) +
                    0.5 * binomialDistribution.probability(majorCount));
        } catch (MathException e) {
            e.printStackTrace();
            return -1.0;
        }

        return score;
    }
}
