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

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.core.sequencing.read.SSequencingReadImpl;
import com.milaboratory.oncomigec.util.Util;

import java.util.Collections;
import java.util.List;

public final class SConsensus implements Consensus<SSequencingRead> {
    private transient final List<NucleotideSQPair> assembledReads, droppedReads;
    private final NucleotideSQPair consensusSQPair;
    private final NucleotideSequence umi;
    private final int assembledReadsCount, droppedReadsCount;

    public SConsensus(NucleotideSequence umi, NucleotideSQPair consensusSQPair,
                      List<NucleotideSQPair> assembledReads, List<NucleotideSQPair> droppedReads) {
        this.umi = umi;
        this.assembledReads = assembledReads;
        this.droppedReads = droppedReads;
        this.consensusSQPair = consensusSQPair;
        this.assembledReadsCount = assembledReads.size();
        this.droppedReadsCount = droppedReads.size();
    }

    public List<NucleotideSQPair> getAssembledReads() {
        return Collections.unmodifiableList(assembledReads);
    }

    public List<NucleotideSQPair> getDroppedReads() {
        return Collections.unmodifiableList(droppedReads);
    }

    public NucleotideSQPair getConsensusSQPair() {
        return consensusSQPair;
    }

    @Override
    public SSequencingRead asRead() {
        return new SSequencingReadImpl(
                "C:" + umi + ":" + assembledReadsCount + ":" + droppedReadsCount,
                consensusSQPair,
                -1);
    }

    @Override
    public byte getMinQual() {
        int minQ = Util.PH33_MAX_QUAL;
        for (int i = 0; i < consensusSQPair.size(); i++) {
            minQ = (byte) Math.min(consensusSQPair.getQuality().value(i), minQ);
        }
        return (byte) minQ;
    }

    @Override
    public byte getMaxQual() {
        int maxQ = Util.PH33_MIN_QUAL;
        for (int i = 0; i < consensusSQPair.size(); i++) {
            maxQ = (byte) Math.min(consensusSQPair.getQuality().value(i), maxQ);
        }
        return (byte) maxQ;
    }

    @Override
    public byte getAvgQual() {
        double avgQ = 0;
        for (int i = 0; i < consensusSQPair.size(); i++) {
            avgQ += consensusSQPair.getQuality().value(i);
        }
        return (byte) (avgQ / (double) consensusSQPair.size());
    }

    @Override
    public int fullSize() {
        return assembledReadsCount + droppedReadsCount;
    }

    @Override
    public int size() {
        return assembledReadsCount;
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
