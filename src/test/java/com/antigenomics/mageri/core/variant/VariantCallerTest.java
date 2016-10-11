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

package com.antigenomics.mageri.core.variant;

import com.antigenomics.mageri.ComplexRandomTests;
import com.antigenomics.mageri.core.assemble.Assembler;
import com.antigenomics.mageri.core.mapping.*;
import com.antigenomics.mageri.core.mutations.Substitution;
import com.antigenomics.mageri.core.variant.model.ErrorModel;
import com.antigenomics.mageri.core.variant.model.ErrorModelProvider;
import com.antigenomics.mageri.generators.MutationGenerator;
import com.antigenomics.mageri.DoubleRangeAssertion;
import com.antigenomics.mageri.PercentRangeAssertion;
import com.antigenomics.mageri.core.Mig;
import com.antigenomics.mageri.core.assemble.Consensus;
import com.antigenomics.mageri.core.assemble.SAssembler;
import com.antigenomics.mageri.core.genomic.Reference;
import com.antigenomics.mageri.core.genomic.ReferenceLibrary;
import com.antigenomics.mageri.core.mapping.alignment.Aligner;
import com.antigenomics.mageri.core.mapping.alignment.ExtendedKmerAligner;
import com.antigenomics.mageri.core.mutations.Mutation;
import com.antigenomics.mageri.generators.ModelMigGenerator;
import com.antigenomics.mageri.generators.ModelMigGeneratorFactory;
import com.antigenomics.mageri.generators.RandomReferenceGenerator;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class VariantCallerTest {
    @Test
    @Category(ComplexRandomTests.class)
    public void skewedDistributionTest() {
        System.out.println("Testing identification of somatic mutations and hot-spot errors " +
                "for various hot spot models");

        ModelMigGeneratorFactory modelMigGeneratorFactory = new ModelMigGeneratorFactory();
        int qualThreshold = 20;
        String setting = "Skewed, Q" + qualThreshold;

        test(modelMigGeneratorFactory,
                qualThreshold,
                PercentRangeAssertion.createLowerBound("Matching unique variants", setting, 80),
                PercentRangeAssertion.createUpperBound("Erroneous unique variants", setting, 1),
                DoubleRangeAssertion.createUpperBound("Average variant count difference", setting, 0.15),
                PercentRangeAssertion.createLowerBound("Specificity", setting, 90),
                PercentRangeAssertion.createLowerBound("Sensitivity", setting, 85));
    }

    @Test
    @Category(ComplexRandomTests.class)
    public void uniformPositionTest() {
        System.out.println("Testing identification of somatic mutations and hot-spot errors " +
                "for various hot spot models");

        ModelMigGeneratorFactory modelMigGeneratorFactory = new ModelMigGeneratorFactory();
        int qualThreshold = 20;
        String setting = "Uniform position, Q" + qualThreshold;
        modelMigGeneratorFactory.setHotSpotPositionRatio(1.0);
        modelMigGeneratorFactory.setPcrPositionRatio(1.0);

        test(modelMigGeneratorFactory,
                qualThreshold,
                PercentRangeAssertion.createLowerBound("Matching unique variants", setting, 80),
                PercentRangeAssertion.createUpperBound("Erroneous unique variants", setting, 1),
                DoubleRangeAssertion.createUpperBound("Average variant count difference", setting, 0.15),
                PercentRangeAssertion.createLowerBound("Specificity", setting, 90),
                PercentRangeAssertion.createLowerBound("Sensitivity", setting, 85));
    }

    @Test
    @Category(ComplexRandomTests.class)
    public void uniformPositionPatternTest() {
        System.out.println("Testing identification of somatic mutations and hot-spot errors " +
                "for various hot spot models");

        ModelMigGeneratorFactory modelMigGeneratorFactory = new ModelMigGeneratorFactory();
        int qualThreshold = 20;

        String setting = "Uniform position and pattern, Q" + qualThreshold;
        modelMigGeneratorFactory.setPcrErrorGenerator(MutationGenerator.NO_INDEL);

        test(modelMigGeneratorFactory,
                qualThreshold,
                PercentRangeAssertion.createLowerBound("Matching unique variants", setting, 80),
                PercentRangeAssertion.createUpperBound("Erroneous unique variants", setting, 1),
                DoubleRangeAssertion.createUpperBound("Average variant count difference", setting, 0.15),
                PercentRangeAssertion.createLowerBound("Specificity", setting, 90),
                PercentRangeAssertion.createLowerBound("Sensitivity", setting, 85));
    }

    @SuppressWarnings("unchecked")
    public void test(ModelMigGeneratorFactory modelMigGeneratorFactory,
                     int qualThreshold,
                     PercentRangeAssertion matchingVariantsRange,
                     PercentRangeAssertion erroneousVariantsRange,
                     DoubleRangeAssertion countDeltaRange,
                     PercentRangeAssertion specificityRange,
                     PercentRangeAssertion sensitivityRange) {
        int nReferences = 10, nMigs = 20000;

        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator();
        randomReferenceGenerator.setReferenceSizeMin(50);
        randomReferenceGenerator.setReferenceSizeMax(100);

        int finalMigs = 0;
        int expectedVariants = 0, expectedSomatic = 0, expectedHotSpot = 0,
                matchingVariants = 0, observedVariants = 0;
        int tp = 0, fp = 0, tn = 0, fn = 0;
        double variantCountDelta = 0, meanHotSpotQ = 0, meanSomaticQ = 0;

        for (int i = 0; i < nReferences; i++) {
            final ReferenceLibrary referenceLibrary = randomReferenceGenerator.nextReferenceLibrary(1);
            final Reference reference = referenceLibrary.getAt(0);
            final Assembler assembler = new SAssembler();
            final Aligner aligner = new ExtendedKmerAligner(referenceLibrary,
                    // we do relax local alignment evaluator as error rates are high:
                    ConsensusAlignerParameters.DEFAULT
                            .withMinIdentityRatio(0.7)
                            .withMinAlignedQueryRelativeSpan(0.5));

            final ConsensusAligner consensusAligner = new SConsensusAligner(aligner);
            final ModelMigGenerator modelMigGenerator = modelMigGeneratorFactory.create(reference.getSequence());

            for (int j = 0; j < nMigs; j++) {
                Mig mig = modelMigGenerator.nextMig();
                Consensus consensus = assembler.assemble(mig);
                if (consensus != null) {
                    AlignedConsensus alignedConsensus = consensusAligner.align(consensus);
                    if (alignedConsensus.isMapped()) {
                        finalMigs++;
                    }
                }
            }

            expectedVariants += modelMigGenerator.totalSize();

            final VariantCaller variantCaller = new VariantCaller(consensusAligner,
                    assembler.getMinorCaller(), VariantCallerParameters.DEFAULT.withModelOrder(0));

            for (Variant variant : variantCaller.getVariants()) {
                Mutation mutation = variant.getMutation();
                if (mutation instanceof Substitution) {
                    Substitution substitution = (Substitution) mutation;
                    int code = substitution.getCode();
                    //System.out.println(variant + "\t" +
                    //        modelMigGenerator.getHotSpotCount(code) + "\t" +
                    //        modelMigGenerator.getSomaticCount(code));

                    int realCount = modelMigGenerator.getVariantCount(code);

                    if (realCount != 0) {
                        matchingVariants++;
                        variantCountDelta += Math.abs(variant.getCount() - realCount);

                        double qual = variant.getQual();
                        boolean passQual = qual >= qualThreshold;

                        int somaticCount = modelMigGenerator.getSomaticCount(code),
                                hotSpotCount = modelMigGenerator.getHotSpotCount(code);

                        if (somaticCount > 0) {
                            meanSomaticQ += Math.log10(qual);
                            expectedSomatic++;
                        }
                        if (hotSpotCount > 0) {
                            meanHotSpotQ += Math.log10(qual);
                            expectedHotSpot++;
                        }

                        // Can be both somatic and
                        if (somaticCount > hotSpotCount) {
                            if (passQual) {
                                tp++;
                            } else {
                                fn++;
                            }
                        } else {
                            if (passQual) {
                                fp++;
                            } else {
                                tn++;
                            }
                        }
                    }
                    observedVariants++;
                }
            }
        }

        meanSomaticQ /= expectedSomatic;
        meanHotSpotQ /= expectedHotSpot;
        meanSomaticQ = Math.pow(10, meanSomaticQ);
        meanHotSpotQ = Math.pow(10, meanHotSpotQ);

        System.out.println("Processed " + finalMigs + " migs.");
        System.out.println("Generated " + expectedSomatic + " somatic and " + expectedHotSpot + " hot-spot variants.");
        System.out.println("Geom. mean quality is " + meanSomaticQ + " and " + meanHotSpotQ + " for somatic and hot-spot variants.");
        System.out.println("Found " + observedVariants + " variants.");

        matchingVariantsRange.assertInRange(matchingVariants, expectedVariants);
        erroneousVariantsRange.assertInRange(observedVariants - matchingVariants, expectedVariants);
        countDeltaRange.assertInRange(variantCountDelta / matchingVariants);

        specificityRange.assertInRange(tn, fp + tn);
        sensitivityRange.assertInRange(tp, tp + fn);
    }
}