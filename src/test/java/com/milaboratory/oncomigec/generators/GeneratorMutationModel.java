package com.milaboratory.oncomigec.generators;

import com.milaboratory.core.sequence.mutations.GenericNucleotideMutationModel;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.mutations.NucleotideMutationModel;
import com.milaboratory.core.sequence.mutations.SubstitutionModels;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.genomic.Reference;

import java.util.Random;

public class GeneratorMutationModel {
    private final Random random;
    private final NucleotideMutationModel mutationModel;
    private static final double DEL_PROB = 0.005, INS_PROB = 0.002;

    public static final GeneratorMutationModel DEFAULT = new GeneratorMutationModel(51102L, 1.0, 1.0),
            NO_INDEL = new GeneratorMutationModel(51102L, 1.0, 0.0);

    public GeneratorMutationModel(long seed, double multiplier, double indelFactor) {
        this.random = new Random(seed);
        this.mutationModel = new GenericNucleotideMutationModel(
                SubstitutionModels.getEmpiricalNucleotideSubstitutionModel(),
                DEL_PROB * indelFactor, INS_PROB * indelFactor,
                seed).multiply(multiplier);
    }

    private GeneratorMutationModel(Random random, NucleotideMutationModel mutationModel) {
        this.random = random;
        this.mutationModel = mutationModel;
    }

    public GeneratorMutationModel multiply(double multiplier) {
        return new GeneratorMutationModel(random, mutationModel.multiply(multiplier));
    }

    public int[] nextMutations(NucleotideSequence reference) {
        return Mutations.generateMutations(reference, mutationModel);
    }

    public int[] nextMutations(Reference reference) {
        return Mutations.generateMutations(reference.getSequence(), mutationModel);
    }

    public NucleotideSequence nextMutatedSequence(NucleotideSequence reference) {
        return Mutations.mutate(reference, Mutations.generateMutations(reference, mutationModel));
    }

    public NucleotideSequence nextMutatedSequence(Reference reference) {
        NucleotideSequence sequence = reference.getSequence();
        return Mutations.mutate(sequence, Mutations.generateMutations(sequence, mutationModel));
    }

    public int nextFromRange(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    public int nextIndex(int n) {
        return random.nextInt(n);
    }
}
