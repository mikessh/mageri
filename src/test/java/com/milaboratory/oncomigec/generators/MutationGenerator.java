/*
 * Copyright (c) 2014-2015, Bolotin Dmitry, Chudakov Dmitry, Shugay Mikhail
 * (here and after addressed as Inventors)
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact the Inventors using one of the following
 * email addresses: chudakovdm@mail.ru, chudakovdm@gmail.com
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */

package com.milaboratory.oncomigec.generators;

import com.milaboratory.core.sequence.mutations.*;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.genomic.Reference;

import java.util.Random;

public class MutationGenerator {
    private final Random random;
    private final SubstitutionModel substitutionModel;
    private final NucleotideMutationModel mutationModel;
    private static final double DEL_PROB = 0.005, INS_PROB = 0.002;

    public static final MutationGenerator DEFAULT = new MutationGenerator(51102L, 1.0),
            NO_INDEL = new MutationGenerator(51102L, 0.0),
            NO_INDEL_SKEWED = new MutationGenerator(51102L, 0.0, getSkewedNucleotideSubstitutionModel());

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
