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
import com.antigenomics.mageri.core.variant.VariantCallerParameters;
import com.milaboratory.core.sequence.mutations.Mutations;

public class MinorBasedErrorModel implements ErrorModel {
    private final int coverageThreshold, minorCountThreshold;
    private final double order, cycles, lambda, propagateProb;
    private final MutationsTable mutationsTable;
    private final SubstitutionErrorMatrix substitutionErrorMatrix;
    private final MinorCaller minorCaller;

    public MinorBasedErrorModel(VariantCallerParameters variantCallerParameters,
                                MutationsTable mutationsTable, MinorCaller minorCaller) {
        this(variantCallerParameters.getModelOrder(),
                variantCallerParameters.getModelCycles(),
                variantCallerParameters.getModelEfficiency(),
                variantCallerParameters.getCoverageThreshold(),
                variantCallerParameters.getModelMinorCountThreshold(),
                mutationsTable, minorCaller);
    }

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
        if (order == 0)
            return 1.0;

        double lambda = efficiency - 1;
        return Math.pow((1.0 - lambda), order) * Math.pow(lambda, order + 1);
    }

    public static double computeBaseErrorRateEstimate(double minorRate, double fdr,
                                                      double readFractionEstForCalledMinors) {
        return minorRate * (1.0 - fdr) * readFractionEstForCalledMinors;
    }

    @Override
    public ErrorRateEstimate computeErrorRate(Mutation mutation) {
        int code = ((Substitution) mutation).getCode();
        return computeErrorRate(Mutations.getPosition(code), Mutations.getFrom(code), Mutations.getTo(code));
    }

    @Override
    public ErrorRateEstimate computeErrorRate(int pos, int from, int to) {
        int coverage = mutationsTable.getMigCoverage(pos),
                minorCount = mutationsTable.getMinorMigCount(pos, to);

        double localMinorRate = minorCount / (double) coverage,
                globalMinorRate = substitutionErrorMatrix.getRate(from, to),
                readFractionForCalledMinors = minorCaller.getReadFractionForCalledMinors(from, to),
                filteredReadFraction = minorCaller.getFilteredReadFraction(from, to);

        // Use global error rate and minor read fraction if not enough coverage / statistics for a given position
        int useGlobal = coverage < coverageThreshold || minorCount < minorCountThreshold ? 1 : 0;
        if (globalMinorRate == 0) {
            globalMinorRate = minorCaller.getGlobalMinorRate(from, to);
            useGlobal = 2;
        }
        double minorRate = useGlobal > 0 ? globalMinorRate : localMinorRate;

        // Share of minors that are misidentified sequencing errors
        double fdr = minorCaller.computeFdr(from, to);

        double errorRateBase = computeBaseErrorRateEstimate(minorRate, fdr, readFractionForCalledMinors);

        // Share of minors to expect in the absence of sequencing errors due to sampling
        double recall = Math.exp(-errorRateBase * minorCaller.getGeometricMeanMigSize());

        // Adjust for probability of error propagation
        double majorErrorRate = errorRateBase / cycles / lambda * (1.0 + lambda) * propagateProb;

        return new ErrorRateEstimate(majorErrorRate,
                errorRateBase, minorCount, fdr, recall,
                globalMinorRate * coverage,
                readFractionForCalledMinors, filteredReadFraction,
                minorCaller.getGeometricMeanMigSize(),
                useGlobal);
    }
}
