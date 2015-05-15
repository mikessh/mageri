/*
 * Copyright (c) 2014-2015, Bolotin Dmitry, Chudakov Dmitry, Shugay Mikhail
 * (here and after addressed as Inventors)
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact the Inventors using one of the following
 * email addresses: chudakovdm@mail.ru, chudakovdm@gmail.com
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */

package com.milaboratory.oncomigec.core.mutations;

import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.oncomigec.core.genomic.Reference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MutationArray implements Serializable {
    protected final Reference reference;
    protected final List<Mutation> mutations;
    protected int length, numberOfFiltered = 0;

    public MutationArray(Reference reference, int[] codes) {
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

    public Reference getReference() {
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
}
