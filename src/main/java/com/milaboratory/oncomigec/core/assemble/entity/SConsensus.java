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
import com.milaboratory.oncomigec.util.QualityHistogram;

import java.util.List;

public final class SConsensus implements Consensus {
    private final List<NucleotideSQPair> assembledReads, droppedReads;
    private final NucleotideSQPair consensusSQPair;
    private final NucleotideSequence umi;

    public SConsensus(NucleotideSequence umi, NucleotideSQPair consensusSQPair,
                      List<NucleotideSQPair> assembledReads, List<NucleotideSQPair> droppedReads) {
        this.umi = umi;
        this.assembledReads = assembledReads;
        this.droppedReads = droppedReads;
        this.consensusSQPair = consensusSQPair;
    }

    public List<NucleotideSQPair> getAssembledReads() {
        return assembledReads;
    }

    public List<NucleotideSQPair> getDroppedReads() {
        return droppedReads;
    }

    public NucleotideSQPair getConsensusSQPair() {
        return consensusSQPair;
    }


    public static String formattedSequenceHeader() {
        return "Consensus\tQuality";
    }

    @Override
    public int fullSize() {
        return assembledReads.size() + droppedReads.size();
    }

    @Override
    public int size() {
        return assembledReads.size();
    }

    @Override
    public NucleotideSequence getUmi() {
        return umi;
    }

    @Override
    public String formattedSequence() {
        return new StringBuilder(consensusSQPair.getSequence().toString()).
                append("\t").append(consensusSQPair.getQuality().toString()).
                toString();
    }

    @Override
    public QualityHistogram getQualityHistogram() {
        QualityHistogram qualityHistogram = new QualityHistogram();
        qualityHistogram.append(consensusSQPair.getQuality());
        return qualityHistogram;
    }

    @Override
    public String toString() {
        String formattedString = "@" + umi.toString() + ":" + size() + "\n" +
                NucleotideSQPair.toPrettyString(consensusSQPair) + "\n+";

        for (NucleotideSQPair read : assembledReads) {
            formattedString += "\n" + read.getSequence().toString();
        }

        /* todo
        for (int i = 0; i < substitutionCodes.length; i++) {
            formattedString += "\n" + Mutations.encode(substitutionCodes[i], NucleotideAlphabet.INSTANCE) + "\t" +
                    substitutionCounts[i];
        } */

        return formattedString;
    }
}
