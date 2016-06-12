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

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.antigenomics.mageri.core.Mig;
import com.antigenomics.mageri.pipeline.analysis.Sample;

public abstract class Consensus<ReadType extends SequencingRead> extends Mig {
    public Consensus(Sample sample, NucleotideSequence umi) {
        super(sample, umi);
    }

    @Override
    public int size() {
        return 1;
    }
    
    public abstract int getAssembledSize();

    public abstract int getTrueSize();

    public abstract ReadType asRead();
}
