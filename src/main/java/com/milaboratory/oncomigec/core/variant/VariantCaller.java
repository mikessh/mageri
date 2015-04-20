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

import com.milaboratory.oncomigec.core.PipelineBlock;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.core.mapping.ConsensusAligner;
import com.milaboratory.oncomigec.core.mapping.ConsensusAlignerTable;
import com.milaboratory.oncomigec.core.variant.filter.CoverageFilter;
import com.milaboratory.oncomigec.core.variant.filter.QualFilter;
import com.milaboratory.oncomigec.core.variant.filter.SingletonFilter;
import com.milaboratory.oncomigec.core.variant.filter.VariantFilter;

import java.util.HashMap;
import java.util.Map;

public class VariantCaller extends PipelineBlock {
    protected final ReferenceLibrary referenceLibrary;
    protected final Map<Reference, VariantCallerTable> variantCallerTableByReference = new HashMap<>();
    protected final VariantFilter[] filters;

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

        ErrorModel errorModel = new ErrorModel(variantCallerParameters.getModelCycles(),
                variantCallerParameters.getModelEfficiency() - 1.0);

        for (Reference reference : referenceLibrary.getReferences()) {
            ConsensusAlignerTable consensusAlignerTable = consensusAligner.getAlignerTable(reference);
            if (consensusAlignerTable.wasUpdated()) {
                variantCallerTableByReference.put(reference,
                        new VariantCallerTable(this, consensusAlignerTable, errorModel));
            }
        }

        // This is quite important for memory usage
        // Of all objects, mig reader, consensus aligner and variant caller
        // consume most memory. Consensus aligner holds memory ~ number of references
        // Mig reader holds the entire read index, yet it gets immediately disposed
        // Variant caller data is needed to merge variant tables from different samples
        // Consensus aligner is the only thing we can and should get rid from here
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

    public VariantCallerTable getVariantCallerTable(Reference reference) {
        return variantCallerTableByReference.get(reference);
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
