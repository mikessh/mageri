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
 * Last modified on 12.4.2015 by mikesh
 */

package com.milaboratory.oncomigec.core.variant;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.mutations.Mutation;
import com.milaboratory.oncomigec.core.variant.filter.FilterSummary;

public class Variant {
    private final Reference reference;
    private final Mutation mutation;
    private final int count, depth;
    private final double alleleFrequency, qual;
    private final NucleotideSequence ancestralAllele;
    private final boolean hasReference;
    private FilterSummary filterSummary = FilterSummary.DUMMY;

    public Variant(Reference reference, Mutation mutation,
                   int count, int depth, double alleleFrequency, double qual,
                   NucleotideSequence ancestralAllele, boolean hasReference) {
        this.reference = reference;
        this.mutation = mutation;
        this.count = count;
        this.depth = depth;
        this.alleleFrequency = alleleFrequency;
        this.qual = qual;
        this.ancestralAllele = ancestralAllele;
        this.hasReference = hasReference;
    }

    public Reference getReference() {
        return reference;
    }

    public Mutation getMutation() {
        return mutation;
    }

    public int getDepth() {
        return depth;
    }

    public int getCount() {
        return count;
    }

    public double getAlleleFrequency() {
        return alleleFrequency;
    }

    public double getQual() {
        return qual;
    }

    public NucleotideSequence getAncestralAllele() {
        return ancestralAllele;
    }

    public boolean hasReference() {
        return hasReference;
    }

    public FilterSummary getFilterSummary() {
        return filterSummary;
    }

    public void filter(VariantCaller variantCaller) {
        this.filterSummary = new FilterSummary(variantCaller, this);
    }

    @Override
    public String toString() {
        return mutation.toString() + "\t" + getCount() + "\t" + getQual();
    }
}
