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
 * Last modified on 25.4.2015 by mikesh
 */

package com.milaboratory.oncomigec.core.assemble;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.DoubleRangeAssertion;
import com.milaboratory.oncomigec.core.input.SMig;
import com.milaboratory.oncomigec.generators.MutationGenerator;
import com.milaboratory.oncomigec.generators.MigWithMutations;
import com.milaboratory.oncomigec.generators.RandomMigGenerator;
import com.milaboratory.oncomigec.generators.RandomReferenceGenerator;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class AssemblerModeTest {
    @Test
    public void assemblerDiagnosticsTest() throws Exception {
        String condition;

        RandomMigGenerator randomMigGenerator = new RandomMigGenerator();

        condition = "Reads with indels, default assembler";
        assemblerDiagnosticsTest(randomMigGenerator,
                AssemblerParameters.DEFAULT,
                DoubleRangeAssertion.createDummy("MeanCQS", condition),
                DoubleRangeAssertion.createDummy("MeanUMICoverage", condition));

        System.out.println("[Indel-compatible assembler not implemented yet: No read recovery mode!]");
        condition = "Reads with indels, TORRENT454 assembler";
        assemblerDiagnosticsTest(randomMigGenerator,
                AssemblerParameters.TORRENT454,
                DoubleRangeAssertion.createDummy("MeanCQS", condition),
                DoubleRangeAssertion.createDummy("MeanUMICoverage", condition));

        condition = "Reads without indels, default assembler";
        randomMigGenerator.setMutationGenerator(MutationGenerator.NO_INDEL);
        assemblerDiagnosticsTest(randomMigGenerator,
                AssemblerParameters.DEFAULT,
                DoubleRangeAssertion.createLowerBound("MeanCQS", condition, 35),
                DoubleRangeAssertion.createLowerBound("MeanUMICoverage", condition, 0.99));
    }

    private void assemblerDiagnosticsTest(RandomMigGenerator migGenerator,
                                          AssemblerParameters parameters,
                                          DoubleRangeAssertion meanCqsRange,
                                          DoubleRangeAssertion meanUmiCoverageRange) throws Exception {
        int nRepetitions = 1000;
        RandomReferenceGenerator referenceGenerator = new RandomReferenceGenerator();
        SAssembler assembler = new SAssembler(parameters);
        SConsensus consensus;

        int migsAssembled = 0;
        double meanMinCqs = 0, meanCoverage = 0;

        for (int i = 0; i < nRepetitions; i++) {
            NucleotideSequence core = referenceGenerator.nextSequence();
            MigWithMutations randomMig = migGenerator.nextMig(core);

            SMig mig = randomMig.getSMig();

            consensus = assembler.assemble(mig);

            if (consensus != null) {
                NucleotideSQPair consensusSQPair = consensus.getConsensusSQPair();
                String consensusSequence = consensusSQPair.getSequence().toString(),
                        coreStr = core.toString();

                migsAssembled++;

                meanMinCqs += consensusSQPair.getQuality().minValue();

                // Consensus could be larger or smaller than core due to indels
                if (coreStr.contains(consensusSequence) || consensusSequence.contains(coreStr)) {
                    meanCoverage += Math.min(consensusSequence.length(), coreStr.length()) / (double) coreStr.length();
                }
            }
        }

        meanCqsRange.assertInRange(meanMinCqs / (double) migsAssembled);
        meanUmiCoverageRange.assertInRange(meanCoverage / (double) migsAssembled);
    }
}
