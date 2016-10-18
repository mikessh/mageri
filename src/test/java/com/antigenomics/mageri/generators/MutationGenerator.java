/*
 * Copyright 2014-2016 Mikhail Shugay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.antigenomics.mageri.generators;

import com.milaboratory.core.sequence.mutations.*;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.antigenomics.mageri.core.genomic.Reference;

import java.util.Random;

public class MutationGenerator {
    private final Random random;
    private final SubstitutionModel substitutionModel;
    private final NucleotideMutationModel mutationModel;
    private static final double DEL_PROB = 0.005, INS_PROB = 0.002;

    public static final MutationGenerator DEFAULT = new MutationGenerator(51102L, 1.0),
            SEQ_Q30 = getUniform(0.001),
            NO_INDEL = new MutationGenerator(51102L, 0.0),
            NO_INDEL_SKEWED = new MutationGenerator(51102L, 0.0, getSkewedNucleotideSubstitutionModel());

    public static MutationGenerator getUniform(double freq) {
        return new MutationGenerator(51102L, 0.0, getUniformSubstitutionModel(freq));
    }

    private static SubstitutionModel getSkewedNucleotideSubstitutionModel() {
        return getSkewedNucleotideSubstitutionModel(SubstitutionModels.getEmpiricalNucleotideSubstitutionModel());
    }

    private static SubstitutionModel getSkewedNucleotideSubstitutionModel(SubstitutionModel substitutionModel) {
        SubstitutionModelBuilder builder = new SubstitutionModelBuilder(4);

        for (byte i = 0; i < 4; i++) {
            for (byte j = 0; j < 4; j++) {
                if (i != j) {
                    builder.setProbability(i, j, Math.pow(substitutionModel.getValue(i, j), 3) * 500_000);
                }
            }
        }

        return builder.build();
    }

    private static SubstitutionModel getUniformSubstitutionModel(double errorRate) {
        SubstitutionModelBuilder builder = new SubstitutionModelBuilder(4);

        for (byte i = 0; i < 4; i++) {
            for (byte j = 0; j < 4; j++) {
                if (i != j) {
                    builder.setProbability(i, j, errorRate);
                }
            }
        }

        return builder.build();
    }

    public MutationGenerator(long seed, double indelFactor, SubstitutionModel substitutionModel) {
        this.random = new Random(seed);
        this.substitutionModel = substitutionModel;
        this.mutationModel = new GenericNucleotideMutationModel(
                substitutionModel,
                DEL_PROB * indelFactor, INS_PROB * indelFactor,
                seed);
    }

    public MutationGenerator(long seed, double indelFactor) {
        this.random = new Random(seed);
        this.substitutionModel = SubstitutionModels.getEmpiricalNucleotideSubstitutionModel();
        this.mutationModel = new GenericNucleotideMutationModel(
                substitutionModel,
                DEL_PROB * indelFactor, INS_PROB * indelFactor,
                seed);
    }

    private MutationGenerator(Random random, NucleotideMutationModel mutationModel, SubstitutionModel substitutionModel) {
        this.random = random;
        this.substitutionModel = substitutionModel;
        this.mutationModel = mutationModel;
    }

    public MutationGenerator multiply(double multiplier) {
        return new MutationGenerator(random,
                mutationModel.multiply(multiplier),
                substitutionModel.multiply(multiplier)
        );
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

    public NucleotideMutationModel getMutationModel() {
        return mutationModel;
    }

    public SubstitutionModel getSubstitutionModel() {
        return substitutionModel;
    }
}
