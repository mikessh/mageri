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

package com.antigenomics.mageri.generators;

import com.antigenomics.mageri.core.genomic.Reference;
import com.antigenomics.mageri.core.genomic.ReferenceLibrary;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;

public class ReferenceLibrarySampler implements RandomSequenceGenerator {
    private int minReadSize = 50, maxReadSize = 150;
    private final ReferenceLibrary referenceLibrary;

    public ReferenceLibrarySampler(ReferenceLibrary referenceLibrary) {
        this.referenceLibrary = referenceLibrary;
    }

    public int getMinReadSize() {
        return minReadSize;
    }

    public int getMaxReadSize() {
        return maxReadSize;
    }

    public void setMinReadSize(int minReadSize) {
        this.minReadSize = minReadSize;
    }

    public void setMaxReadSize(int maxReadSize) {
        this.maxReadSize = maxReadSize;
    }

    @Override
    public NucleotideSequence nextSequence() {
        Reference reference = referenceLibrary.getAt(RandomUtil.nextIndex(referenceLibrary.size()));
        NucleotideSequence sequence = reference.getSequence();

        int size = RandomUtil.nextFromRange(minReadSize, maxReadSize),
                from = RandomUtil.nextFromRange(0, Math.max(0, sequence.size() - size));
        int to = Math.min(sequence.size(), from + size);

        return sequence.getRange(from, to);
    }
}
