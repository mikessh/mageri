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

import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequenceBuilder;
import com.milaboratory.oncomigec.core.PipelineBlock;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.core.mapping.ConsensusAligner;
import com.milaboratory.oncomigec.core.mapping.MutationsTable;
import com.milaboratory.oncomigec.core.mutations.Mutation;
import com.milaboratory.oncomigec.core.mutations.Substitution;
import com.milaboratory.oncomigec.core.variant.filter.CoverageFilter;
import com.milaboratory.oncomigec.core.variant.filter.QualFilter;
import com.milaboratory.oncomigec.core.variant.filter.SingletonFilter;
import com.milaboratory.oncomigec.core.variant.filter.VariantFilter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class VariantCaller extends PipelineBlock {
    protected final ReferenceLibrary referenceLibrary;
    protected final VariantFilter[] filters;
    protected final List<Variant> variants = new LinkedList<>();

    public VariantCaller(ConsensusAligner consensusAligner) {
        this(consensusAligner, VariantCallerParameters.DEFAULT);
    }

    public VariantCaller(ConsensusAligner consensusAligner,
                         VariantCallerParameters variantCallerParameters) {
        super("variant.caller");
        this.referenceLibrary = consensusAligner.getReferenceLibrary();
        filters = new VariantFilter[3];
        filters[0] = new QualFilter(variantCallerParameters.getQualityThreshold());
        filters[1] = new SingletonFilter(variantCallerParameters.getSingletonFrequencyThreshold());
        filters[2] = new CoverageFilter(variantCallerParameters.getCoverageThreshold());

        ErrorModel errorModel = new ErrorModel(
                variantCallerParameters.getOrder(),
                variantCallerParameters.getModelCycles(),
                variantCallerParameters.getModelEfficiency() - 1.0);

        for (Reference reference : referenceLibrary.getReferences()) {
            MutationsTable mutationsTable = consensusAligner.getAlignerTable(reference);
            if (mutationsTable.wasUpdated()) {
                for (Mutation mutation : mutationsTable.getMutations()) {
                    if (mutation instanceof Substitution) {
                        int code = ((Substitution) mutation).getCode(),
                                pos = Mutations.getPosition(code),
                                base = Mutations.getTo(code);

                        int majorCount = mutationsTable.getMajorMigCount(pos, base);

                        assert majorCount > 0;

                        int coverage = mutationsTable.getMigCoverage(pos);

                        double score = errorModel.getLog10PValue(
                                majorCount,
                                mutationsTable.getMinorMigCount(pos, base),
                                coverage);

                        NucleotideSequenceBuilder nsb = new NucleotideSequenceBuilder(1);
                        nsb.setCode(0, mutationsTable.getAncestralBase(pos));

                        Variant variant = new Variant(reference,
                                mutation, majorCount,
                                mutationsTable.getMigCoverage(pos),
                                majorCount / (double) coverage, score,
                                nsb.create(), mutationsTable.hasReferenceBase(pos));

                        variant.filter(this);

                        variants.add(variant);
                    } else {
                        // TODO: IMPORTANT: INDELS
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
        return null;
    }

    @Override
    public String getBody() {
        return null;
    }
}
