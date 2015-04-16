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
package com.milaboratory.oncomigec.core.assemble;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.core.sequencing.read.SSequencingReadImpl;

import java.util.Collections;
import java.util.Set;

public final class SConsensus implements Consensus<SSequencingRead> {
    private final NucleotideSQPair consensusSQPair;
    private final NucleotideSequence umi;
    private final int assembledSize, trueSize;
    private final Set<Integer> minors;

    public SConsensus(NucleotideSequence umi,
                      NucleotideSQPair consensusSQPair,
                      Set<Integer> minors,
                      int assembledSize, int trueSize) {
        this.umi = umi;
        this.consensusSQPair = consensusSQPair;
        this.minors = minors;
        this.assembledSize = assembledSize;
        this.trueSize = trueSize;
    }

    public Set<Integer> getMinors() {
        return Collections.unmodifiableSet(minors);
    }

    public NucleotideSQPair getConsensusSQPair() {
        return consensusSQPair;
    }

    @Override
    public SSequencingRead asRead() {
        return new SSequencingReadImpl(
                "C:" + umi + ":" + assembledSize + ":" + trueSize,
                consensusSQPair,
                -1);
    }

    public int getAssembledSize() {
        return assembledSize;
    }

    public int getTrueSize() {
        return trueSize;
    }

    @Override
    public NucleotideSequence getUmi() {
        return umi;
    }

    @Override
    public boolean isPairedEnd() {
        return false;
    }
}
