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

package com.antigenomics.mageri.core.mutations;

import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequenceBuilder;

public class Deletion extends Indel {
    public Deletion(MutationArray parent, int[] codes) {
        super(parent, codes);
    }

    @Override
    public char getType() {
        return 'D';
    }

    @Override
    public int getStart() {
        return Mutations.getPosition(codes[0]);
    }

    @Override
    public int getEnd() {
        return Mutations.getPosition(codes[codes.length - 1]) + 1;
    }

    @Override
    public NucleotideSequence getRef() {
        NucleotideSequenceBuilder nsb = new NucleotideSequenceBuilder(getLength());
        for (int i = 0; i < getLength(); i++) {
            nsb.setCode(i, (byte) Mutations.getFrom(codes[i]));
        }
        return nsb.create();
    }

    @Override
    public NucleotideSequence getAlt() {
        return NucleotideSequence.EMPTY_NUCLEOTIDE_SEUQUENCE;
    }
}
