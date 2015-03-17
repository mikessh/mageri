/*
 * Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)
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
package com.milaboratory.oncomigec.core.haplotype;

import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequencing.io.fasta.FastaWriter;
import com.milaboratory.oncomigec.core.PipelineBlock;
import com.milaboratory.oncomigec.core.correct.CorrectedConsensus;
import com.milaboratory.oncomigec.core.correct.CorrectorReferenceLibrary;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.mutations.MutationDifference;
import com.milaboratory.oncomigec.model.variant.VariantLibrary;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.BinomialDistribution;
import org.apache.commons.math.distribution.BinomialDistributionImpl;

import java.io.IOException;
import java.util.*;

public class HaplotypeAssembler extends PipelineBlock {
    private final HaplotypeAssemblerParameters parameters;
    private transient final Map<Reference, List<Haplotype>> haplotypesByReference = new HashMap<>();
    private final List<Haplotype> assembledHaplotypes = new LinkedList<>();
    private final CorrectorReferenceLibrary correctorReferenceLibrary;
    private final VariantLibrary variantLibrary;

    public HaplotypeAssembler(CorrectorReferenceLibrary correctorReferenceLibrary) {
        this(correctorReferenceLibrary, HaplotypeAssemblerParameters.DEFAULT);
    }

    public HaplotypeAssembler(CorrectorReferenceLibrary correctorReferenceLibrary,
                              HaplotypeAssemblerParameters parameters) {
        super("haplotypes");
        this.variantLibrary = correctorReferenceLibrary.getVariantLibrary();
        this.correctorReferenceLibrary = correctorReferenceLibrary;
        this.parameters = parameters;
    }

    public void add(CorrectedConsensus correctedConsensus) {
        Reference reference = correctedConsensus.getReference();

        Haplotype haplotype = new Haplotype(reference,
                correctedConsensus.getMutations(), correctedConsensus.getMigSize(),
                correctedConsensus.getWorstPValue(), correctedConsensus.getCoverageMask(),
                correctedConsensus.getRanges());

        List<Haplotype> haplotypeList = haplotypesByReference.get(reference);

        if (haplotypeList == null) {
            haplotypeList = new ArrayList<>();
            haplotypesByReference.put(reference, haplotypeList);
        }

        haplotypeList.add(haplotype);
    }

    public void assemble() throws MathException {
        for (List<Haplotype> haplotypeList : haplotypesByReference.values()) {
            // Sort, otherwise it's going to take eternity
            Collections.sort(haplotypeList, SpanComparator.INSTANCE);

            // Sliding merge
            Haplotype currentHaplotype = null;
            for (Haplotype haplotype : haplotypeList) {
                if (currentHaplotype != null) {
                    // NOTE: !embedded/touching and two-pass merge can be implemented for long inserts
                    if (new HaplotypeIntersection(currentHaplotype, haplotype, true).good()) {
                        // append and continue sliding
                        haplotype.merge(haplotype);
                    } else {
                        // failed to overlap with previous one
                        assembledHaplotypes.add(currentHaplotype);
                        // start sliding with me
                        currentHaplotype = haplotype;
                    }
                } else {
                    // start sliding with me
                    currentHaplotype = haplotype;
                }
            }

            // add last
            assembledHaplotypes.add(currentHaplotype);

            // Compute "correction missed" p-values
            for (Haplotype haplotype1 : assembledHaplotypes) {
                for (Haplotype haplotype2 : assembledHaplotypes) {
                    if (haplotype1 != haplotype2) {
                        if (haplotype1.getHaplotypeCounters().getCount() >
                                haplotype2.getHaplotypeCounters().getCount()) {
                            updatePValue(haplotype1, haplotype2);
                        } else {
                            updatePValue(haplotype2, haplotype1);
                        }
                    }
                }
            }
        }
    }

    private void updatePValue(Haplotype parentHaplotype, Haplotype childHaplotype) throws MathException {
        HaplotypeIntersection haplotypeIntersection = new HaplotypeIntersection(parentHaplotype, childHaplotype, false);
        Reference reference = parentHaplotype.getReference();

        if (!haplotypeIntersection.intersects() || haplotypeIntersection.embeddedOrTouching())
            return; // nothing to check

        HaplotypeCounters parentCounter = parentHaplotype.getHaplotypeCounters(),
                childCounter = childHaplotype.getHaplotypeCounters();

        assert !haplotypeIntersection.matches();

        double pValue = 1.0;
        int m = 0;

        for (MutationDifference mutationDifference : haplotypeIntersection.getMutationDifferences()) {
            // Take max P-value among mismatches
            for (int mutation : mutationDifference) {
                if (m++ >= parameters.getDepth()) {
                    return; // too much mismatches
                }

                int totalCountForMutation = totalCountForMutation(reference, mutation);

                // ONLY update p-value if child is masked by more than x times (default=twice) MIGs
                if (totalCountForMutation / (double) childCounter.getCount() >= parameters.getErrorMaskingTestMajorRatio()) {
                    double pi = calculatePValue(
                            reference,
                            mutation, parentCounter.getReadCount(), childCounter.getReadCount());
                    pValue *= pi;
                } else {
                    return;
                }
            }
        }

        childCounter.updatepValue(pValue); // Take max P-value among parents
    }

    private int totalCountForMutation(Reference reference, int mutation) {
        int totalCountForMutation = 0;
        int pos = Mutations.getPosition(mutation);

        switch (Mutations.getType(mutation)) {
            case Substitution:
                totalCountForMutation = correctorReferenceLibrary.
                        getMajorCount(reference, pos,
                                Mutations.getTo(mutation));
                break;
            case Insertion:
                totalCountForMutation = correctorReferenceLibrary.getMajorInsCount(reference, pos);

                break;
            case Deletion:
                totalCountForMutation = correctorReferenceLibrary.getMajorDelCount(reference, pos);
                break;
        }

        return totalCountForMutation;
    }

    private double calculatePValue(Reference reference, int mutation, int parentCount, int childCount) throws MathException {
        double p;
        int pos = Mutations.getPosition(mutation);
        if (Mutations.isSubstitution(mutation))
            p = variantLibrary.getBgFreqMig(reference,
                    (byte) Mutations.getFrom(mutation),
                    (byte) Mutations.getTo(mutation));
        else
            p = 1.0 / correctorReferenceLibrary.getMajorCount(reference, pos);

        BinomialDistribution binomialDistribution = new BinomialDistributionImpl(parentCount + childCount, p);

        return 1.0 - binomialDistribution.cumulativeProbability(childCount) + 0.5 *
                binomialDistribution.probability(childCount);
    }

    public List<Haplotype> getAssembledClonotypes() {
        return assembledHaplotypes;
    }

    public List<Haplotype> getFilteredHaplotypes() {
        return getFileredHaplotypes(parameters.getPValueThreshold());
    }

    public List<Haplotype> getFileredHaplotypes(double pValueThreshold) {
        List<Haplotype> filteredHaplotypes = new LinkedList<>();

        for (Haplotype haplotype : assembledHaplotypes) {
            HaplotypeCounters counters = haplotype.getHaplotypeCounters();
            if (counters.getpValue() <= pValueThreshold)
                filteredHaplotypes.add(haplotype);
        }

        return filteredHaplotypes;
    }

    @Override
    public String getHeader() {
        return "reference\tsequence\tmutations\tmig.count\tread.count\tworst.corrector.prob\tmissed.correction.pvalue";
    }

    @Override
    public String getBody() {
        StringBuilder sb = new StringBuilder();

        for (Haplotype haplotype : getAssembledClonotypes()) {
            sb.append(haplotype.getReference().getFullName());
            sb.append(haplotype.getMaskedSequence());

            HaplotypeCounters counters = haplotype.getHaplotypeCounters();
            sb.append("\t").append(haplotype.getMutationSignature()).
                    append("\t").append(counters.getCount()).
                    append("\t").append(counters.getReadCount()).
                    append("\t").append(1.0 - haplotype.getWorstPointPvalue()).
                    append("\t").append(counters.getpValue()).
                    append("\n");
        }

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writePlainText(String pathPrefix) throws IOException {
        super.writePlainText(pathPrefix);

        FastaWriter writer = new FastaWriter(pathPrefix + ".haplotypes.fa");

        int id = 0;

        for (Haplotype haplotype : assembledHaplotypes) {
            writer.write(haplotype.asFastaRecord(id++));
        }

        writer.close();
    }
}
