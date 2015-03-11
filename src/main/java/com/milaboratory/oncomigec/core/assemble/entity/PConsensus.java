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
import com.milaboratory.oncomigec.util.QualityHistogram;

public final class PConsensus implements Consensus {
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

    public static String formattedSequenceHeader() {
        return "Consensus1\tConsensus2\tQuality1\tQuality2";
    }

    @Override
    public String formattedSequence() {
        return new StringBuilder(consensus1.getConsensusSQPair().getSequence().toString()).
                append('\t').append(consensus2.getConsensusSQPair().getSequence().toString()).
                append('\t').append(consensus1.getConsensusSQPair().getQuality().toString()).
                append('\t').append(consensus2.getConsensusSQPair().getQuality().toString()).
                toString();
    }

    @Override
    public QualityHistogram getQualityHistogram() {
        QualityHistogram qualityHistogram = new QualityHistogram();
        qualityHistogram.append(consensus1.getConsensusSQPair().getQuality());
        qualityHistogram.append(consensus2.getConsensusSQPair().getQuality());
        return qualityHistogram;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("#Consensus1\n").append(consensus1.toString()).
                append("\n#Consensus2\n").append(consensus2.toString());

        return sb.toString();
    }
}
