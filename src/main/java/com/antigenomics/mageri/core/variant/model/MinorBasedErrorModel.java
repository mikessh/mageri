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
                                MutationsTable mutationsTable,
                                MinorCaller minorCaller) {
        this.order = order;
        this.cycles = cycles;
        this.lambda = efficiency - 1;
        this.propagateProb = order > 0 ? 1.0 : (1.0 - lambda) * lambda * lambda;
        this.mutationsTable = mutationsTable;
        this.minorCaller = minorCaller;
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

    public static double computeBaseErrorRateEstimate(double minorRate, double fdr,
                                                      double readFractionEstForCalledMinors,
                                                      double geomMeanMigSize) {
        minorRate *= (1.0 - fdr);
        return minorRate * readFractionEstForCalledMinors -
                (1.0 - minorRate) * Math.log(1.0 - minorRate) / geomMeanMigSize;
    }

    @Override
    public ErrorRateEstimate computeErrorRate(Mutation mutation) {
        int code = ((Substitution) mutation).getCode();
        return computeErrorRate(Mutations.getPosition(code), Mutations.getFrom(code), Mutations.getTo(code));
    }

    @Override
    public ErrorRateEstimate computeErrorRate(int pos, int from, int to) {
        return computeErrorRate(pos, from, to, false);
    }

    public ErrorRateEstimate computeErrorRate(int pos, int from, int to, boolean localOnly) {
        int coverage = mutationsTable.getMigCoverage(pos),
                minorCount = mutationsTable.getMinorMigCount(pos, to);

        double localMinorRate = minorCount / (double) coverage,
                globalMinorRate = minorCaller.getGlobalMinorRate(from, to),
                readFractionForCalledMinors = minorCaller.getReadFractionForCalledMinors(from, to),
                filteredReadFraction = minorCaller.getFilteredReadFraction(from, to);

        // Use global error rate and minor read fraction if not enough coverage / statistics for a given position
        int useGlobal = localOnly ? 0 : (coverage < coverageThreshold || minorCount < minorCountThreshold ? 1 : 0);
        double minorRate = useGlobal > 0 ? globalMinorRate : localMinorRate;

        // Share of minors that are misidentified sequencing errors
        double fdr = minorCaller.computeFdr(from, to);

        double errorRateBase = computeBaseErrorRateEstimate(minorRate, fdr,
                readFractionForCalledMinors, minorCaller.getGeometricMeanMigSize());

        // Share of minors to expect in the absence of sequencing errors due to sampling
        double recall = 1.0 - Math.exp(-errorRateBase * minorCaller.getGeometricMeanMigSize());

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