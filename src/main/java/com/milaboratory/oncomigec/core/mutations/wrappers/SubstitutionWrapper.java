package com.milaboratory.oncomigec.core.mutations.wrappers;

import com.milaboratory.core.sequence.mutations.MutationType;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;

public final class SubstitutionWrapper extends MutationWrapper {
    private final int at;
    private final byte from, to;

    public SubstitutionWrapper(int mutationCode) {
        super(MutationType.Substitution, mutationCode);
        this.at = Mutations.getPosition(mutationCode);
        this.from = (byte) Mutations.getFrom(mutationCode);
        this.to = (byte) Mutations.getTo(mutationCode);
    }

    @Override
    public int position() {
        return at;
    }

    public byte from() {
        return from;
    }

    public byte to() {
        return to;
    }

    @Override
    public String toString() {
        return new StringBuilder("S").append(at + 1).append(":"). //1-based
                append(NucleotideAlphabet.INSTANCE.symbolFromCode(from)).
                append(">").append(NucleotideAlphabet.INSTANCE.symbolFromCode(to)).toString();
    }
}
