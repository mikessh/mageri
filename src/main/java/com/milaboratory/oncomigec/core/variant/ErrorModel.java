/*
 * Copyright 2013-2015 Mikhail Shugay (mikhail.shugay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last modified on 9.4.2015 by mikesh
 */

package com.milaboratory.oncomigec.core.variant;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.BinomialDistribution;
import org.apache.commons.math.distribution.BinomialDistributionImpl;

public class ErrorModel {
    private double cycles, lambda;
    private double propagateProb;

    public ErrorModel() {
        this(20, 0.95);
    }

    public ErrorModel(double cycles, double lambda) {
        this.cycles = cycles;
        this.lambda = lambda;
        calcPropagateProb();
    }

    public double getLog10PValue(int majorCount, int minorCount, int total) {
        double errorRateBase = Math.pow(1.0 - minorCount / (double) total, 1.0 / cycles),
                errorRate = Math.log(lambda) - Math.log((1.0 + lambda) * errorRateBase - 1);

        BinomialDistribution binomialDistribution = new BinomialDistributionImpl(total,
                errorRate * propagateProb);

        try {
            return -Math.log10(1.0 - binomialDistribution.cumulativeProbability(majorCount) +
                    0.5 * binomialDistribution.probability(majorCount));
        } catch (MathException e) {
            e.printStackTrace();
            return -Math.log10(binomialDistribution.probability(majorCount));
        }
    }

    public double getCycles() {
        return cycles;
    }

    public double getLambda() {
        return lambda;
    }

    public void setCycles(double cycles) {
        this.cycles = cycles;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
        calcPropagateProb();
    }

    private void calcPropagateProb() {
        this.propagateProb = lambda * (1 - lambda);
    }
}
