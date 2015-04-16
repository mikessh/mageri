/*
 * Copyright 2013-2015 Mikhail Shugay (mikhail.shugay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last modified on 16.2.2015 by mikesh
 */

package com.milaboratory.oncomigec.core.input;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.input.index.Read;

import java.util.ArrayList;
import java.util.List;

public class SMig extends Mig {
    private final List<Read> reads;
    private final NucleotideSequence umi;

    public SMig(List<Read> reads, NucleotideSequence umi) {
        this.reads = reads;
        this.umi = umi;
    }

    public List<Read> getReads() {
        return reads;
    }

    public List<NucleotideSequence> getSequences() {
        List<NucleotideSequence> sequences = new ArrayList<>();
        for (Read read : reads) {
            sequences.add(read.getSequence());
        }
        return sequences;
    }

    @Override
    public NucleotideSequence getUmi() {
        return umi;
    }

    @Override
    public int size() {
        return reads.size();
    }
}
