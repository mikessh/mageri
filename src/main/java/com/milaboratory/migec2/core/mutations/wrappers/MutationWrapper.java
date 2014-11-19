package com.milaboratory.migec2.core.mutations.wrappers;

import com.milaboratory.core.sequence.mutations.MutationType;
import com.milaboratory.core.sequence.mutations.Mutations;

public abstract class MutationWrapper {
    protected final MutationType type;

    protected MutationWrapper(MutationType type, int mutationCode) {
        if (Mutations.getType(mutationCode) != type)
            throw new IllegalArgumentException("Type of mutation and mutationCode don't match");
        this.type = type;
    }

    public final MutationType getType() {
        return type;
    }

    public abstract int position();
}
