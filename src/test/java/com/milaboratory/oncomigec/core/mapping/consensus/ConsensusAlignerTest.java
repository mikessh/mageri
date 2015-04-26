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
package com.milaboratory.oncomigec.core.mapping.consensus;

import com.milaboratory.oncomigec.DoubleRangeAssertion;
import com.milaboratory.oncomigec.PercentRangeAssertion;
import com.milaboratory.oncomigec.core.assemble.*;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.core.input.SMig;
import com.milaboratory.oncomigec.core.mapping.*;
import com.milaboratory.oncomigec.core.mapping.alignment.ExtendedKmerAligner;
import com.milaboratory.oncomigec.generators.GeneratorMutationModel;
import com.milaboratory.oncomigec.generators.RandomMigGenerator;
import com.milaboratory.oncomigec.generators.RandomReferenceGenerator;
import org.junit.Test;

import java.util.Arrays;

public class ConsensusAlignerTest {
    private final int nRepetiotions = 20, nMigs = 100;

    @Test
    public void assemblerDiagnosticsTest() throws Exception {
        String condition;

        RandomMigGenerator randomMigGenerator = new RandomMigGenerator();

        condition = "Reads with indels, default assembler";
        assemblerDiagnosticsTest(randomMigGenerator,
                AssemblerParameters.DEFAULT,
                DoubleRangeAssertion.createLowerBound("MeanCQS", condition, 25.0),
                DoubleRangeAssertion.createLowerBound("MeanUMICoverage", condition, 0.95),
                PercentRangeAssertion.createLowerBound("MeanAlignmentRate", condition, 95));

        condition = "Reads with indels, TORRENT454 assembler";
        assemblerDiagnosticsTest(randomMigGenerator,
                AssemblerParameters.TORRENT454,
                DoubleRangeAssertion.createLowerBound("MeanCQS", condition, 37.0),
                DoubleRangeAssertion.createLowerBound("MeanUMICoverage", condition, 0.90),
                PercentRangeAssertion.createLowerBound("MeanAlignmentRate", condition, 90));

        condition = "Reads without indels, default assembler";
        randomMigGenerator.setGeneratorMutationModel(GeneratorMutationModel.NO_INDEL);
        assemblerDiagnosticsTest(randomMigGenerator,
                AssemblerParameters.DEFAULT,
                DoubleRangeAssertion.createLowerBound("MeanCQS", condition, 39.0),
                DoubleRangeAssertion.createLowerBound("MeanUMICoverage", condition, 1.00),
                PercentRangeAssertion.createLowerBound("MeanAlignmentRate", condition, 100));
    }

    private void assemblerDiagnosticsTest(RandomMigGenerator migGenerator,
                                          AssemblerParameters parameters,
                                          DoubleRangeAssertion meanCqsRange,
                                          DoubleRangeAssertion meanUmiCoverageRange,
                                          PercentRangeAssertion meanAlignmentRateRange) throws Exception {
        final RandomReferenceGenerator referenceGenerator = new RandomReferenceGenerator();
        final int nBins = 20;

        final double[] cqsProfile = new double[nBins], umiCoverageProfile = new double[nBins], binCount = new double[nBins];
        double avgAlignmentRate = 0;

        for (int n = 0; n < nRepetiotions; n++) {
            // No ambiguity for consensusAligner
            ReferenceLibrary library = referenceGenerator.nextReferenceLibrary(1);
            Reference reference = referenceGenerator.nextReference(library);
            Assembler assembler = new SAssembler(parameters);
            ConsensusAligner consensusAligner = new SConsensusAligner(new ExtendedKmerAligner(library));

            int nAlignedConsensuses = 0;
            for (int m = 0; m < nMigs; m++) {
                SMig mig = migGenerator.nextMig(reference).getMig();

                Consensus cons = assembler.assemble(mig);

                if (cons != null) {
                    AlignedConsensus alignedConsensus = consensusAligner.align(cons);

                    if (alignedConsensus.isAligned())
                        nAlignedConsensuses++;
                }
            }

            MutationsTable mutCov = consensusAligner.getAlignerTable(reference);

            int len = reference.getSequence().size();
            for (int i = 0; i < len; i++) {
                int bin = (int) ((i / (double) len) * nBins);
                cqsProfile[bin] += mutCov.getCqsSumCoverage(i);
                umiCoverageProfile[bin] += mutCov.getMigCoverage(i);
                binCount[bin] += nAlignedConsensuses;
            }

            avgAlignmentRate += nAlignedConsensuses / (double) nMigs;


            //System.out.println();
            //System.out.println(consensusAligner.getAlignerReferenceLibrary());
        }

        avgAlignmentRate /= nRepetiotions;

        double meanCqs = 0, meanUmiCoverage = 0;

        for (int i = 0; i < nBins; i++) {
            cqsProfile[i] /= binCount[i];
            meanCqs += cqsProfile[i];
            umiCoverageProfile[i] /= binCount[i];
            meanUmiCoverage += umiCoverageProfile[i];
        }
        meanCqs /= nBins;
        meanUmiCoverage /= nBins;

        System.out.println("UMI coverage by length bin:");
        System.out.println(Arrays.toString(umiCoverageProfile));
        System.out.println("Average CQS by length bin:");
        System.out.println(Arrays.toString(cqsProfile));

        meanAlignmentRateRange.assertInRange(avgAlignmentRate);
        meanCqsRange.assertInRange(meanCqs);
        meanUmiCoverageRange.assertInRange(meanUmiCoverage);
    }

    @Test
    public void exomeConsensusAlignerTest() throws Exception {
        final int nReferences = 50;

        RandomReferenceGenerator referenceGenerator = new RandomReferenceGenerator();
        RandomMigGenerator migGenerator = new RandomMigGenerator();

        int alignmentFails = 0, incorrectAlignments = 0, totalConsensuses = 0;
        for (int n = 0; n < nRepetiotions; n++) {
            ReferenceLibrary library = referenceGenerator.nextReferenceLibrary(nReferences);
            SAssembler assembler = new SAssembler();
            SConsensusAligner consensusAligner = new SConsensusAligner(new ExtendedKmerAligner(library));

            for (int m = 0; m < nMigs; m++) {
                Reference reference = referenceGenerator.nextReference(library);
                SMig mig = migGenerator.nextMig(reference).getMig();

                SConsensus cons = assembler.assemble(mig);

                if (cons != null) {
                    SAlignedConsensus alignedConsensus = (SAlignedConsensus) consensusAligner.align(cons);

                    if (!alignedConsensus.isMapped()) {
                        alignmentFails++;
                    } else if (!alignedConsensus.getAlignmentResult().getReference().equals(reference)) {
                        incorrectAlignments++;
                    }

                    totalConsensuses++;
                }
            }
        }

        System.out.println("Alignment fails=" + PercentRangeAssertion.percent(alignmentFails, totalConsensuses) +
                "%, incorrect alignment=" + PercentRangeAssertion.percent(incorrectAlignments, totalConsensuses) + "%");
    }
}
