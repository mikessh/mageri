/*
 * Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)
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
package com.milaboratory.migec2.core.align.processor.aligners;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.migec2.core.align.entity.SAlignmentResult;
import com.milaboratory.migec2.core.align.processor.Aligner;
import com.milaboratory.migec2.core.align.processor.AlignerFactory;
import com.milaboratory.migec2.core.align.reference.ReferenceLibrary;
import com.milaboratory.migec2.util.testing.PercentRange;
import com.milaboratory.migec2.util.testing.generators.RandomIndelGenerator;
import com.milaboratory.migec2.util.testing.generators.RandomReferenceGenerator;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

public class AlignersTest {
    int nReferences = 100, nRepetitions1 = 100, nRepetitions2 = 100;

    @Test
    public void runTests() throws InvocationTargetException, IOException,
            IllegalAccessException, NoSuchMethodException, URISyntaxException {
        System.out.println("Generating random mutated variants from reference library and testing aligners..");

        randomizedSimilarityTest(new SimpleExomeAlignerFactory(),
                PercentRange.createLowerBound("Similarity", "SimpleExomeAligner-Normal", 95));


        randomizedSimilarityTest(new ExtendedExomeAlignerFactory(),
                PercentRange.createLowerBound("Similarity", "ImprovedExomeAligner-Normal", 99));

        System.out.println("Testing aligners for deletions..");

        System.out.println("Simple");
        randomizedIndelTest(new SimpleExomeAlignerFactory(LocalAlignmentEvaluator.FLEXIBLE),
                RandomIndelGenerator.DEFAULT_DELETION,
                PercentRange.createUpperBound("SubstitutionRate", "SimpleExomeAligner-Deletions", 20),
                PercentRange.createLowerBound("AlignmentRate", "SimpleExomeAligner-Deletions", 50));

        System.out.println("Extended");
        randomizedIndelTest(new ExtendedExomeAlignerFactory(LocalAlignmentEvaluator.FLEXIBLE),
                RandomIndelGenerator.DEFAULT_DELETION,
                PercentRange.createUpperBound("SubstitutionRate", "ImprovedExomeAligner-Deletions", 5),
                PercentRange.createLowerBound("AlignmentRate", "ImprovedExomeAligner-Deletions", 90));

        System.out.println("Testing aligners for insertions..");

        System.out.println("Simple");
        randomizedIndelTest(new SimpleExomeAlignerFactory(LocalAlignmentEvaluator.FLEXIBLE),
                RandomIndelGenerator.DEFAULT_INSERTION,
                PercentRange.createUpperBound("SubstitutionRate", "SimpleExomeAligner-Insertions", 20),
                PercentRange.createLowerBound("AlignmentRate", "SimpleExomeAligner-Insertions", 50));

        System.out.println("Extended");
        randomizedIndelTest(new ExtendedExomeAlignerFactory(LocalAlignmentEvaluator.FLEXIBLE),
                RandomIndelGenerator.DEFAULT_INSERTION,
                PercentRange.createUpperBound("SubstitutionRate", "ImprovedExomeAligner-Insertions", 5),
                PercentRange.createLowerBound("AlignmentRate", "ImprovedExomeAligner-Insertions", 90));
    }

    public void randomizedSimilarityTest(AlignerFactory alignerFactory,
                                         PercentRange assertRange) throws URISyntaxException, IOException,
            NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        float similarity = 0;
        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator();
        for (int i = 0; i < nRepetitions1; i++) {
            ReferenceLibrary referenceLibrary = randomReferenceGenerator.nextReferenceLibrary(nReferences);
            Aligner aligner = alignerFactory.fromReferenceLibrary(referenceLibrary);

            for (int j = 0; j < nRepetitions2; j++) {
                NucleotideSequence seq = randomReferenceGenerator.nextMutatedReferenceSequence(referenceLibrary);
                SAlignmentResult result = aligner.align(seq);

                if (result != null)
                    similarity += result.calculateSimilarities().get(0);
            }
        }
        similarity /= nRepetitions2 * nReferences;
        assertRange.assertInRange(similarity);
    }

    public void randomizedIndelTest(AlignerFactory alignerFactory,
                                    RandomIndelGenerator indelGenerator,
                                    PercentRange substitutionRatioAssertRange,
                                    PercentRange percentAlignedAssertRange) {
        int nAligned = 0;
        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator();

        float substitutionRate = 0;
        for (int i = 0; i < nRepetitions1; i++) {
            ReferenceLibrary referenceLibrary = randomReferenceGenerator.nextReferenceLibrary(nReferences);
            Aligner aligner = alignerFactory.fromReferenceLibrary(referenceLibrary);

            for (int j = 0; j < nRepetitions2; j++) {
                NucleotideSequence seq = randomReferenceGenerator.nextReferenceSequence(referenceLibrary);
                NucleotideSequence mutatedSeq = indelGenerator.mutate(seq);

                SAlignmentResult result = aligner.align(mutatedSeq);

                if (result != null) {
                    substitutionRate += result.calculateSubstitutionRatios().get(0);
                    nAligned++;
                }
            }
        }
        substitutionRate /= nRepetitions2 * nReferences;
        substitutionRatioAssertRange.assertInRange(substitutionRate);
        percentAlignedAssertRange.assertInRange(nAligned, nRepetitions2 * nReferences);
    }
}
