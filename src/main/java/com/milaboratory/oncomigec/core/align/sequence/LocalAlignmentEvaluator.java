/**
 * Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)
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
 */

package com.milaboratory.oncomigec.core.align.sequence;

import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;

public class LocalAlignmentEvaluator implements AlignmentEvaluator<LocalAlignment> {
    private int maxConsequentMismatches;
    private double minIdentityRatio, minAlignedQueryRelativeSpan;

    public LocalAlignmentEvaluator() {
        this(5, 0.7, 0.3);
    }

    public LocalAlignmentEvaluator(int maxConsequentMismatches,
                                   double minIdentityRatio,
                                   double minAlignedQueryRelativeSpan) {
        this.maxConsequentMismatches = maxConsequentMismatches;
        this.minIdentityRatio = minIdentityRatio;
        this.minAlignedQueryRelativeSpan = minAlignedQueryRelativeSpan;
    }

    @Override
    public boolean isGood(LocalAlignment alignment, NucleotideSequence reference, NucleotideSequence query) {
        int[] mutations = alignment.getMutations();

        int alignedRefLength = alignment.getSequence1Range().length(),
                alignedQueryLength = alignment.getSequence2Range().length();
        double identity = (alignedRefLength - mutations.length) / (double) alignedRefLength,
                alignedQueryRelativeSpan = alignedQueryLength / (double) query.size();

        if (identity < minIdentityRatio || alignedQueryRelativeSpan < minAlignedQueryRelativeSpan) {
            return false;
        }

        int consequentMutations = 1, previousSubstitutionPos = -2;
        for (int mutation : mutations) {
            int pos = Mutations.getPosition(mutation);
            if (Mutations.isSubstitution(mutation)) {
                if (pos - 1 == previousSubstitutionPos)
                    consequentMutations++;
                else
                    consequentMutations = 1;
                previousSubstitutionPos = pos;
            } else {
                consequentMutations = 1;
            }

            if (consequentMutations > maxConsequentMismatches) {
                return false;
            }
        }

        return true;
    }

    public int getMaxConsequentMismatches() {
        return maxConsequentMismatches;
    }

    public double getMinIdentityRatio() {
        return minIdentityRatio;
    }

    public double getMinAlignedQueryRelativeSpan() {
        return minAlignedQueryRelativeSpan;
    }

    public void setMaxConsequentMismatches(int maxConsequentMismatches) {
        this.maxConsequentMismatches = maxConsequentMismatches;
    }

    public void setMinIdentityRatio(double minIdentityRatio) {
        this.minIdentityRatio = minIdentityRatio;
    }

    public void setMinAlignedQueryRelativeSpan(double minAlignedQueryRelativeSpan) {
        this.minAlignedQueryRelativeSpan = minAlignedQueryRelativeSpan;
    }
}
