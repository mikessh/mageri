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

package com.antigenomics.mageri.core.input;

import com.antigenomics.mageri.core.input.index.Read;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.antigenomics.mageri.core.Mig;
import com.antigenomics.mageri.pipeline.analysis.Sample;

import java.util.ArrayList;
import java.util.List;

public final class SMig extends Mig {
    private final List<Read> reads;

    public SMig(Sample sample, NucleotideSequence umi, List<Read> reads) {
        super(sample, umi);
        this.reads = reads;
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
    public int size() {
        return reads.size();
    }

    @Override
    public boolean isPairedEnd() {
        return false;
    }
}
