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

import com.antigenomics.mageri.core.mapping.ConsensusAlignerParameters;
import com.milaboratory.core.sequence.alignment.AffineGapAlignmentScoring;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;

import static com.milaboratory.core.sequence.alignment.ScoringUtils.getSymmetricMatrix;

public class AlignmentScoring {
    private final int matchReward, mismatchPenalty, gapOpenPenalty, gapExtendPenalty;
    private final AffineGapAlignmentScoring internalScoring;

    public AlignmentScoring() {
        this(ConsensusAlignerParameters.DEFAULT);
    }

    public AlignmentScoring(ConsensusAlignerParameters alignerParameters) {
        this(alignerParameters.getMatchReward(), alignerParameters.getMismatchPenalty(),
                alignerParameters.getGapOpenPenalty(), alignerParameters.getGapExtendPenalty());
    }

    public AlignmentScoring(int matchReward, int mismatchPenalty,
                            int gapOpenPenalty, int gapExtendPenalty) {
        this.matchReward = matchReward;
        this.mismatchPenalty = mismatchPenalty;
        this.gapOpenPenalty = gapOpenPenalty;
        this.gapExtendPenalty = gapExtendPenalty;
        this.internalScoring = new AffineGapAlignmentScoring<>(NucleotideAlphabet.INSTANCE,
                getSymmetricMatrix(matchReward, mismatchPenalty, 4), gapOpenPenalty, gapExtendPenalty);
    }

    public AffineGapAlignmentScoring asInternalScoring() {
        return internalScoring;
    }

    public int getMatchReward() {
        return matchReward;
    }

    public int getMismatchPenalty() {
        return mismatchPenalty;
    }

    public int getGapOpenPenalty() {
        return gapOpenPenalty;
    }

    public int getGapExtendPenalty() {
        return gapExtendPenalty;
    }

    public AffineGapAlignmentScoring getInternalScoring() {
        return internalScoring;
    }
}
