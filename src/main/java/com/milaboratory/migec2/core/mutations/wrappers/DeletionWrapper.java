package com.milaboratory.migec2.core.mutations.wrappers;

import com.milaboratory.core.sequence.mutations.MutationType;
import com.milaboratory.core.sequence.mutations.Mutations;

public final class DeletionWrapper extends IndelWrapper {

    public DeletionWrapper(int mutationCode) {
        super(MutationType.Deletion, mutationCode);
    }

    //
    // For append mechanics
    //

    // Two consequent deletions have pos1=x and pos2=x+1

    @Override
    protected int headPos() {
        return end;
    }

    @Override
    protected byte nt(int mutationCode) {
        return (byte) Mutations.getFrom(mutationCode);
    }

    //
    // Other
    //

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("D").append(start);
        if (end - start == 1)
            return sb.toString();
        else
            return sb.append("-").append(end - 1).toString();
    }
}
