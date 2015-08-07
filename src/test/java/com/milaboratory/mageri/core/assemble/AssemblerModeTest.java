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

package com.milaboratory.mageri.core.assemble;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.mageri.DoubleRangeAssertion;
import com.milaboratory.mageri.FastTests;
import com.milaboratory.mageri.core.input.PreprocessorParameters;
import com.milaboratory.mageri.core.input.SMig;
import com.milaboratory.mageri.generators.MigWithMutations;
import com.milaboratory.mageri.generators.MutationGenerator;
import com.milaboratory.mageri.generators.RandomMigGenerator;
import com.milaboratory.mageri.generators.RandomReferenceGenerator;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@SuppressWarnings("unchecked")
public class AssemblerModeTest {
    @Test
    @Category(FastTests.class)
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
    }
}
