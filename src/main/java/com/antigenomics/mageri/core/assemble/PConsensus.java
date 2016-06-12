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

import com.milaboratory.core.sequencing.read.PSequencingRead;
import com.milaboratory.core.sequencing.read.PSequencingReadImpl;

public final class PConsensus extends Consensus<PSequencingRead> {
    private final SConsensus consensus1, consensus2;

    public PConsensus(SConsensus consensus1, SConsensus consensus2) {
        super(consensus1.getSample(), consensus1.getUmi());
        this.consensus1 = consensus1;
        this.consensus2 = consensus2;
    }

    public SConsensus getConsensus1() {
        return consensus1;
    }

    public SConsensus getConsensus2() {
        return consensus2;
    }

    @Override
    public int getTrueSize() {
        return consensus1.getTrueSize();
    }

    @Override
    public int getAssembledSize() {
        return Math.min(consensus1.getAssembledSize(),
                consensus2.getAssembledSize());
    }

    @Override
    public PSequencingRead asRead() {
        return new PSequencingReadImpl(consensus1.asRead(),
                consensus2.asRead());
    }

    @Override
    public boolean isPairedEnd() {
        return true;
    }

    @Override
    public String toString() {
        return consensus1.toString() + "\t" +
                consensus2.getConsensusSQPair().getSequence() + "\t" + consensus2.getConsensusSQPair().getQuality();
    }
}
