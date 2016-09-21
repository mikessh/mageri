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

package com.antigenomics.mageri.core.assemble;

import com.antigenomics.mageri.core.mapping.alignment.AlignmentScoring;
import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.alignment.AffineGapAlignmentScoring;
import com.milaboratory.core.sequence.alignment.LocalAligner;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.antigenomics.mageri.DoubleRangeAssertion;
import com.antigenomics.mageri.FastTests;
import com.antigenomics.mageri.core.input.PreprocessorParameters;
import com.antigenomics.mageri.core.input.SMig;
import com.antigenomics.mageri.generators.MigWithMutations;
import com.antigenomics.mageri.generators.MutationGenerator;
import com.antigenomics.mageri.generators.RandomMigGenerator;
import com.antigenomics.mageri.generators.RandomReferenceGenerator;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@SuppressWarnings("unchecked")
public class AssemblerModeTest {
    @Test
    public void temp() {
        //                                 *
        //             0000000000111111111122222222223333333333444
        //             0123456789012345678901234567890123456789012
        String seq1 = "AAACAGATCGACTCGATCGTCCGATCCGTACGATCGATTTTTT",
                // 20 - 3 = 17
                seq2 = "ACAGATCACTCGATCGTACGATCCGTACGATCGATTTTTT";


        AffineGapAlignmentScoring scoring = new AlignmentScoring().asInternalScoring();
        LocalAlignment alignment = LocalAligner.align(scoring, new NucleotideSequence(seq1),
                new NucleotideSequence(seq2));

        int mut = alignment.getMutations()[1];

        System.out.println(alignment.getSequence1Range());
        System.out.println(alignment.getSequence2Range());
        System.out.println(alignment.getSequence1Range().getFrom() + Mutations.getPosition(mut));
        System.out.println(Mutations.convertPosition(alignment.getMutations(), Mutations.getPosition(mut)));
    }


    @Test
    @Category(FastTests.class)
    public void assemblerDiagnosticsNoIndelTest() throws Exception {
        String condition;

        RandomMigGenerator randomMigGenerator = new RandomMigGenerator();

        condition = "Reads without indels, default assembler";
        randomMigGenerator.setMutationGenerator(MutationGenerator.NO_INDEL);
        assemblerDiagnosticsTest(randomMigGenerator,
                AssemblerParameters.DEFAULT,
                DoubleRangeAssertion.createLowerBound("Mean CQS", condition, 35),
                DoubleRangeAssertion.createLowerBound("Mean coverage of expected consensus", condition, 0.99),
                DoubleRangeAssertion.createLowerBound("MIGs assembled", condition, 0.99));
    }

    @Test
    @Category(FastTests.class)
    public void assemblerDiagnosticsIndelDefaultTest() throws Exception {
        String condition;

        RandomMigGenerator randomMigGenerator = new RandomMigGenerator();

        condition = "Reads with indels, default assembler";
        assemblerDiagnosticsTest(randomMigGenerator,
                AssemblerParameters.DEFAULT,
                DoubleRangeAssertion.createDummy("Mean CQS", condition),
                DoubleRangeAssertion.createDummy("Mean coverage of expected consensus", condition),
                DoubleRangeAssertion.createLowerBound("MIGs assembled", condition, 0.99));
    }

    @Test
    @Category(FastTests.class)
    public void assemblerDiagnosticsIndelT454Test() throws Exception {
        String condition;

        RandomMigGenerator randomMigGenerator = new RandomMigGenerator();

        randomMigGenerator.setMutationGenerator(MutationGenerator.NO_INDEL);

        condition = "Reads without indels, TORRENT454 assembler";
        assemblerDiagnosticsTest(randomMigGenerator,
                AssemblerParameters.DEFAULT,
                DoubleRangeAssertion.createLowerBound("MeanCQS", condition, 35),
                DoubleRangeAssertion.createLowerBound("Mean coverage of expected consensus", condition, 0.99),
                DoubleRangeAssertion.createLowerBound("MIGs assembled", condition, 0.99));

        randomMigGenerator.setMutationGenerator(MutationGenerator.DEFAULT);
        randomMigGenerator.setMigSizeMin(10);
        randomMigGenerator.setMigSizeMin(30);

        condition = "Reads with indels, TORRENT454 assembler";
        assemblerDiagnosticsTest(randomMigGenerator,
                AssemblerParameters.TORRENT454,
                DoubleRangeAssertion.createLowerBound("Mean CQS", condition, 30),
                DoubleRangeAssertion.createLowerBound("Mean coverage of expected consensus", condition, 0.80),
                DoubleRangeAssertion.createLowerBound("MIGs assembled", condition, 0.99));
    }

    private void assemblerDiagnosticsTest(RandomMigGenerator migGenerator,
                                          AssemblerParameters parameters,
                                          DoubleRangeAssertion meanCqsRange,
                                          DoubleRangeAssertion meanUmiCoverageRange,
                                          DoubleRangeAssertion migsAssembledrange) throws Exception {
        int nRepetitions = 1000;
        RandomReferenceGenerator referenceGenerator = new RandomReferenceGenerator();
        SAssembler assembler = new SAssembler(PreprocessorParameters.IGNORE_QUAL, // don't care abt minors here
                parameters);
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
        migsAssembledrange.assertInRange(migsAssembled / (double) nRepetitions);
    }
}
