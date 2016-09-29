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

import com.antigenomics.mageri.core.mapping.MutationsTable;
import com.antigenomics.mageri.core.mutations.Mutation;
import com.antigenomics.mageri.core.mutations.Substitution;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequenceBuilder;

public class MinorBasedErrorModel implements ErrorModel {
    public static final int COVERAGE_THRESHOLD = 100;

    private double order;
    private double cycles, lambda;
    private double propagateProb;

    public MinorBasedErrorModel() {
        this(1.0, 20, 0.95);
    }

    public MinorBasedErrorModel(double order, double cycles, double lambda) {
        this.order = order;
        this.cycles = cycles;
        this.lambda = lambda;
        calcPropagateProb();
    }

    public double getErrorRate(int minorCount, int total,
                               int from, int to,
                               SubstitutionErrorMatrix substitutionErrorMatrix) {
        minorCount = minorCount > 0 ? minorCount : 1;

        double rate = Math.max(total < COVERAGE_THRESHOLD ? 1.0 / (double) total : (minorCount / (double) total),
                substitutionErrorMatrix.getRate(from, to));

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

    @Override
    public double computeLog10Score(Mutation mutation, MutationsTable mutationsTable) {
        int code = ((Substitution) mutation).getCode(),
                pos = Mutations.getPosition(code),
                from = Mutations.getFrom(code), to = Mutations.getTo(code);

        int majorCount = mutationsTable.getMajorMigCount(pos, to);

        assert majorCount > 0;

        int coverage = mutationsTable.getMigCoverage(pos),
                minorCount = mutationsTable.getMinorMigCount(pos, to);

        double errorRate = getErrorRate(minorCount, coverage,
                from, to,
                minorMatrix),
                score = -10 * getLog10PValue(majorCount, coverage, errorRate);

        NucleotideSequenceBuilder nsb = new NucleotideSequenceBuilder(1);
        nsb.setCode(0, mutationsTable.getAncestralBase(pos));

        Variant variant = new Variant(reference,
                mutation, majorCount, minorCount,
                mutationsTable.getMigCoverage(pos),
                majorCount / (double) coverage,
                score, mutationsTable.getMeanCqs(pos, to), errorRate,
                nsb.create(), mutationsTable.hasReferenceBase(pos));

        variant.filter(this);

        variants.add(variant);
    }
}
