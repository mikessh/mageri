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
package com.milaboratory.oncomigec.core.align;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.align.sequence.AlignmentResult;
import com.milaboratory.oncomigec.core.mutations.MutationArray;

import java.io.Serializable;

public final class AlignedConsensus implements Serializable {
    private final MutationArray majorMutations1, majorMutations2;
    private final AlignmentResult alignmentResult1, alignmentResult2;
    private final NucleotideSQPair consensusSQPair1, consensusSQPair2;
    private final boolean pairedEnd;
    private final NucleotideSequence umi;

    public AlignedConsensus(NucleotideSequence umi,
                            MutationArray mutations1, MutationArray mutations2,
                            AlignmentResult alignmentResult1, AlignmentResult alignmentResult2,
                            NucleotideSQPair consensusSQPair1, NucleotideSQPair consensusSQPair2) {
        this.pairedEnd = true;
        this.umi = umi;
        this.majorMutations1 = mutations1;
        this.majorMutations2 = mutations2;
        this.alignmentResult1 = alignmentResult1;
        this.alignmentResult2 = alignmentResult2;
        this.consensusSQPair1 = consensusSQPair1;
        this.consensusSQPair2 = consensusSQPair2;
    }

    public AlignedConsensus(NucleotideSequence umi,
                            MutationArray mutations,
                            AlignmentResult alignmentResult,
                            NucleotideSQPair consensus) {
        this.pairedEnd = false;
        this.umi = umi;
        this.majorMutations1 = mutations;
        this.majorMutations2 = null;
        this.alignmentResult1 = alignmentResult;
        this.alignmentResult2 = null;
        this.consensusSQPair1 = consensus;
        this.consensusSQPair2 = null;
    }

    public boolean isPairedEnd() {
        return pairedEnd;
    }

    public boolean firstMateAligned() {
        return alignmentResult1 != null;
    }

    public boolean secondMateAligned() {
        return alignmentResult2 != null;
    }

    public boolean allAligned() {
        return firstMateAligned() && (!pairedEnd || secondMateAligned());
    }

    public boolean chimeric() {
        return pairedEnd && allAligned() &&
                !alignmentResult1.getReference().equals(alignmentResult2.getReference());
    }

    public MutationArray getMajorMutations1() {
        return majorMutations1;
    }

    public MutationArray getMajorMutations2() {
        return majorMutations2;
    }

    public AlignmentResult getAlignmentResult1() {
        return alignmentResult1;
    }

    public AlignmentResult getAlignmentResult2() {
        return alignmentResult2;
    }

    public NucleotideSQPair getConsensusSQPair1() {
        return consensusSQPair1;
    }

    public NucleotideSQPair getConsensusSQPair2() {
        return consensusSQPair2;
    }

    public NucleotideSequence getUmi() {
        return umi;
    }
}
