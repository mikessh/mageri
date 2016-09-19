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
package com.antigenomics.mageri.core.mapping.alignment;

import com.antigenomics.mageri.PercentRangeAssertion;
import com.antigenomics.mageri.generators.*;
import com.antigenomics.mageri.pipeline.input.ResourceIOProvider;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.antigenomics.mageri.FastTests;
import com.antigenomics.mageri.core.genomic.BasicGenomicInfoProvider;
import com.antigenomics.mageri.core.genomic.ReferenceLibrary;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;

public class AlignerTest {
    private final byte mapqThreshold = 20;

    @Test
    @Category(FastTests.class)
    public void falsePositiveRandomTest() {
        int nReferences = 1000;

        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator();
        randomReferenceGenerator.setReferenceSizeMin(75);
        randomReferenceGenerator.setReferenceSizeMax(75);

        ReferenceLibrary referenceLibrary = randomReferenceGenerator.nextReferenceLibrary(nReferences);

        falsePositiveTest(referenceLibrary, randomReferenceGenerator, 10000);
    }

    @Test
    @Category(FastTests.class)
    public void falsePositivePseudogeneTest() throws IOException {
        ReferenceLibrary referenceLibrary = ReferenceLibrary.fromInput(
                ResourceIOProvider.INSTANCE.getWrappedStream("genomic/cgc_exons_flank50.fa"),
                new BasicGenomicInfoProvider()),
                pseudogeneReferenceLibrary = ReferenceLibrary.fromInput(
                        ResourceIOProvider.INSTANCE.getWrappedStream("genomic/pseudogene.fa"),
                        new BasicGenomicInfoProvider());

        falsePositiveTest(referenceLibrary, new ReferenceLibrarySampler(pseudogeneReferenceLibrary), 1000);
    }

    public void falsePositiveTest(ReferenceLibrary referenceLibrary, 
                                  RandomSequenceGenerator randomSequenceGenerator,
                                  int nRepetitions) {
        AlignerFactory alignerFactory = new ExtendedKmerAlignerFactory(referenceLibrary);
        Aligner aligner = alignerFactory.create();

        int nAligned = 0, nMapqFitlered = 0, nEvaluatorFiltered = 0;

        for (int i = 0; i < nRepetitions; i++) {
            NucleotideSequence query = randomSequenceGenerator.nextSequence();

            AlignmentResult alignmentResult = aligner.align(query);

            if (alignmentResult != null) {
                nAligned++;

                if (!alignmentResult.isGood()) {
                    nEvaluatorFiltered++;
                }
                if (alignmentResult.getScore() < mapqThreshold) {
                    nMapqFitlered++;
                }
            }
        }

        PercentRangeAssertion.createLowerBound("MAPQ threshold filtered filtering", "Aligner false positive test", 95).assertInRange(nMapqFitlered, nAligned);
        PercentRangeAssertion.createLowerBound("Alignment evaluator filtering", "Aligner false positive test", 95).assertInRange(nEvaluatorFiltered, nAligned);
    }

    @Test
    @Category(FastTests.class)
    public void test() {
        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator(),
                randomReferenceGenerator1 = new RandomReferenceGenerator();

        randomReferenceGenerator.setMutationGenerator(MutationGenerator.DEFAULT.multiply(10));
        randomReferenceGenerator1.setMutationGenerator(MutationGenerator.DEFAULT.multiply(10));

        int nReferences = 500, nRepetitions1 = 100, nRepetitions2 = 100;

        int nAligned = 0, nFailed = 0,
                TP = 0, TN = 0, FP = 0, FN = 0;

        for (int i = 0; i < nRepetitions1; i++) {
            ReferenceLibrary referenceLibrary = randomReferenceGenerator.nextHomologousReferenceLibrary(nReferences);

            AlignerFactory alignerFactory = new ExtendedKmerAlignerFactory(referenceLibrary);
            Aligner aligner = alignerFactory.create();

            for (int j = 0; j < nRepetitions2; j++) {
                ReferenceParentChildPair parentChildPair =
                        randomReferenceGenerator1.nextParentChildPair(referenceLibrary);

                NucleotideSequence subject = parentChildPair.getParentSequence(),
                        query = parentChildPair.getChildSequence();

                AlignmentResult alignmentResult = aligner.align(parentChildPair.getChildSequence());

                if (alignmentResult == null) {
                    nFailed++;
                } else {
                    nAligned++;
                    LocalAlignment alignment = alignmentResult.getAlignment();

                    boolean correctAlignment = false;

                    if (alignmentResult.getReference().equals(parentChildPair.getParentReference())) {
                        subject = subject.getRange(alignment.getSequence1Range());
                        query = query.getRange(alignment.getSequence2Range());

                        correctAlignment = Mutations.mutate(subject, alignment.getMutations()).equals(query);
                    }

                    if (correctAlignment) {
                        if (alignmentResult.isGood()) {
                            TP++;
                        } else {
                            FN++;
                        }
                    } else {
                        if (alignmentResult.isGood() && alignmentResult.getScore() > mapqThreshold) {
                            FP++;
                        } else {
                            TN++;
                        }
                    }
                }
            }
        }

        PercentRangeAssertion.createLowerBound("Alignment rate", "Full aligner test", 95).assertInRange(nAligned, nAligned + nFailed);
        PercentRangeAssertion.createLowerBound("Specificity", "Full aligner test", 95).assertInRange(TN, FP + TN);
        PercentRangeAssertion.createLowerBound("Sensitivity", "Full aligner test", 90).assertInRange(TP, TP + FN);
    }
}
