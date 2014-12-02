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
package com.milaboratory.migec2.core.correct;

import com.milaboratory.core.sequence.Range;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.migec2.core.align.reference.Reference;
import com.milaboratory.migec2.core.consalign.entity.AlignedConsensus;
import com.milaboratory.migec2.core.haplotype.Haplotype;
import com.milaboratory.migec2.core.mutations.MutationDifference;
import com.milaboratory.migec2.core.mutations.wrappers.MutationWrapperCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class CorrectedConsensus {
    private final AlignedConsensus alignedConsensus;
    private final List<MutationWrapperCollection> mutationsList;
    private final Set<Integer> coverageMask;
    private final int numberOfReferences;
    private final int migSize;
    private final double maxPValue;

    public CorrectedConsensus(AlignedConsensus alignedConsensus,
                              Set<Integer> coverageMask,
                              double maxPValue) {
        this.alignedConsensus = alignedConsensus;
        this.mutationsList = new ArrayList<>();
        this.coverageMask = coverageMask;
        this.numberOfReferences = alignedConsensus.getNumberOfReferences();

        for (int i = 0; i < numberOfReferences; i++) {
            mutationsList.add(new MutationWrapperCollection(alignedConsensus.getReference(i),
                    alignedConsensus.getMajorMutations(i).getMutationCodes()));
        }

        this.migSize = alignedConsensus.getMigSize();
        this.maxPValue = maxPValue;
    }

    public Haplotype generateHaplotype() {
        NucleotideSequence haplotypeSequence = getHaplotypeSequence();
        NucleotideSequence referenceSequence = getFullReferenceSequence();
        String maskedSequence = getMaskedSequence(haplotypeSequence);

        return new Haplotype(this, haplotypeSequence, referenceSequence, maskedSequence);
    }

    public static List<MutationDifference> getMutationDifferences(CorrectedConsensus parent, CorrectedConsensus child)
            throws Exception {
        if (parent.numberOfReferences != child.numberOfReferences)
            throw new Exception("Different number of references");

        List<MutationDifference> mutationDifferenceList = new ArrayList<>();

        for (int i = 0; i < parent.numberOfReferences; i++) {
            Reference parentReference = parent.alignedConsensus.getReference(i),
                    childReference = child.alignedConsensus.getReference(i);

            if (parentReference != childReference)
                throw new Exception("Different reference set of parent and child");

            mutationDifferenceList.add(new MutationDifference(parentReference,
                    parent.mutationsList.get(i).getMutationCodes(),
                    child.mutationsList.get(i).getMutationCodes()));
        }

        return mutationDifferenceList;
    }

    private NucleotideSequence getHaplotypeSequence() {
        NucleotideSequence seq = NucleotideSequence.EMPTY_NUCLEOTIDE_SEUQUENCE;
        for (int i = 0; i < numberOfReferences; i++) {
            Reference reference = alignedConsensus.getReference(i);
            Range range = alignedConsensus.getRange(i);
            int[] mutations = mutationsList.get(i).getMutationCodes();

            NucleotideSequence referenceSequence = reference.getSequence();

            if (!reference.isDeNovo()) {
                seq = seq.concatenate(Mutations.mutate(referenceSequence.getRange(range), mutations));
            } else
                seq = seq.concatenate(referenceSequence);
        }
        return seq;
    }

    private String getMaskedSequence(NucleotideSequence haplotypeSequence) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < haplotypeSequence.size(); i++)
            if (coverageMask.contains(i))
                sb.append('N');
            else
                sb.append(NucleotideAlphabet.INSTANCE.symbolFromCode(haplotypeSequence.codeAt(i)));
        return sb.toString();
    }

    private NucleotideSequence getFullReferenceSequence() {
        NucleotideSequence seq = NucleotideSequence.EMPTY_NUCLEOTIDE_SEUQUENCE;
        for (int i = 0; i < numberOfReferences; i++) {
            Reference reference = alignedConsensus.getReference(i);
            Range range = alignedConsensus.getRange(i);
            NucleotideSequence referenceSequence = reference.getSequence();

            seq = seq.concatenate(referenceSequence.getRange(range));
        }
        return seq;
    }

    public double getWorstPValue() {
        return maxPValue;
    }

    public int getNumberOfReferences() {
        return numberOfReferences;
    }

    public MutationWrapperCollection getMajorMutations(int referenceIndex) {
        return mutationsList.get(referenceIndex);
    }

    public Reference getReference(int referenceIndex) {
        return alignedConsensus.getReference(referenceIndex);
    }

    public int getMigSize() {
        return migSize;
    }

    @Override
    public String toString() {
        return getMaskedSequence(getHaplotypeSequence());
    }
}
