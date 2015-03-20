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
import com.milaboratory.oncomigec.core.genomic.Reference;

public class SAlignmentResult {
    private final LocalAlignment alignment;
    private final Reference reference;

    public SAlignmentResult(LocalAlignment alignment,
                            Reference reference) {
        this.alignment = alignment;
        this.reference = reference;
    }

    public Reference getReference() {
        return reference;
    }

    public Range getRange() {
        return alignment.getSequence1Range();
    }

    public LocalAlignment getAlignment() {
        return alignment;
    }

    public float calculateSimilarity() {
        return Mutations.similarity(reference.getSequence().getRange(alignment.getSequence1Range()),
                alignment.getMutations());
    }

    public float calculateSubstitutionRatio() {
        int[] mutations = alignment.getMutations();
        int substitutionCount = calculateSubstitutionCount(mutations);

        return substitutionCount / (float) alignment.getSequence1Range().length();
    }

    private int calculateSubstitutionCount(int[] mutations) {
        int substitutionCount = 0;
        for (int mutation : mutations)
            if (Mutations.isSubstitution(mutation))
                substitutionCount++;
        return substitutionCount;
    }

    public int calculateIdentity() {
        return alignment.getSequence1Range().length() - alignment.getMutations().length;
    }
}