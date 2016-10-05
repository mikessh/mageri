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

import com.antigenomics.mageri.core.mutations.Substitution;
import com.antigenomics.mageri.core.variant.model.ErrorModel;
import com.antigenomics.mageri.core.variant.model.ErrorRateEstimate;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.antigenomics.mageri.core.genomic.Contig;
import com.antigenomics.mageri.core.genomic.Reference;
import com.antigenomics.mageri.core.mutations.Mutation;
import com.antigenomics.mageri.core.variant.filter.FilterSummary;

import java.io.Serializable;

public class Variant implements Serializable, Comparable<Variant> {
    private final Reference reference;
    private final Mutation mutation;
    private final int count, depth;
    private final double alleleFrequency, qual, cqs;
    private final NucleotideSequence ancestralAllele;
    private final boolean hasReference;
    private final ErrorRateEstimate errorRateEstimate;
    private FilterSummary filterSummary = FilterSummary.DUMMY;

    public Variant(Reference reference, Mutation mutation,
                   int count, int depth,
                   double qual, double cqs,
                   NucleotideSequence ancestralAllele, boolean hasReference,
                   ErrorRateEstimate errorRateEstimate) {
        this.reference = reference;
        this.mutation = mutation;
        this.count = count;
        this.depth = depth;
        this.alleleFrequency = count / (double) depth;
        this.qual = qual;
        this.cqs = cqs;
        this.errorRateEstimate = errorRateEstimate;
        this.ancestralAllele = ancestralAllele;
        this.hasReference = hasReference;
    }

    public String getRef() {
        if (mutation instanceof Substitution) {
            return mutation.getRef().toString();
        } else {
            return reference.charFromCodeAt(mutation.getStart()) + mutation.getRef().toString();
        }
    }

    public String getAlt() {
        if (mutation instanceof Substitution) {
            return mutation.getAlt().toString();
        } else {
            return reference.charFromCodeAt(mutation.getStart()) + mutation.getAlt().toString();
        }
    }

    public Reference getReference() {
        return reference;
    }

    public String getChrom() {
        return reference.getGenomicInfo().getChrom();
    }

    public int getGenomicPosition() {
        return reference.getGenomicInfo().getStart() + mutation.getStart() + 1;
    }

    private Contig getContig() {
        return reference.getGenomicInfo().getContig();
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

    public ErrorRateEstimate getErrorRateEstimate() {
        return errorRateEstimate;
    }

    public double getQual() {
        return qual;
    }

    public double getCqs() {
        return cqs;
    }

    public String getAncestralAllele() {
        return ancestralAllele.size() == 0 ? "." : ancestralAllele.toString();
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

    public static String getHeaderBase() {
        return "reference\tmutation\t" +
                "count\tcoverage\tfreq\t" +
                "score\tcqs\t" +
                "has.reference\tancestral.allele";
    }

    public String getGenomicString() {
        return getChrom() + ":" + getGenomicPosition() + ":" + mutation.getType() +
                ":" + getRef() + ">" + getAlt();
    }

    @Override
    public String toString() {
        return reference.getName() + "\t" + mutation.toString() + "\t" +
                count + "\t" + depth + "\t" + (float) alleleFrequency + "\t" +
                (float) qual + "\t" + (float) cqs + "\t" +
                (hasReference ? "TRUE" : "FALSE") + "\t" + ancestralAllele.toString() +
                errorRateEstimate.getErrorRateEstimateRowPart();
    }

    @Override
    public int compareTo(Variant o) {
        int result = getContig().compareTo(o.getContig());

        if (result == 0) {
            return Integer.compare(getGenomicPosition(), o.getGenomicPosition());
        }

        return result;
    }
}
