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
 * Last modified on 10.4.2015 by mikesh
 */

package com.milaboratory.oncomigec.core.input.index;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;

import java.util.BitSet;

public class Read {
    private final NucleotideSequence sequence;
    private final BitSet qualityMask;

    public Read(NucleotideSequence sequence,
                BitSet qualityMask) {
        this.sequence = sequence;
        this.qualityMask = qualityMask;
    }

    public Read rc() {
        BitSet qualityMask = new BitSet(length());
        for (int i = 0; i < length(); i++) {
            qualityMask.set(i, this.qualityMask.get(length() - i - 1));
        }
        return new Read(sequence.getReverseComplement(), qualityMask);
    }

    public Read trim5Prime(int from) {
        return region(from, length());
    }

    public Read trim3Prime(int to) {
        return region(0, to);
    }

    public Read region(int from, int to) {
        return new Read(sequence.getRange(from, to),
                qualityMask.get(from, to));
    }

    public NucleotideSequence getSequence() {
        return sequence;
    }

    public boolean goodQuality(int pos) {
        return qualityMask.get(pos);
    }

    public int length() {
        return sequence.size();
    }
}
