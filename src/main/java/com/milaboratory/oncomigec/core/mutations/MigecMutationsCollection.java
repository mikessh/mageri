package com.milaboratory.oncomigec.core.mutations;

import com.milaboratory.oncomigec.core.genomic.Reference;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MigecMutationsCollection implements Iterable<MigecMutation>, MutationsCollection {
    private final Reference reference;
    private final List<MigecMutation> mutations = new LinkedList<>();
    private AtomicInteger filteredCount = new AtomicInteger(), substitutionCount = new AtomicInteger();

    public static MigecMutationsCollection EMPTY(Reference reference) {
        return new MigecMutationsCollection(reference);
    }

    public MigecMutationsCollection(Reference reference, int[] mutationCodes) {
        this.reference = reference;

        for (int mutationCode : mutationCodes) {
            MigecMutation mutation = new MigecMutation(mutationCode, this);

            if (mutation.isSubstitution())
                substitutionCount.incrementAndGet();
            mutations.add(mutation);
        }
    }

    public MigecMutationsCollection(Reference reference) {
        this.reference = reference;
    }

    public void append(MigecMutationsCollection other) {
        if (other.reference != this.reference)
            throw new IllegalArgumentException("References don't match");

        // Don't forget to change parent
        for (MigecMutation mutation : other.mutations) {
            MigecMutation newMutation = new MigecMutation(mutation.code(), this);
            mutations.add(newMutation);
            if (mutation.isFiltered())
                newMutation.filter();
            if (mutation.isSubstitution())
                substitutionCount.incrementAndGet();
        }
    }

    public Reference getReference() {
        return reference;
    }

    void incrementFiltered() {
        filteredCount.incrementAndGet();
    }

    void decrementSubstitutions() {
        substitutionCount.decrementAndGet();
    }

    @Override
    public Iterator<MigecMutation> iterator() {
        return mutations.iterator();
    }

    @Override
    public int[] getMutationCodes() {
        final int[] mutationCodes = new int[mutations.size() - filteredCount.get()];
        int i = 0;
        for (MigecMutation mutation : mutations) {
            if (!mutation.isFiltered())
                mutationCodes[i++] = mutation.code();
        }
        //Mutations.printMutations(NucleotideAlphabet.INSTANCE, mutationCodes);
        //System.out.println(toString());
        return mutationCodes;
    }

    @Override
    public int size() {
        return mutations.size() - filteredCount.get();
    }

    @Override
    public int substitutionCount() {
        return substitutionCount.get();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String separator = "";
        for (MigecMutation mutation : mutations) {
            sb.append(separator).append(mutation.toString());
            separator = ",";
        }
        return sb.toString();
    }
}
