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

package com.antigenomics.mageri.core.variant;

import java.io.Serializable;

public class ErrorModel implements Serializable {
    public static final int COVERAGE_THRESHOLD = 100;

    private double order;
    private double cycles, lambda;
    private double propagateProb;

    public ErrorModel() {
        this(1.0, 20, 0.95);
    }

    public ErrorModel(double order, double cycles, double lambda) {
        this.order = order;
        this.cycles = cycles;
        this.lambda = lambda;
        calcPropagateProb();
    }

    public double getErrorRate(int minorCount, int total,
                               int from, int to,
                               MinorMatrix minorMatrix) {
        minorCount = minorCount > 0 ? minorCount : 1;

        double rate = Math.max(total < COVERAGE_THRESHOLD ? 1.0 / (double) total : (minorCount / (double) total),
                minorMatrix.getRate(from, to));

        double errorRateBase = Math.pow(1.0 - rate, 1.0 / cycles);

        return (Math.log(lambda) - Math.log((1.0 + lambda) * errorRateBase - 1)) * propagateProb;
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

    public double getOrder() {
        return order;
    }

    public void setOrder(double order) {
        this.order = order;
        calcPropagateProb();
    }

    public double getPropagateProb() {
        return propagateProb;
    }

    private void calcPropagateProb() {
        this.propagateProb = Math.pow((1.0 - lambda), order) * Math.pow(lambda, order + 1);
    }
}
