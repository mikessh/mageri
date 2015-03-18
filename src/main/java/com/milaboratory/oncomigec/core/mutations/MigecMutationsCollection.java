package com.milaboratory.oncomigec.core.mutations;

import com.milaboratory.core.sequence.Range;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.oncomigec.core.genomic.Reference;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MigecMutationsCollection implements Iterable<MigecMutation>, MutationsCollection {
    private static final int INDEL_BOUNDARY_SIZE = 3, SUBSTITUTION_BOUNDARY_SIZE = 1;
    private final Reference reference;
    private final List<MigecMutation> mutations = new LinkedList<>();
    private AtomicInteger filteredCount = new AtomicInteger(), substitutionCount = new AtomicInteger();

    public static MigecMutationsCollection EMPTY(Reference reference) {
        return new MigecMutationsCollection(reference);
    }

    private static boolean checkBounds(int pos, Range range, boolean isSubstitution) {
        int boundarySize = isSubstitution ? SUBSTITUTION_BOUNDARY_SIZE : INDEL_BOUNDARY_SIZE;
        return pos - range.getFrom() > boundarySize &&
                range.getTo() - pos > boundarySize;
    }

    public static MigecMutationsCollection WITH_BOUNDARY_FILTER(Reference reference,
                                                                int[] mutationCodes,
                                                                Range range) {
        MigecMutationsCollection migecMutationsCollection = new MigecMutationsCollection(reference);

        for (int i = 0; i < mutationCodes.length; i++) {
            int code = mutationCodes[i], pos = Mutations.getPosition(code);
            boolean isSubstitution = Mutations.isSubstitution(code);

            if (checkBounds(pos, range, isSubstitution)) {
                if (isSubstitution)
                    migecMutationsCollection.substitutionCount.incrementAndGet();
                migecMutationsCollection.mutations.add(new MigecMutation(code, migecMutationsCollection));
            }
        }

        return migecMutationsCollection;
    }

    public MigecMutationsCollection(Reference reference, int[] mutationCodes) {
        this.reference = reference;

        for (int i = 0; i < mutationCodes.length; i++) {
            MigecMutation mutation = new MigecMutation(mutationCodes[i], this);
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
            this.mutations.add(newMutation);
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
