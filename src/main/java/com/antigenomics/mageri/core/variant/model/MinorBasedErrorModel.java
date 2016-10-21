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
                                                      double readFractionEstForCalledMinors,
                                                      double lambda, double nCycles) {
        // minorRate = eps * sum_{n=1..nStar}(1+lambda)^n
        // Where eps - true error rate
        // lambda - efficiency
        // sum_{n=1..nStar}(1+lambda)^n - total number of molecules from cycles that can yield detectable minor
        //
        // nStar is computed as
        // 1 / (1+lambda) ^ nStar = minor read frequency est
        // minor read frequency est is
        // (total number of reads in minors) / (total number of reads in MIGs where minors were detected)
        //readFractionEstForCalledMinors = readFractionEstForCalledMinors > 0 ? readFractionEstForCalledMinors :
        //        Math.pow(1.0 + lambda, -nCycles);

        return minorRate * (1.0 - fdr) * readFractionEstForCalledMinors / nCycles;
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

        // Use global error rate and minor read fraction if not enough coverage / statistics for a given position
        double minorRate = coverage < coverageThreshold || minorCount < minorCountThreshold ?
                substitutionErrorMatrix.getRate(from, to) : minorCount / (double) coverage;

        double fdr = minorCaller.computeFdr(from, to); // share of minors that are actually misidentified seq errors

        double errorRateBase = computeBaseErrorRateEstimate(minorRate,
                fdr, minorCaller.getReadFractionForCalledMinors(from, to), lambda, cycles);

        // Expected share of minors that are not lost due to sampling
        double recall = minorRate / errorRateBase / lambda / cycles * (1.0 + lambda);

        // Adjust for probability of error propagation
        double firstCycleErrorRate = errorRateBase * propagateProb;

        return new ErrorRateEstimate(firstCycleErrorRate,
                errorRateBase, minorCount, fdr, recall);
    }
}
