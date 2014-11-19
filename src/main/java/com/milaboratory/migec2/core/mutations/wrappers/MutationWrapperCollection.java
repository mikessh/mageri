package com.milaboratory.migec2.core.mutations.wrappers;

import com.milaboratory.core.sequence.mutations.MutationType;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import com.milaboratory.migec2.core.align.reference.Reference;
import com.milaboratory.migec2.core.mutations.MutationsCollection;

import java.util.LinkedList;
import java.util.List;

public class MutationWrapperCollection implements MutationsCollection {
    private final LinkedList<MutationWrapper> mutations = new LinkedList<>();
    private final Reference reference;
    private final int[] mutationCodes;
    private final int nSubstitutions;

    public MutationWrapperCollection(Reference reference, int[] mutationCodes) {
        this.reference = reference;
        this.mutationCodes = mutationCodes;

        int nSubstitutions = 0;
        for (int i = 0; i < mutationCodes.length; i++) {
            int mutationCode = mutationCodes[i];

            MutationWrapper last = mutations.size() > 0 ? mutations.getLast() : null;

            switch (Mutations.getType(mutationCode)) {
                case Insertion:                                     // if
                    if (last == null ||                             // no mutations so far
                            last.type != MutationType.Insertion ||  // previous mutation is non-insertion
                            !((IndelWrapper) last).append(mutationCode))   // failed to extend previous insertion
                        mutations.add(new InsertionWrapper(mutationCode)); // then new insertion
                    break;
                case Deletion:
                    if (last == null ||
                            last.type != MutationType.Deletion ||
                            !((IndelWrapper) last).append(mutationCode))
                        mutations.add(new DeletionWrapper(mutationCode));
                    break;
                case Substitution:
                    SubstitutionWrapper substitution = new SubstitutionWrapper(mutationCode);
                    mutations.add(substitution);
                    nSubstitutions++;
                    break;
            }
        }

        this.nSubstitutions = nSubstitutions;

        // Finalize indels
        for (MutationWrapper mutation : mutations) {
            if (mutation.type != MutationType.Substitution) {
                ((IndelWrapper) mutation).build();
            }
        }
    }

    public Reference getReference() {
        return reference;
    }

    @Override
    public int[] getMutationCodes() {
        return mutationCodes;
    }

    @Override
    public int size() {
        return mutations.size();
    }

    @Override
    public int substitutionCount() {
        return nSubstitutions;
    }

    public List<MutationWrapper> getMutations() {
        return mutations;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        String sep = "";

        for (MutationWrapper mutation : mutations) {
            sb.append(sep).append(mutation);
            if (sep.equals(""))
                sep = ",";
        }

        return sb.toString();
    }
}
