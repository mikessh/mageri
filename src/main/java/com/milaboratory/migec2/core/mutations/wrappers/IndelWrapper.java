package com.milaboratory.migec2.core.mutations.wrappers;

import com.milaboratory.core.sequence.mutations.MutationType;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequenceBuilder;

import java.util.Arrays;

public abstract class IndelWrapper extends MutationWrapper {
    private byte[] buffer;
    private int bufferPos = 0;

    protected NucleotideSequence sequence;
    protected final int start;
    protected int end;


    protected IndelWrapper(MutationType type, int mutationCode) {
        super(type, mutationCode);
        buffer = new byte[5];
        start = Mutations.getPosition(mutationCode);
        end = start + 1;
        buffer[bufferPos++] = nt(mutationCode);
    }

    //
    // Basic
    //

    @Override
    public final int position() {
        return start;
    }

    public final int size() {
        return sequence.size();
    }

    public final NucleotideSequence sequence() {
        return sequence;
    }


    //
    // Internal / for MutationWrapperCollection implementation
    //

    // gets the non-changing position (end or start) for incremental mutation of this type
    protected abstract int headPos();

    // gets the nucleotide inserted/deleted
    protected abstract byte nt(int mutationCode);

    void build() {
        int length = end - start;
        NucleotideSequenceBuilder nucleotideSequenceBuilder = new NucleotideSequenceBuilder(length);
        for (int i = 0; i < length; i++)
            nucleotideSequenceBuilder.setCode(i, buffer[i]);
        sequence = nucleotideSequenceBuilder.create();
        buffer = null;
    }

    boolean append(int mutationCode) {
        if (Mutations.getType(mutationCode) == this.type) {
            if (buffer == null)
                System.out.println("Append call on finalized Indel");

            int pos = Mutations.getPosition(mutationCode);

            if (pos == this.headPos()) {
                end++;
                if (bufferPos == buffer.length)
                    buffer = Arrays.copyOf(buffer, buffer.length * 2);
                buffer[bufferPos++] = nt(mutationCode);
                return true;
            } else {
                return false;
            }
        }

        throw new IllegalArgumentException("Cannot append mutation of different type");
    }
}
