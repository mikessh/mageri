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
package com.antigenomics.mageri.core.assemble;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.core.sequencing.read.SSequencingReadImpl;
import com.antigenomics.mageri.pipeline.analysis.Sample;

import java.util.Collections;
import java.util.Set;

public final class SConsensus extends Consensus<SSequencingRead> {
    private final NucleotideSQPair consensusSQPair;
    private final int assembledSize, trueSize;
    private final Set<Integer> minors;

    public SConsensus(Sample sample,
                      NucleotideSequence umi,
                      NucleotideSQPair consensusSQPair,
                      Set<Integer> minors,
                      int assembledSize, int trueSize) {
        super(sample, umi);
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
                "C:" + sample.getFullName() + " " + umi + ":" + assembledSize + ":" + trueSize,
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
    public boolean isPairedEnd() {
        return false;
    }
    
    @Override
    public String toString() {
        return umi + "\t" + consensusSQPair.getSequence() + "\t" + consensusSQPair.getQuality();
    }
}
