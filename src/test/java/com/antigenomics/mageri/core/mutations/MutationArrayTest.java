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

package com.antigenomics.mageri.core.mutations;

import com.antigenomics.mageri.core.mapping.ConsensusAlignerParameters;
import com.antigenomics.mageri.core.mapping.alignment.AlignmentScoring;
import com.milaboratory.core.sequence.alignment.AffineGapAlignmentScoring;
import com.milaboratory.core.sequence.alignment.LocalAligner;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.antigenomics.mageri.FastTests;
import com.antigenomics.mageri.core.genomic.Reference;
import com.antigenomics.mageri.generators.MutationGenerator;
import com.antigenomics.mageri.generators.RandomReferenceGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class MutationArrayTest {
    @Test
    @Category(FastTests.class)
    public void test() {
        MutationGenerator mutationGenerator = MutationGenerator.DEFAULT.multiply(10);
        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator();
        int n = 100000;

        for (int i = 0; i < n; i++) {
            Reference reference = randomReferenceGenerator.nextReference();
            NucleotideSequence sequence = reference.getSequence();

            int[] mutations = mutationGenerator.nextMutations(sequence);
            //mutations = Mutations.filterMutations(sequence, mutations);
            //Mutations.shiftIndelsAtHomopolymers(sequence, mutations);

            MutationArray mutationArray = new MutationArray(reference, mutations);

            int[] recoveredMutations = mutationArray.getMutationCodes(false);

            /*for (int j = 0; j < mutations.length; j++) {
                System.out.println(
                        Mutations.toString(NucleotideAlphabet.INSTANCE, mutations[j]) + "\t" +
                                Mutations.toString(NucleotideAlphabet.INSTANCE, recoveredMutations[j]));
            }
            System.out.println();*/

            Assert.assertArrayEquals("Correct mutations recovered", mutations, recoveredMutations);
        }
    }

    @Test
    @Category(FastTests.class)
    public void indelCollapseExactTest() {
        String referen = "ATACGATCGCTACTACCAAAAAAACTGATCTACGTAGCTGCCATCGAGTTTTTATCGGCGCGAGCGACGATATTTCAGCGCGCAGCGAAAA",
                //        0000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889
                //        0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890
                //                                      -------
                query1 = "ATACGATCGCTACTACCAAAAAAACTGATCCTGCCATCGAGTTTTTATCGGCGCGAGCGACGATATTTCAGCGCGCAGCGAAAA",
                //        0000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889
                //        0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890
                //                                              +++++
                query2 = "ATACGATCGCTACTACCAAAAAAACTGATCTACGTAGCATTAATGCCATCGAGTTTTTATCGGCGCGAGCGACGATATTTCAGCGCGCAGCGAAAA",
                //        0000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889
                //        0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890
                //                             ---
                query3 = "ATACGATCGCTACTACCAAAACTGATCTACGTAGCTGCCATCGAGTTTTTATCGGCGCGAGCGACGATATTTCAGCGCGCAGCGAAAA",
                //        0000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889
                //        0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890
                //                                +++
                query4 = "ATACGATCGCTACTACCAAAAAAAAAACTGATCTACGTAGCTGCCATCGAGTTTTTATCGGCGCGAGCGACGATATTTCAGCGCGCAGCGAAAA";

        Assert.assertEquals("Composite deletion", "D30-37:TACGTAG>", getMutations(referen, query1).toString());
        Assert.assertEquals("Composite insertion", "I38:>ATTAA>", getMutations(referen, query2).toString());
        Assert.assertEquals("Composite deletion at homopolymer, right-shifted", "D21-24:AAA>", getMutations(referen, query3).toString());
        Assert.assertEquals("Composite insertion at homopolymer, right-shifted", "I24:>AAA", getMutations(referen, query4).toString());
    }

    private static MutationArray getMutations(String ref, String query) {
        NucleotideSequence r = new NucleotideSequence(ref), q = new NucleotideSequence(query);
        AffineGapAlignmentScoring scoring = new AlignmentScoring(ConsensusAlignerParameters.DEFAULT).asInternalScoring();
        LocalAlignment alignment = LocalAligner.align(scoring, r, q);
        return new MutationArray(r, alignment.getAbsoluteMutations());
    }
}
