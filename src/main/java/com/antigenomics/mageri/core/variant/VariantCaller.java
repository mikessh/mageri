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

import com.antigenomics.mageri.core.PipelineBlock;
import com.antigenomics.mageri.core.assemble.DummyMinorCaller;
import com.antigenomics.mageri.core.assemble.MinorCaller;
import com.antigenomics.mageri.core.genomic.Reference;
import com.antigenomics.mageri.core.mapping.ConsensusAligner;
import com.antigenomics.mageri.core.mapping.MutationsTable;
import com.antigenomics.mageri.core.mutations.Substitution;
import com.antigenomics.mageri.core.output.VcfUtil;
import com.antigenomics.mageri.core.variant.filter.QualFilter;
import com.antigenomics.mageri.core.variant.filter.SingletonFilter;
import com.antigenomics.mageri.core.variant.filter.VariantFilter;
import com.antigenomics.mageri.core.variant.model.ErrorModel;
import com.antigenomics.mageri.core.variant.model.ErrorModelProvider;
import com.antigenomics.mageri.core.variant.model.ErrorModelType;
import com.antigenomics.mageri.core.variant.model.ErrorRateEstimate;
import com.antigenomics.mageri.misc.AuxiliaryStats;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequenceBuilder;
import com.antigenomics.mageri.core.genomic.ReferenceLibrary;
import com.antigenomics.mageri.core.mutations.Mutation;
import com.antigenomics.mageri.core.variant.filter.CoverageFilter;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.BinomialDistribution;
import org.apache.commons.math.distribution.BinomialDistributionImpl;

import java.util.*;

public class VariantCaller extends PipelineBlock {
    private final VariantCallerParameters variantCallerParameters;
    private final ReferenceLibrary referenceLibrary;
    private final VariantFilter[] filters;
    private final List<Variant> variants = new ArrayList<>();

    public VariantCaller(ConsensusAligner consensusAligner) {
        this(consensusAligner, new DummyMinorCaller(), VariantCallerParameters.DEFAULT);
    }

    public VariantCaller(ConsensusAligner consensusAligner, MinorCaller minorCaller) {
        this(consensusAligner, minorCaller, VariantCallerParameters.DEFAULT);
    }

    public VariantCaller(ConsensusAligner consensusAligner, MinorCaller minorCaller,
                         VariantCallerParameters variantCallerParameters) {
        super("variant.caller");
        this.variantCallerParameters = variantCallerParameters;
        this.referenceLibrary = consensusAligner.getReferenceLibrary();
        filters = new VariantFilter[3];
        filters[0] = new QualFilter(variantCallerParameters.getQualityThreshold());
        filters[1] = new SingletonFilter(variantCallerParameters.getSingletonFrequencyThreshold());
        filters[2] = new CoverageFilter(variantCallerParameters.getCoverageThreshold());

        int errorModelStatisticCount = ErrorModelProvider.getErrorModelStatisticNames(variantCallerParameters).length;

        for (Reference reference : referenceLibrary.getReferences()) {
            MutationsTable mutationsTable = consensusAligner.getAlignerTable(reference);
            if (mutationsTable.wasUpdated()) {
                ErrorModel errorModel = ErrorModelProvider.create(variantCallerParameters,
                        mutationsTable, minorCaller);

                Set<Integer> substitutionCodes = new HashSet<>();

                for (Mutation mutation : mutationsTable.getMutations()) {
                    Variant variant;

                    if (mutation instanceof Substitution) {
                        int code = ((Substitution) mutation).getCode(),
                                pos = Mutations.getPosition(code),
                                to = Mutations.getTo(code);

                        if (variantCallerParameters.showAbsentVariants()) {
                            substitutionCodes.add(code);
                        }

                        int majorCount = mutationsTable.getMajorMigCount(pos, to),
                                coverage = mutationsTable.getMigCoverage(pos);

                        ErrorRateEstimate errorRateEstimate = errorModel.computeErrorRate(mutation);
                        double score = getQScore(majorCount, coverage,
                                errorRateEstimate.getErrorRate(), variantCallerParameters);

                        NucleotideSequenceBuilder nsb = new NucleotideSequenceBuilder(1);
                        nsb.setCode(0, mutationsTable.getAncestralBase(pos));

                        variant = new Variant(reference,
                                mutation, majorCount,
                                mutationsTable.getMigCoverage(pos),
                                score, mutationsTable.getMeanCqs(pos, to),
                                nsb.create(), mutationsTable.hasReferenceBase(pos),
                                errorRateEstimate);
                    } else if (variantCallerParameters.isNoIndels()) {
                        continue;
                    } else {
                        int rawCount = mutationsTable.getRawMutationCount(mutation);
                        int pos = mutation.getStart();

                        variant = new Variant(reference,
                                mutation, rawCount,
                                mutationsTable.getMigCoverage(pos),
                                VcfUtil.UNDEF_QUAL, mutationsTable.getMeanCqs(pos),
                                new NucleotideSequence(""), true,
                                ErrorRateEstimate.createDummy(errorModelStatisticCount));
                    }

                    variant.filter(this);
                    variants.add(variant);
                }

                // Debug mode - provide error rates for absent positions
                if (variantCallerParameters.showAbsentVariants()) {
                    for (int pos = 0; pos < reference.size(); pos++) {
                        int from = reference.codeAt(pos);
                        for (int to = 0; to < 4; to++) {
                            if (to != from) {
                                int code = Mutations.createSubstitution(pos, from, to);

                                if (!substitutionCodes.contains(code)) {
                                    Mutation mutation = new Substitution(null, code);

                                    ErrorRateEstimate errorRateEstimate = errorModel.computeErrorRate(mutation);

                                    NucleotideSequenceBuilder nsb = new NucleotideSequenceBuilder(1);
                                    nsb.setCode(0, mutationsTable.getAncestralBase(pos));

                                    Variant variant = new Variant(reference,
                                            mutation, 0,
                                            mutationsTable.getMigCoverage(pos),
                                            VcfUtil.UNDEF_QUAL, mutationsTable.getMeanCqs(pos, to),
                                            nsb.create(), mutationsTable.hasReferenceBase(pos),
                                            errorRateEstimate);

                                    variants.add(variant);
                                }
                            }
                        }
                    }
                }
            }
        }

        // This is quite important for memory usage
        // Of all objects, mig reader, consensus consensusAligner and variant caller
        // consume most memory. Consensus consensusAligner holds memory ~ number of references
        // Mig reader holds the entire read index, yet it gets immediately disposed
        // Variant caller data is needed to merge variant tables from different samples
        // Consensus consensusAligner is the only thing we can and should get rid from here
        consensusAligner.clear();

        // Variants should be sorted for GATK compatibility
        Collections.sort(variants);
    }

    private static double getBinomialQScore(int majorCount, int total, double errorRate) {
        BinomialDistribution binomialDistribution = new BinomialDistributionImpl(total,
                errorRate);

        double score;
        try {
            score = -10 * Math.log10(1.0 - binomialDistribution.cumulativeProbability(majorCount) +
                    0.5 * binomialDistribution.probability(majorCount));
        } catch (MathException e) {
            e.printStackTrace();
            return VcfUtil.UNDEF_QUAL;
        }

        return score;
    }

    private static double getNegativeBinomialQScore(int majorCount, int total, double errorRate) {
        errorRate *= total;

        // Estimate the parameters of distribution of real error rate (lambda) based on errorRate estimate

        // Mean and variance of lambda from linear fitting
        double meanA = 5.02, meanB = 0.35,
                varA = 27.67, varB = 4.09;

        double mean = meanA + meanB * errorRate, var = varA + varB * errorRate;

        // Alpha and beta parameters of Gamma distribution
        double beta = mean / var, alpha = mean * beta;

        // Convert them to Negative Binomial distribution parameters
        double p = beta / (1.0 + beta);
        int r = (int) Math.round(alpha);

        if (r == 0) {
            return VcfUtil.UNDEF_QUAL;
        }

        return -10 * Math.log10(1.0 - AuxiliaryStats.negativeBinomialCdf(majorCount, r, p) +
                0.5 * AuxiliaryStats.negativeBinomialPdf(majorCount, r, p));
    }

    private static double getQScore(int majorCount, int total, double errorRate,
                                    VariantCallerParameters variantCallerParameters) {
        if (majorCount == 0) {
            return 0;
        }

        if (Double.isNaN(errorRate)) {
            return VcfUtil.UNDEF_QUAL;
        }

        double score = variantCallerParameters.getErrorModelType() == ErrorModelType.MinorBased ?
                getNegativeBinomialQScore(majorCount, total, errorRate) :
                getBinomialQScore(majorCount, total, errorRate);

        return Double.isInfinite(score) ? VcfUtil.MAX_QUAL : score;
    }

    public ReferenceLibrary getReferenceLibrary() {
        return referenceLibrary;
    }

    public int getFilterCount() {
        return filters.length;
    }

    public VariantFilter getFilter(int index) {
        return filters[index];
    }

    public List<Variant> getVariants() {
        return Collections.unmodifiableList(variants);
    }

    @Override
    public String getHeader() {
        return Variant.getHeaderBase() + ErrorModelProvider.getErrorModelHeader(variantCallerParameters);
    }

    public String[] getErrorModelStatisticIds() {
        return ErrorModelProvider.getErrorModelStatisticIDs(variantCallerParameters);
    }

    public String[] getErrorModelStatisticDescriptions() {
        return ErrorModelProvider.getErrorModelStatisticDescriptions(variantCallerParameters);
    }

    @Override
    public String getBody() {
        StringBuilder stringBuilder = new StringBuilder();

        for (Variant variant : variants) {
            stringBuilder.append(variant.toString()).append("\n");
        }

        return stringBuilder.toString();
    }
}
