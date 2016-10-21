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
import com.antigenomics.mageri.generators.*;
import com.antigenomics.mageri.PercentRangeAssertion;
import com.antigenomics.mageri.core.Mig;
import com.antigenomics.mageri.core.assemble.Consensus;
import com.antigenomics.mageri.core.assemble.SAssembler;
import com.antigenomics.mageri.core.genomic.Reference;
import com.antigenomics.mageri.core.genomic.ReferenceLibrary;
import com.antigenomics.mageri.core.mapping.alignment.Aligner;
import com.antigenomics.mageri.core.mapping.alignment.ExtendedKmerAligner;
import com.antigenomics.mageri.core.mutations.Mutation;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class VariantCallerTest {
    @Test
    @Category(ComplexRandomTests.class)
    public void skewedDistributionTest() {
        System.out.println("Testing identification of somatic mutations and hot-spot errors " +
                "for various hot spot models");
        int qualThreshold = 20;
        String setting = "Skewed, Q" + qualThreshold;

        test(0.5, 1e-3,
                MutationGenerator.NO_INDEL_SKEWED, 1e-3,
                qualThreshold,
                PercentRangeAssertion.createDummy("Specificity", setting),
                PercentRangeAssertion.createDummy("Sensitivity", setting));
    }

    @SuppressWarnings("unchecked")
    public void test(double somaticPositionRatio,
                     double somaticFreq, MutationGenerator pcrErrorModel, double seqErrorFreq,
                     int qualThreshold,
                     PercentRangeAssertion specificityRange,
                     PercentRangeAssertion sensitivityRange) {
        int nMigs = 30000, migSize = 100;

        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator();
        randomReferenceGenerator.setReferenceSizeMin(100);
        randomReferenceGenerator.setReferenceSizeMax(100);

        final ReferenceLibrary referenceLibrary = randomReferenceGenerator.nextReferenceLibrary(1);
        final Reference reference = referenceLibrary.getAt(0);
        final Assembler assembler = new SAssembler();
        final Aligner aligner = new ExtendedKmerAligner(referenceLibrary,
                // we do relax local alignment evaluator as error rates are high:
                ConsensusAlignerParameters.DEFAULT
                        .withMinIdentityRatio(0.7)
                        .withMinAlignedQueryRelativeSpan(0.5));
        ConsensusAligner consensusAligner = new SConsensusAligner(aligner);

        VariantCallerParameters variantCallerParameters = VariantCallerParameters.DEFAULT.withModelOrder(0);
        ModelMigGenerator modelMigGenerator = new ModelMigGenerator(variantCallerParameters, reference, migSize,
                somaticPositionRatio,
                MutationGenerator.getUniform(somaticFreq),
                MutationGenerator.getUniform(seqErrorFreq),
                pcrErrorModel);

        for (int j = 0; j < nMigs; j++) {
            Mig mig = modelMigGenerator.nextMig();
            Consensus consensus = assembler.assemble(mig);
            if (consensus != null) {
                consensusAligner.align(consensus);
            }
        }

        final VariantCaller variantCaller = new VariantCaller(consensusAligner,
                assembler.getMinorCaller(), variantCallerParameters);

        int tp = 0, fp = 0, tn = 0, fn = 0,
                totalSomatic = 0, totalErrors = 0;
        double meanSomaticFreq = 0, meanSomaticFreqDiff = 0, meanErrorFreq = 0;
        double meanSomaticQ = 0, meanErrorQ = 0;

        for (Variant variant : variantCaller.getVariants()) {
            Mutation mutation = variant.getMutation();
            if (mutation instanceof Substitution) {
                Substitution substitution = (Substitution) mutation;
                int code = substitution.getCode();

                double qual = variant.getQual();
                boolean passQual = qual >= qualThreshold;
                double knownFreq = modelMigGenerator.getSomaticFreq(code),
                        varFreq = variant.getAlleleFrequency();

                if (knownFreq > 0) {
                    totalSomatic++;
                    meanSomaticQ += qual;
                    meanSomaticFreq += varFreq;
                    meanSomaticFreqDiff += (varFreq - knownFreq);
                    if (passQual) {
                        tp++;
                    } else {
                        fn++;
                    }
                } else {
                    totalErrors++;
                    meanErrorQ += qual;
                    meanErrorFreq += varFreq;
                    if (passQual) {
                        fp++;
                    } else {
                        tn++;
                    }
                }
            }
        }

        meanSomaticFreq /= totalSomatic;
        meanSomaticFreqDiff /= totalSomatic;
        meanSomaticQ /= totalSomatic;
        meanErrorFreq /= totalErrors;
        meanErrorQ /= totalErrors;

        System.out.println("Found " + totalSomatic + " somatic and " + totalErrors + " erroneous variants.");
        System.out.println("Mean frequency is " + meanSomaticFreq + " and " + meanErrorFreq + " for somatic and erroneous variants.");
        System.out.println("Mean quality is " + meanSomaticQ + " and " + meanErrorQ + " for somatic and erroneous variants.");
        System.out.println("Mean difference between observed and expected frequency of somatic mutaitons is " + meanSomaticFreqDiff + " variants.");

        specificityRange.assertInRange(tn, fp + tn);
        sensitivityRange.assertInRange(tp, tp + fn);
    }
}