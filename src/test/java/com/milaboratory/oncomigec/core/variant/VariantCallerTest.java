/*
 * Copyright 2013-2015 Mikhail Shugay (mikhail.shugay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last modified on 2.5.2015 by mikesh
 */

package com.milaboratory.oncomigec.core.variant;

import com.milaboratory.oncomigec.DoubleRangeAssertion;
import com.milaboratory.oncomigec.PercentRangeAssertion;
import com.milaboratory.oncomigec.core.Mig;
import com.milaboratory.oncomigec.core.assemble.Assembler;
import com.milaboratory.oncomigec.core.assemble.Consensus;
import com.milaboratory.oncomigec.core.assemble.SAssembler;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.core.mapping.ConsensusAligner;
import com.milaboratory.oncomigec.core.mapping.SConsensusAligner;
import com.milaboratory.oncomigec.core.mapping.alignment.Aligner;
import com.milaboratory.oncomigec.core.mapping.alignment.ExtendedKmerAligner;
import com.milaboratory.oncomigec.core.mutations.Mutation;
import com.milaboratory.oncomigec.core.mutations.Substitution;
import com.milaboratory.oncomigec.generators.ModelMigGenerator;
import com.milaboratory.oncomigec.generators.RandomReferenceGenerator;
import org.junit.Test;

public class VariantCallerTest {
    @Test
    @SuppressWarnings("unchecked")
    public void test() {
        int nReferences = 10, nMigs = 10000, qualThreshold = 20;

        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator();
        randomReferenceGenerator.setReferenceSizeMin(100);
        randomReferenceGenerator.setReferenceSizeMax(100);

        int expectedVariants = 0, matchingVariants = 0, observedVariants = 0;
        int tp = 0, fp = 0, tn = 0, fn = 0;
        double variantCountDelta = 0;

        for (int i = 0; i < nReferences; i++) {
            ReferenceLibrary referenceLibrary = randomReferenceGenerator.nextReferenceLibrary(1);
            Reference reference = referenceLibrary.getAt(0);
            Assembler assembler = new SAssembler();
            Aligner aligner = new ExtendedKmerAligner(referenceLibrary);
            ConsensusAligner consensusAligner = new SConsensusAligner(aligner);
            ModelMigGenerator modelMigGenerator = new ModelMigGenerator(reference.getSequence());

            for (int j = 0; j < nMigs; j++) {
                Mig mig = modelMigGenerator.nextMig();
                Consensus consensus = assembler.assemble(mig);
                consensusAligner.align(consensus);
            }

            expectedVariants += modelMigGenerator.totalSize();

            VariantCaller variantCaller = new VariantCaller(consensusAligner);

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

                        // Can be both somatic and 

                        boolean passQual = variant.getQual() >= qualThreshold;

                        if (modelMigGenerator.getSomaticCount(code) >
                                modelMigGenerator.getHotSpotCount(code)) {
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

        PercentRangeAssertion.createDummy("Matching unique variants", "").
                assertInRange(matchingVariants, expectedVariants);
        PercentRangeAssertion.createDummy("Erroneous unique variants", "").
                assertInRange(observedVariants - matchingVariants, expectedVariants);
        DoubleRangeAssertion.createDummy("Average variant count difference", "").
                assertInRange(variantCountDelta / matchingVariants);

        PercentRangeAssertion.createDummy("Specificity", "Minor recovery").
                assertInRange(tn, fp + tn);
        PercentRangeAssertion.createDummy("Sensitivity", "Minor recovery").
                assertInRange(tp, tp + fn);
    }
}