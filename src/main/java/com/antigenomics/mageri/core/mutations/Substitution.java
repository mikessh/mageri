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

public class Substitution extends Mutation {
    protected final int code;

    public Substitution(MutationArray parent,
                        int code) {
        super(parent);
        this.code = code;
    }

    @Override
    public char getType() {
        return 'S';
    }

    @Override
    public int getStart() {
        return Mutations.getPosition(code);
    }

    @Override
    public int getEnd() {
        return getStart() + 1;
    }

    public int getCode() {
        return code;
    }

    @Override
    public NucleotideSequence getRef() {
        NucleotideSequenceBuilder nsb = new NucleotideSequenceBuilder(1);
        nsb.setCode(0, (byte) Mutations.getFrom(code));
        return nsb.create();
    }

    @Override
    public NucleotideSequence getAlt() {
        NucleotideSequenceBuilder nsb = new NucleotideSequenceBuilder(1);
        nsb.setCode(0, (byte) Mutations.getTo(code));
        return nsb.create();
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Substitution that = (Substitution) o;

        if (code != that.code) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return code;
    }

    @Override
    public String toString() {
        return getType() + "" + getStart() + ":" + getRef().toString() + ">" + getAlt().toString();
    }
}
