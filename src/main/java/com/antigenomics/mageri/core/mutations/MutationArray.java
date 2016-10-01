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

import com.milaboratory.core.sequence.Sequence;
import com.milaboratory.core.sequence.mutations.Mutations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MutationArray implements Serializable {
    protected final Sequence reference;
    protected final List<Mutation> mutations;
    protected int length, numberOfFiltered = 0;

    public MutationArray(Sequence reference, int[] codes) {
        this.reference = reference;
        this.mutations = new ArrayList<>();
        this.length = codes.length;

        IndelAccumulator insertionAccumulator = new InsertionAccumulator(),
                deletionAccumulator = new DeletionAccumulator();

        for (int code : codes) {
            switch (Mutations.getType(code)) {
                case Insertion:
                    deletionAccumulator.safeFlush();
                    insertionAccumulator.append(code);
                    break;
                case Deletion:
                    insertionAccumulator.safeFlush();
                    deletionAccumulator.append(code);
                    break;
                case Substitution:
                    insertionAccumulator.safeFlush();
                    deletionAccumulator.safeFlush();
                    mutations.add(new Substitution(this, code));
                    break;
            }
        }

        insertionAccumulator.safeFlush();
        deletionAccumulator.safeFlush();
    }

    public void append(MutationArray other) {
        mutations.addAll(other.mutations);
        length += other.length;
    }

    public Sequence getReference() {
        return reference;
    }

    public int getLength() {
        return length;
    }

    public int getNumberOfFiltered() {
        return numberOfFiltered;
    }

    public List<Mutation> getMutations() {
        return Collections.unmodifiableList(mutations);
    }

    public List<Mutation> getMutations(boolean filtered) {
        List<Mutation> mutations = new ArrayList<>(length - numberOfFiltered);
        for (Mutation mutation : this.mutations) {
            if (!(filtered && mutation.filtered)) {
                mutations.add(mutation);
            }
        }
        return mutations;
    }

    public int[] getMutationCodes(boolean filtered) {
        int[] codes = new int[length - (filtered ? numberOfFiltered : 0)];
        int i = 0;
        for (Mutation mutation : mutations) {
            if (!(filtered && mutation.filtered)) {
                if (mutation instanceof Indel) {
                    for (int code : ((Indel) mutation).codes) {
                        codes[i++] = code;
                    }
                } else {
                    codes[i++] = ((Substitution) mutation).code;
                }
            }
        }
        return codes;
    }

    private abstract class IndelAccumulator {
        protected final int[] codes;
        protected int counter = 0;

        public IndelAccumulator() {
            this.codes = new int[length];
        }

        protected int getLastPos() {
            return Mutations.getPosition(codes[counter - 1]);
        }

        protected abstract boolean canExtend(int pos);

        public void append(int code) {
            if (counter == 0 || canExtend(Mutations.getPosition(code)) || flush())
                codes[counter++] = code;
        }

        protected abstract Indel create();

        public boolean flush() {
            mutations.add(create());
            counter = 0;
            return true;
        }

        public void safeFlush() {
            if (counter > 0)
                flush();
        }
    }

    private class InsertionAccumulator extends IndelAccumulator {
        @Override
        protected boolean canExtend(int pos) {
            return getLastPos() == pos;
        }

        @Override
        public Indel create() {
            return new Insertion(MutationArray.this,
                    Arrays.copyOf(codes, counter));
        }
    }


    private class DeletionAccumulator extends IndelAccumulator {
        @Override
        protected boolean canExtend(int pos) {
            return getLastPos() + 1 == pos;
        }

        @Override
        public Indel create() {
            return new Deletion(MutationArray.this,
                    Arrays.copyOf(codes, counter));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Mutation mutation : mutations) {
            sb.append(",");
            sb.append(mutation.toString());
            if (mutation.filtered)
                sb.append("(f)");
        }

        return sb.deleteCharAt(0).toString();
    }
}
