package com.milaboratory.oncomigec.core.mutations;

import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.oncomigec.core.genomic.Reference;
import org.apache.commons.collections.iterators.ArrayIterator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

public class MutationDifference implements Iterable<Integer>, Serializable {
    private final Reference reference;
    private final int[] difference;

    public MutationDifference(Reference reference, int[] parentMutations, int[] childMutations) {
        this.reference = reference;

        // Remove exact matching mutations, important in case of insertions because of reference-based structure
        final boolean[] parentFilter = new boolean[parentMutations.length],
                childFilter = new boolean[childMutations.length];

        for (int i = 0; i < parentMutations.length; i++) {
            for (int j = 0; j < childMutations.length; j++) {
                if (!childFilter[j] && parentMutations[i] == childMutations[j]) {
                    parentFilter[i] = true;
                    childFilter[j] = true;
                    //System.out.println(Mutations.toString(NucleotideAlphabet.INSTANCE, parentMutations[i]));
                    break;
                }
            }
        }

        //System.out.println(Mutations.toString(NucleotideAlphabet.INSTANCE, parentMutations));
        //System.out.println(Mutations.toString(NucleotideAlphabet.INSTANCE, childMutations));

        //System.out.println(Arrays.toString(parentFilter));
        //System.out.println(Arrays.toString(childFilter));

        final int[] filteredParentMutations = filterMutations(parentMutations, parentFilter),
                filteredChildMutations = filterMutations(childMutations, childFilter);

        // Compute difference
        int[] difference = Mutations.combineMutations(Mutations.invertMutations(filteredParentMutations),
                filteredChildMutations);
        Mutations.shiftIndelsAtHomopolymers(reference.getSequence(), difference);

        final int[] filteredDifference = Mutations.filterMutations(reference.getSequence(), difference);

        this.difference = filteredDifference;
        //System.out.println(Mutations.toString(NucleotideAlphabet.INSTANCE, filteredParentMutations));
        //System.out.println(Mutations.toString(NucleotideAlphabet.INSTANCE, filteredChildMutations));
        //System.out.println(Mutations.toString(NucleotideAlphabet.INSTANCE, this.difference));
        //System.out.println();
    }

    private static int[] filterMutations(int[] mutationCodes, boolean[] filter) {
        int[] mutationCodesFiltered = new int[mutationCodes.length];
        int k = 0;
        for (int i = 0; i < mutationCodes.length; i++)
            if (!filter[i])
                mutationCodesFiltered[k++] = mutationCodes[i];
        return Arrays.copyOf(mutationCodesFiltered, k);
    }

    //todo: test
    int[] mutationCodes() {
        return difference;
    }

    public Reference getReference() {
        return reference;
    }

    public int size() {
        return difference.length;
    }

    public boolean isEmpty() {
        return difference.length == 0;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new ArrayIterator(difference);
    }
}
