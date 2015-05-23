/*
 * Copyright (c) 2014-2015, Bolotin Dmitry, Chudakov Dmitry, Shugay Mikhail
 * (here and after addressed as Inventors)
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact the Inventors using one of the following
 * email addresses: chudakovdm@mail.ru, chudakovdm@gmail.com
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */

package com.milaboratory.oncomigec.core.variant;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.genomic.Contig;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.mutations.Mutation;
import com.milaboratory.oncomigec.core.variant.filter.FilterSummary;

import java.io.Serializable;

public class Variant implements Serializable, Comparable<Variant> {
    private final Reference reference;
    private final Mutation mutation;
    private final int count, depth, minorCount;
    private final double alleleFrequency, qual, cqs;
    private final NucleotideSequence ancestralAllele;
    private final boolean hasReference;
    private FilterSummary filterSummary = FilterSummary.DUMMY;

    public Variant(Reference reference, Mutation mutation,
                   int count, int minorCount, int depth, double alleleFrequency,
                   double qual, double cqs,
                   NucleotideSequence ancestralAllele, boolean hasReference) {
        this.reference = reference;
        this.mutation = mutation;
        this.count = count;
        this.minorCount = minorCount;
        this.depth = depth;
        this.alleleFrequency = alleleFrequency;
        this.qual = qual;
        this.cqs = cqs;
        this.ancestralAllele = ancestralAllele;
        this.hasReference = hasReference;
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

    public int getMinorCount() {
        return minorCount;
    }

    public double getAlleleFrequency() {
        return alleleFrequency;
    }

    public double getQual() {
        return qual;
    }

    public double getCqs() {
        return cqs;
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

    public static String getHeader() {
        return "reference\tmutation\tcount.major\tcount.minor\tcoverage\tscore\tcqs\thas.reference\tancestral.allele";
    }

    @Override
    public String toString() {
        return reference.getName() + "\t" + mutation.toString() + "\t" +
                count + "\t" + minorCount + "\t" + depth + "\t" +
                qual + "\t" + cqs + "\t" +
                (hasReference ? "TRUE" : "FALSE") + "\t" + ancestralAllele.toString();
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
