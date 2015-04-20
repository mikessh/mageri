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

package com.milaboratory.oncomigec.core;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.ReadSpecific;
import com.milaboratory.oncomigec.pipeline.analysis.Sample;

import java.io.Serializable;

public abstract class Mig implements ReadSpecific, Serializable {
    protected final Sample sample;
    protected final NucleotideSequence umi;

    public Mig(Sample sample, NucleotideSequence umi) {
        this.sample = sample;
        this.umi = umi;
    }

    public Sample getSample() {
        return sample;
    }

    public NucleotideSequence getUmi() {
        return umi;
    }

    public abstract int size();
}
