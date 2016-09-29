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
import com.milaboratory.core.sequence.mutations.Mutations;

public class MinorBasedErrorModel implements ErrorModel {
    public static final int COVERAGE_THRESHOLD = 100;

    private final double order, cycles, lambda, propagateProb;
    private final MutationsTable mutationsTable;
    private final SubstitutionErrorMatrix substitutionErrorMatrix;

    public MinorBasedErrorModel(double order, double cycles, double lambda,
                                MutationsTable mutationsTable) {
        this.order = order;
        this.cycles = cycles;
        this.lambda = lambda;
        this.propagateProb = computePropagateProb(lambda, order);
        this.mutationsTable = mutationsTable;
        this.substitutionErrorMatrix = SubstitutionErrorMatrix.fromMutationsTable(mutationsTable);
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

    public static double computePropagateProb(double lambda, double order) {
        return Math.pow((1.0 - lambda), order) * Math.pow(lambda, order + 1);
    }

    @Override
    public double computeErrorRate(Mutation mutation) {
        int code = ((Substitution) mutation).getCode(),
                pos = Mutations.getPosition(code),
                from = Mutations.getFrom(code), to = Mutations.getTo(code);

        int coverage = mutationsTable.getMigCoverage(pos),
                minorCount = mutationsTable.getMinorMigCount(pos, to);

        minorCount = minorCount > 0 ? minorCount : 1;

        double rate = Math.max(coverage < COVERAGE_THRESHOLD ? 1.0 / (double) coverage : (minorCount / (double) coverage),
                substitutionErrorMatrix.getRate(from, to));

        double errorRateBase = Math.pow(1.0 - rate, 1.0 / cycles);

        return (Math.log(lambda) - Math.log((1.0 + lambda) * errorRateBase - 1)) * propagateProb;
    }
}
