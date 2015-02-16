package com.milaboratory.oncomigec.core.mutations.wrappers;

import com.milaboratory.core.sequence.mutations.MutationType;
import com.milaboratory.core.sequence.mutations.Mutations;

public final class InsertionWrapper extends IndelWrapper {
    public InsertionWrapper(int mutationCode) {
        super(MutationType.Insertion, mutationCode);
    }

    //
    // For append mechanics
    //

    // Two consequent insertions have pos1=x and pos2=x

    @Override
    protected int headPos() {
        return start;
    }

    @Override
    protected byte nt(int mutationCode) {
        return (byte) Mutations.getTo(mutationCode);
    }

    //
    // Other
    //

    @Override
    public String toString() {
        return new StringBuilder("I").append(start).append(":").append(sequence.toString()).toString();
    }
}
