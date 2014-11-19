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

package com.milaboratory.migec2.core.align.processor.aligners;

import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;

public class LocalAlignmentEvaluator implements AlignmentEvaluator<LocalAlignment> {
    private final int maxConsequentMismatches;
    private final double minIdentity, minAlignedQueryRelativeSpan;

    public static LocalAlignmentEvaluator STRICT = new LocalAlignmentEvaluator(2, 0.9, 0.8);

    public LocalAlignmentEvaluator(int maxConsequentMismatches,
                                   double minIdentity,
                                   double minAlignedQueryRelativeSpan) {
        this.maxConsequentMismatches = maxConsequentMismatches;
        this.minIdentity = minIdentity;
        this.minAlignedQueryRelativeSpan = minAlignedQueryRelativeSpan;
    }

    @Override
    public boolean isGood(LocalAlignment alignment, NucleotideSequence query) {
        int[] mutations = alignment.getMutations();

        int alignedRefLength = alignment.getSequence1Range().length(),
                alignedQueryLength = alignment.getSequence2Range().length();
        double identity = (alignedRefLength - mutations.length) / (double) alignedRefLength,
                alignedQueryRelativeSpan = alignedQueryLength / (double) query.size();

        if (identity < minIdentity || alignedQueryRelativeSpan < minAlignedQueryRelativeSpan)
            return false;

        int consequentMutations = 1, previousSubstitutionPos = -2;
        for (int i = 0; i < mutations.length; i++) {
            int code = mutations[i], pos = Mutations.getPosition(code);
            if (Mutations.isSubstitution(code)) {
                if (pos - 1 == previousSubstitutionPos)
                    consequentMutations++;
                else
                    consequentMutations = 1;
                previousSubstitutionPos = pos;
            } else {
                consequentMutations = 1;
            }

            if (consequentMutations > maxConsequentMismatches)
                return false;
        }

        return true;
    }
}
