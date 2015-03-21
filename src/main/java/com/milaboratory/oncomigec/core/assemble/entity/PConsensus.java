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
package com.milaboratory.oncomigec.core.assemble.entity;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.PSequencingRead;
import com.milaboratory.core.sequencing.read.PSequencingReadImpl;

public final class PConsensus implements Consensus<PSequencingRead> {
    private final SConsensus consensus1, consensus2;

    public PConsensus(SConsensus consensus1, SConsensus consensus2) {
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
    public int fullSize() {
        return Math.min(consensus1.fullSize(), consensus2.fullSize());
    }

    @Override
    public int size() {
        return Math.min(consensus1.size(), consensus2.size());
    }

    @Override
    public NucleotideSequence getUmi() {
        return this.consensus1.getUmi();
    }

    @Override
    public PSequencingRead asRead() {
        return new PSequencingReadImpl(consensus1.asRead(),
                consensus2.asRead());
    }

    @Override
    public byte getMinQual() {
        return (byte) Math.min(consensus1.getMinQual(), consensus2.getMinQual());
    }

    @Override
    public byte getMaxQual() {
        return (byte) Math.max(consensus1.getMaxQual(), consensus2.getMaxQual());
    }

    @Override
    public byte getAvgQual() {
        return (byte) ((consensus1.getAvgQual() * consensus1.size() + consensus2.getAvgQual() * consensus2.size()) /
                (double) (consensus1.size() + consensus2.size()));
    }

    @Override
    public void empty() {
        consensus1.empty();
        consensus2.empty();
    }

    @Override
    public boolean isPairedEnd() {
        return true;
    }
}
