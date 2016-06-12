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

package com.antigenomics.mageri.core.mapping.alignment;

import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.antigenomics.mageri.core.mapping.ConsensusAlignerParameters;

public class LocalAlignmentEvaluator implements AlignmentEvaluator<LocalAlignment> {
    private double minIdentityRatio, minAlignedQueryRelativeSpan;

    public LocalAlignmentEvaluator() {
        this(ConsensusAlignerParameters.DEFAULT);
    }

    public LocalAlignmentEvaluator(double minIdentityRatio,
                                   double minAlignedQueryRelativeSpan) {
        this.minIdentityRatio = minIdentityRatio;
        this.minAlignedQueryRelativeSpan = minAlignedQueryRelativeSpan;
    }

    public LocalAlignmentEvaluator(ConsensusAlignerParameters alignerParameters) {
        this(alignerParameters.getMinIdentityRatio(), alignerParameters.getMinAlignedQueryRelativeSpan());
    }

    @Override
    public boolean isGood(LocalAlignment alignment, NucleotideSequence reference, NucleotideSequence query) {
        int[] mutations = alignment.getMutations();

        int nSubstitutions = 0;
        for (int mutation : mutations) {
            if (Mutations.isSubstitution(mutation)) {
                nSubstitutions++;
            }
        }

        int alignedRefLength = alignment.getSequence1Range().length(),
                alignedQueryLength = alignment.getSequence2Range().length();
        double identity = (alignedRefLength - nSubstitutions) / (double) alignedRefLength,
                alignedQueryRelativeSpan = alignedQueryLength / (double) query.size(),
                alignedReferenceRelativeSpan = alignedRefLength / (double) reference.size();

        return identity >= minIdentityRatio &&
                Math.max(alignedReferenceRelativeSpan, alignedQueryRelativeSpan) >= minAlignedQueryRelativeSpan;
    }

    public double getMinIdentityRatio() {
        return minIdentityRatio;
    }

    public double getMinAlignedQueryRelativeSpan() {
        return minAlignedQueryRelativeSpan;
    }

    public void setMinIdentityRatio(double minIdentityRatio) {
        this.minIdentityRatio = minIdentityRatio;
    }

    public void setMinAlignedQueryRelativeSpan(double minAlignedQueryRelativeSpan) {
        this.minAlignedQueryRelativeSpan = minAlignedQueryRelativeSpan;
    }
}
