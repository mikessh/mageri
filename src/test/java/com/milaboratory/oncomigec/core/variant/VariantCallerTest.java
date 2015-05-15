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

package com.milaboratory.oncomigec.core.variant;

import com.milaboratory.oncomigec.ComplexRandomTests;
import com.milaboratory.oncomigec.DoubleRangeAssertion;
import com.milaboratory.oncomigec.PercentRangeAssertion;
import com.milaboratory.oncomigec.core.Mig;
import com.milaboratory.oncomigec.core.assemble.Assembler;
import com.milaboratory.oncomigec.core.assemble.Consensus;
import com.milaboratory.oncomigec.core.assemble.SAssembler;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.core.mapping.AlignedConsensus;
import com.milaboratory.oncomigec.core.mapping.ConsensusAligner;
import com.milaboratory.oncomigec.core.mapping.SConsensusAligner;
import com.milaboratory.oncomigec.core.mapping.alignment.Aligner;
import com.milaboratory.oncomigec.core.mapping.alignment.ExtendedKmerAligner;
import com.milaboratory.oncomigec.core.mutations.Mutation;
import com.milaboratory.oncomigec.core.mutations.Substitution;
import com.milaboratory.oncomigec.generators.ModelMigGenerator;
import com.milaboratory.oncomigec.generators.ModelMigGeneratorFactory;
import com.milaboratory.oncomigec.generators.MutationGenerator;
import com.milaboratory.oncomigec.generators.RandomReferenceGenerator;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class VariantCallerTest {
    @Test
    @Category(ComplexRandomTests.class)
    public void test() {
        System.out.println("Testing identification of somatic mutations and hot-spot errors " +
                "for various hot spot models");

        ModelMigGeneratorFactory modelMigGeneratorFactory = new ModelMigGeneratorFactory();
        int qualThreshold = 25;
        String setting = "Skewed, Q" + qualThreshold;

        test(modelMigGeneratorFactory,
                qualThreshold,
                PercentRangeAssertion.createLowerBound("Matching unique variants", setting, 95),
                PercentRangeAssertion.createUpperBound("Erroneous unique variants", setting, 5),
                DoubleRangeAssertion.createUpperBound("Average variant count difference", setting, 0.05),
                PercentRangeAssertion.createLowerBound("Specificity", setting, 95),
                PercentRangeAssertion.createLowerBound("Sensitivity", setting, 90));

        qualThreshold = 25;
        setting = "Uniform position, Q" + qualThreshold;
        modelMigGeneratorFactory.setHotSpotPositionRatio(1.0);
        modelMigGeneratorFactory.setPcrPositionRatio(1.0);

        test(modelMigGeneratorFactory,
                qualThreshold,
                PercentRangeAssertion.createLowerBound("Matching unique variants", setting, 90),
                PercentRangeAssertion.createUpperBound("Erroneous unique variants", setting, 1),
                DoubleRangeAssertion.createUpperBound("Average variant count difference", setting, 0.05),
                PercentRangeAssertion.createLowerBound("Specificity", setting, 95),
                PercentRangeAssertion.createLowerBound("Sensitivity", setting, 85));

        qualThreshold = 25;
        setting = "Uniform position and pattern, Q" + qualThreshold;
        modelMigGeneratorFactory.setPcrErrorGenerator(MutationGenerator.NO_INDEL);

        test(modelMigGeneratorFactory,
                qualThreshold,
                PercentRangeAssertion.createLowerBound("Matching unique variants", setting, 90),
                PercentRangeAssertion.createUpperBound("Erroneous unique variants", setting, 1),
                DoubleRangeAssertion.createUpperBound("Average variant count difference", setting, 0.01),
                PercentRangeAssertion.createLowerBound("Specificity", setting, 95),
                PercentRangeAssertion.createLowerBound("Sensitivity", setting, 80));
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
        randomReferenceGenerator.setReferenceSizeMax(50);

        int finalMigs = 0;
        int expectedVariants = 0, expectedSomatic = 0, expectedHotSpot = 0,
                matchingVariants = 0, observedVariants = 0;
        int tp = 0, fp = 0, tn = 0, fn = 0;
        double variantCountDelta = 0, meanHotSpotQ = 0, meanSomaticQ = 0;

        for (int i = 0; i < nReferences; i++) {
            final ReferenceLibrary referenceLibrary = randomReferenceGenerator.nextReferenceLibrary(1);
            final Reference reference = referenceLibrary.getAt(0);
            final Assembler assembler = new SAssembler();
            final Aligner aligner = new ExtendedKmerAligner(referenceLibrary);
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

            final VariantCaller variantCaller = new VariantCaller(consensusAligner);

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
                            meanSomaticQ += qual;
                            expectedSomatic++;
                        }
                        if (hotSpotCount > 0) {
                            meanHotSpotQ += qual;
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

        System.out.println("Processed " + finalMigs + " migs.");
        System.out.println("Generated " + expectedSomatic + " somatic and " + expectedHotSpot + " hot-spot variants.");
        System.out.println("Mean quality is " + meanSomaticQ + " and " + meanHotSpotQ + " for somatic and hot-spot variants.");
        System.out.println("Found " + observedVariants + " variants.");

        matchingVariantsRange.assertInRange(matchingVariants, expectedVariants);
        erroneousVariantsRange.assertInRange(observedVariants - matchingVariants, expectedVariants);
        countDeltaRange.assertInRange(variantCountDelta / matchingVariants);

        specificityRange.assertInRange(tn, fp + tn);
        sensitivityRange.assertInRange(tp, tp + fn);
    }
}