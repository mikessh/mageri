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

package com.antigenomics.mageri.core.input.raw;

import com.antigenomics.mageri.core.assemble.Consensus;
import com.antigenomics.mageri.core.assemble.PConsensus;
import com.antigenomics.mageri.core.assemble.SConsensus;
import com.antigenomics.mageri.core.input.index.ReadContainer;
import com.antigenomics.mageri.misc.ProcessorResultWrapper;
import com.antigenomics.mageri.pipeline.analysis.Sample;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;

import java.util.HashSet;

public class SomewhatRawReadProperlyWrapped extends ProcessorResultWrapper<Consensus> {
    private static final NucleotideSequence DUMMY_UMI = new NucleotideSequence("");

    public SomewhatRawReadProperlyWrapped(Sample sample, ReadContainer readContainer) {
        super(createConsensus(sample, readContainer));
    }

    private static Consensus createConsensus(Sample sample, ReadContainer readContainer) {
        SConsensus consensus1 = new SConsensus(sample, DUMMY_UMI,
                readContainer.getFirst().toNucleotideSQPair(), new HashSet<Integer>(), 1, 1);

        return readContainer.isPairedEnd() ? new PConsensus(
                consensus1,
                new SConsensus(sample, DUMMY_UMI,
                        readContainer.getSecond().toNucleotideSQPair(), new HashSet<Integer>(), 1, 1)) :
                consensus1;
    }
}
