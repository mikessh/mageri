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

        final ErrorModel errorModel = new ErrorModel(
                variantCallerParameters.getOrder(),
                variantCallerParameters.getModelCycles(),
                variantCallerParameters.getModelEfficiency() - 1.0);

        for (Reference reference : referenceLibrary.getReferences()) {
            final MutationsTable mutationsTable = consensusAligner.getAlignerTable(reference);
            final MinorMatrix minorMatrix = MinorMatrix.fromMutationsTable(mutationsTable);
            if (mutationsTable.wasUpdated()) {
                for (Mutation mutation : mutationsTable.getMutations()) {
                    if (mutation instanceof Substitution) {
                        int code = ((Substitution) mutation).getCode(),
                                pos = Mutations.getPosition(code),
                                from = Mutations.getFrom(code), to = Mutations.getTo(code);

                        int majorCount = mutationsTable.getMajorMigCount(pos, to);

                        assert majorCount > 0;

                        int coverage = mutationsTable.getMigCoverage(pos),
                                minorCount = mutationsTable.getMinorMigCount(pos, to);

                        double score = -10 * errorModel.getLog10PValue(majorCount, minorCount, coverage,
                                from, to,
                                minorMatrix);

                        NucleotideSequenceBuilder nsb = new NucleotideSequenceBuilder(1);
                        nsb.setCode(0, mutationsTable.getAncestralBase(pos));

                        Variant variant = new Variant(reference,
                                mutation, majorCount, minorCount,
                                mutationsTable.getMigCoverage(pos),
                                majorCount / (double) coverage,
                                score, mutationsTable.getMeanCqs(pos, to),
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

        // Variants should be sorted for GATK compatibility
        Collections.sort(variants);
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
        return Variant.getHeader();
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
