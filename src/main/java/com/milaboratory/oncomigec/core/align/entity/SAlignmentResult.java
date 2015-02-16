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
package com.milaboratory.oncomigec.core.align.entity;

import com.milaboratory.core.sequence.Range;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import com.milaboratory.oncomigec.core.align.reference.Reference;

import java.util.ArrayList;
import java.util.List;

public class SAlignmentResult {
    private final List<LocalAlignment> alignments;
    private final List<Reference> references;
    private final List<Range> ranges; // to reconstruct

    public SAlignmentResult(List<LocalAlignment> alignments,
                            List<Reference> references, List<Range> ranges) {
        this.alignments = alignments;
        this.references = references;
        this.ranges = ranges;
    }

    public List<Reference> getReferences() {
        return references;
    }

    public List<Range> getRanges() {
        return ranges;
    }

    public List<LocalAlignment> getAlignments() {
        return alignments;
    }

    public List<Float> calculateSimilarities() {
        List<Float> similarityList = new ArrayList<>();
        for (int i = 0; i < references.size(); i++) {
            Reference reference = references.get(i);
            LocalAlignment alignment = alignments.get(i);
            float similarity = Mutations.similarity(reference.getSequence().getRange(alignment.getSequence1Range()),
                    alignment.getMutations());
            similarityList.add(similarity);
        }
        return similarityList;
    }

    public List<Float> calculateSubstitutionRatios() {
        List<Float> identityRatiosList = new ArrayList<>();
        for (int i = 0; i < references.size(); i++) {
            LocalAlignment alignment = alignments.get(i);
            int[] mutations = alignment.getMutations();
            int substitutionCount = calculateSubstitutionCount(mutations);
            identityRatiosList.add(substitutionCount / (float) alignment.getSequence1Range().length());
        }
        return identityRatiosList;
    }

    private int calculateSubstitutionCount(int[] mutations) {
        int substitutionCount = 0;
        for (int i = 0; i < mutations.length; i++)
            if (Mutations.isSubstitution(mutations[i]))
                substitutionCount++;
        return substitutionCount;
    }

    public List<Integer> calculateIdentities() {
        List<Integer> identityList = new ArrayList<>();
        for (int i = 0; i < references.size(); i++) {
            LocalAlignment alignment = alignments.get(i);
            int identity = alignment.getSequence1Range().length() - alignment.getMutations().length;
            identityList.add(identity);
        }
        return identityList;
    }

    @Override
    public String toString() {
        String alignmentString = "";
        for (int i = 0; i < references.size(); i++) {
            Reference reference = references.get(i);
            LocalAlignment alignment = alignments.get(i);
            alignmentString += "@" + references.get(i).getFullName() + "\t" +
                    Mutations.toString(NucleotideAlphabet.INSTANCE, alignment.getMutations()) + "\n" +
                    Mutations.printAlignmentToString(reference.getSequence().getRange(alignment.getSequence1Range()),
                            alignment.getMutations());
        }
        return alignmentString;
    }
}