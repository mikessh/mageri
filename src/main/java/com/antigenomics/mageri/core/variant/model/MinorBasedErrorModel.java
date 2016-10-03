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

import com.antigenomics.mageri.core.assemble.MinorCaller;
import com.antigenomics.mageri.core.mapping.MutationsTable;
import com.antigenomics.mageri.core.mutations.Mutation;
import com.antigenomics.mageri.core.mutations.Substitution;
import com.milaboratory.core.sequence.mutations.Mutations;

public class MinorBasedErrorModel implements ErrorModel {
    private final int coverageThreshold, minorCountThreshold;
    private final double order, cycles, lambda, propagateProb;
    private final MutationsTable mutationsTable;
    private final SubstitutionErrorMatrix substitutionErrorMatrix;
    private final MinorCaller minorCaller;

    public MinorBasedErrorModel(double order, double cycles, double efficiency,
                                int coverageThreshold, int minorCountThreshold,
                                MutationsTable mutationsTable, MinorCaller minorCaller) {
        this.order = order;
        this.cycles = cycles;
        this.lambda = efficiency - 1;
        this.propagateProb = computePropagateProb(lambda, order);
        this.mutationsTable = mutationsTable;
        this.minorCaller = minorCaller;
        this.substitutionErrorMatrix = SubstitutionErrorMatrix.fromMutationsTable(mutationsTable);
        this.coverageThreshold = coverageThreshold;
        this.minorCountThreshold = minorCountThreshold;
    }

    public double getCycles() {
        return cycles;
    }

    public double getLambda() {
        return lambda;
    }

    public double getOrder() {
        return order;
    }

    public double getPropagateProb() {
        return propagateProb;
    }

    public int getMinorCountThreshold() {
        return minorCountThreshold;
    }

    public int getCoverageThreshold() {
        return coverageThreshold;
    }

    public static double computePropagateProb(double efficiency, double order) {
        double lambda = efficiency - 1;
        return Math.pow((1.0 - lambda), order) * Math.pow(lambda, order + 1);
    }

    @Override
    public double computeErrorRate(Mutation mutation) {
        int code = ((Substitution) mutation).getCode(),
                pos = Mutations.getPosition(code),
                from = Mutations.getFrom(code), to = Mutations.getTo(code);

        int coverage = mutationsTable.getMigCoverage(pos),
                minorCount = mutationsTable.getMinorMigCount(pos, to);

        // Use global error rate if not enough coverage / statistics
        double minorRate = coverage < coverageThreshold || minorCount < minorCountThreshold ?
                substitutionErrorMatrix.getRate(from, to) : (minorCount / (double) coverage);

        // Correct for minor calling FDR
        minorRate *= (1.0 - minorCaller.computeFdr(from, to));

        // Compute per cycle error rate for a given substitution
        // based on probability that no error occurs from X cycles
        double errorRateBase = Math.pow(1.0 - minorRate, 1.0 / cycles);

        // Adjust for efficiency and probability of error propagation
        return (Math.log(lambda) - Math.log((1.0 + lambda) * errorRateBase - 1)) * propagateProb;
    }
}
