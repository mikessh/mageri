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
package com.milaboratory.oncomigec.core.mapping;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.mapping.alignment.AlignmentResult;
import com.milaboratory.oncomigec.core.mutations.MutationArray;
import com.milaboratory.oncomigec.pipeline.analysis.Sample;

import java.util.Objects;

public final class PAlignedConsensus extends AlignedConsensus {
    private final MutationArray mutations1, mutations2;
    private final AlignmentResult alignmentResult1, alignmentResult2;
    private final NucleotideSQPair consensusSQPair1, consensusSQPair2;

    public PAlignedConsensus(Sample sample, NucleotideSequence umi,
                             NucleotideSQPair consensusSQPair1, NucleotideSQPair consensusSQPair2,
                             AlignmentResult alignmentResult1, AlignmentResult alignmentResult2,
                             MutationArray mutations1, MutationArray mutations2) {
        super(sample, umi);
        this.consensusSQPair1 = consensusSQPair1;
        this.consensusSQPair2 = consensusSQPair2;
        this.alignmentResult1 = alignmentResult1;
        this.alignmentResult2 = alignmentResult2;
        this.mutations1 = mutations1;
        this.mutations2 = mutations2;
    }

    public MutationArray getMutations1() {
        return mutations1;
    }

    public MutationArray getMutations2() {
        return mutations2;
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

    public boolean isMapped1() {
        return alignmentResult1 != null;
    }

    public boolean isMapped2() {
        return alignmentResult2 != null;
    }

    public boolean isAligned1() {
        return isMapped1() && alignmentResult1.isGood();
    }

    public boolean isAligned2() {
        return isMapped2() && alignmentResult2.isGood();
    }

    @Override
    public boolean isMapped() {
        return isMapped1() && isMapped2();
    }

    @Override
    public boolean isAligned() {
        return isAligned1() && isAligned2();
    }

    @Override
    public boolean isChimeric() {
        return isMapped() &&
                !(Objects.equals(alignmentResult1.getReference(), alignmentResult2.getReference()));
    }

    @Override
    public boolean isPairedEnd() {
        return true;
    }
}
