package com.milaboratory.migec2.core.mutations;

import com.milaboratory.core.sequence.mutations.MutationType;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;

public final class MigecMutation {
    private final int code;
    private final MigecMutationsCollection parent;
    private boolean filtered = false;

    MigecMutation(int code, MigecMutationsCollection parent) {
        this.code = code;
        this.parent = parent;
    }

    public int code() {
        return code;
    }

    public MutationType type() {
        return Mutations.getType(code);
    }

    public boolean isSubstitution() {
        return Mutations.isSubstitution(code);
    }

    public boolean isInsertion() {
        return Mutations.isInsertion(code);
    }

    public boolean isDeletion() {
        return Mutations.isDeletion(code);
    }

    public int pos() {
        return Mutations.getPosition(code);
    }

    public byte from() {
        return (byte) Mutations.getFrom(code);
    }

    public byte to() {
        return (byte) Mutations.getTo(code);
    }

    public void filter() {
        if (!filtered) {
            parent.incrementFiltered();
            if (isSubstitution())
                parent.decrementSubstitutions();
            filtered = true;
        }
    }

    public boolean isFiltered() {
        return filtered;
    }

    @Override
    public String toString() {
        return (filtered ? "!" : "") + Mutations.toString(NucleotideAlphabet.INSTANCE, code);
    }
}
