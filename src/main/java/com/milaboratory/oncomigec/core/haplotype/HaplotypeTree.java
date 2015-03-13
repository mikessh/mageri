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
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.PipelineBlock;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.correct.CorrectedConsensus;
import com.milaboratory.oncomigec.core.correct.CorrectorReferenceLibrary;
import com.milaboratory.oncomigec.core.mutations.MutationDifference;
import com.milaboratory.oncomigec.model.variant.VariantLibrary;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.BinomialDistribution;
import org.apache.commons.math.distribution.BinomialDistributionImpl;

import java.util.*;

public class HaplotypeTree implements PipelineBlock {
    private final HaplotypeTreeParameters parameters;
    private final Map<NucleotideSequence, Map<Haplotype, HaplotypeCounters>> haplotypesByReference = new HashMap<>();
    private final CorrectorReferenceLibrary correctorReferenceLibrary;
    private final VariantLibrary variantLibrary;

    public HaplotypeTree(CorrectorReferenceLibrary correctorReferenceLibrary) {
        this(correctorReferenceLibrary, HaplotypeTreeParameters.DEFAULT);
    }

    public HaplotypeTree(CorrectorReferenceLibrary correctorReferenceLibrary,
                         HaplotypeTreeParameters parameters) {
        this.variantLibrary = correctorReferenceLibrary.getVariantLibrary();
        this.correctorReferenceLibrary = correctorReferenceLibrary;
        this.parameters = parameters;
    }

    public void add(CorrectedConsensus correctedConsensus) {
        add(correctedConsensus.generateHaplotype(), correctedConsensus.getMigSize());
    }

    private void add(Haplotype haplotype, int migSize) {
        NucleotideSequence referenceSequence = haplotype.getReferenceSequence();

        Map<Haplotype, HaplotypeCounters> haplotypeDataCountMap =
                haplotypesByReference.get(referenceSequence);

        if (haplotypeDataCountMap == null)
            haplotypesByReference.put(referenceSequence, haplotypeDataCountMap = new HashMap<>());

        HaplotypeCounters count = haplotypeDataCountMap.get(haplotype);

        if (count == null)
            haplotypeDataCountMap.put(haplotype, count = new HaplotypeCounters());

        count.incrementCount();
        count.incrementReadCount(migSize);
    }


    private void updatePValue(Haplotype parentHaplotype, HaplotypeCounters parentCounter,
                              Haplotype childHaplotype, HaplotypeCounters childCounter) throws Exception {
        List<MutationDifference> mutationDifferencesList = Haplotype.getMutationDifferences(parentHaplotype,
                childHaplotype);

        if (mutationDifferencesList.size() == 0)
            throw new Exception("Parent and child should be different haplotypes");

        double pValue = 1.0;
        int m = 0;

        for (int i = 0; i < mutationDifferencesList.size(); i++) {
            MutationDifference mutationDifferences = mutationDifferencesList.get(i);
            Reference reference = mutationDifferences.getReference();

            for (int mutation : mutationDifferences) {

                // Take max P-value among references
                int totalCountForMutation = totalCountForMutation(reference, mutation);

                // ONLY update p-value if child is masked by more than x times (default=twice) MIGs
                if (totalCountForMutation / (double) childCounter.getCount() >= parameters.getErrorMaskingTestMajorRatio()) {
                    double pi = calculatePValue(
                            reference,
                            mutation, parentCounter.getReadCount(), childCounter.getReadCount());
                    pValue *= pi;
                } else {
                    pValue = 0;
                    break;
                }
            }

            if (++m > parameters.getDepth()) {
                pValue = 0;
                break;
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

    public void calculatePValues() throws Exception {
        for (Map<Haplotype, HaplotypeCounters> haplotypeEntries : haplotypesByReference.values()) {
            for (Map.Entry<Haplotype, HaplotypeCounters> child : haplotypeEntries.entrySet()) {
                for (Map.Entry<Haplotype, HaplotypeCounters> parent : haplotypeEntries.entrySet()) {
                    if (parent.getKey() != child.getKey() &&
                            parent.getValue().getCount() >= child.getValue().getCount())
                        updatePValue(parent.getKey(), parent.getValue(), child.getKey(), child.getValue());
                }
            }
        }
    }

    public Set<Haplotype> getHaplotypes() {
        return getHaplotypes(parameters.getPValueThreshold());
    }

    public Set<Haplotype> getHaplotypes(double pValueThreshold) {
        Set<Haplotype> filteredHaplotypes = new HashSet<>();
        for (Map<Haplotype, HaplotypeCounters> haplotypeCountersMap : haplotypesByReference.values()) {
            for (Map.Entry<Haplotype, HaplotypeCounters> entry : haplotypeCountersMap.entrySet()) {
                HaplotypeCounters counters = entry.getValue();
                if (counters.getpValue() <= pValueThreshold)
                    filteredHaplotypes.add(entry.getKey());
            }
        }
        return filteredHaplotypes;
    }

    private HaplotypeCounters getCounters(Haplotype haplotype) {
        return haplotypesByReference.get(haplotype.getReferenceSequence()).get(haplotype);
    }

    public String toFastaString() {
        StringBuilder sb = new StringBuilder();

        int i = 0;
        final char HEADER_SEP = '|';
        for (Haplotype haplotype : getHaplotypes()) {
            HaplotypeCounters counters = getCounters(haplotype);

            sb.append(">Haplotype").append(++i).
                    append(HEADER_SEP).append("REF=").
                    append(haplotype.getReferencesSignature()).
                    append(HEADER_SEP).append("MUT=").
                    append(haplotype.getMutationsSignature()).
                    append(HEADER_SEP).append("MIG_COUNT=").
                    append(counters.getCount()).
                    append(HEADER_SEP).append("READ_COUNT=").
                    append(counters.getReadCount()).
                    append(HEADER_SEP).append("P=").
                    append(counters.getpValue()).
                    append('\n').
                    append(haplotype.getMaskedSequence()).
                    append('\n');
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("#").append(super.toString()).
                append("\nSequence\tReferences\tMutations\tMigCount\tReadCount\tWorstPointPvalue\tHaplotypePValue\n");

        for (Haplotype haplotype : getHaplotypes()) {
            HaplotypeCounters counters = getCounters(haplotype);

            sb.append(haplotype.getMaskedSequence());

            sb.append("\t").
                    append(haplotype.getReferencesSignature()).
                    append("\t");

            sb.append(haplotype.getMutationsSignature()).
                    append("\t").
                    append(counters.getCount()).
                    append("\t").
                    append(counters.getReadCount()).
                    append("\t").
                    append(haplotype.getWorstPointPvalue()).
                    append("\t").
                    append(counters.getpValue()).
                    append("\n");
        }

        return sb.toString();
    }
}
