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
package com.milaboratory.migec2.core.io.entity;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SSequencingRead;

import java.util.List;

public class SMig extends Mig {
    private final List<SSequencingRead> reads;
    private final NucleotideSequence umi;

    public SMig(List<SSequencingRead> reads, NucleotideSequence umi) {
        this.reads = reads;
        this.umi = umi;
    }

    public List<SSequencingRead> getReads() {
        return reads;
    }

    @Override
    public NucleotideSequence getUmi() {
        return umi;
    }

    @Override
    public int size() {
        return reads.size();
    }

    @Override
    public String toString() {
        String formattedString = "@" + umi.toString() + ":" + reads.size();
        for (SSequencingRead read : reads) {
            formattedString += "\n" + NucleotideSQPair.toPrettyString(read.getData());
        }
        return formattedString;
    }
}
