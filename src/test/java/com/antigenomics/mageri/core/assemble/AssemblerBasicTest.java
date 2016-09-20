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

import com.antigenomics.mageri.PercentRangeAssertion;
import com.antigenomics.mageri.core.input.PreprocessorParameters;
import com.antigenomics.mageri.core.input.index.QualityProvider;
import com.antigenomics.mageri.core.input.index.Read;
import com.antigenomics.mageri.generators.MigWithMutations;
import com.antigenomics.mageri.generators.MutationGenerator;
import com.antigenomics.mageri.generators.RandomMigGenerator;
import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.antigenomics.mageri.FastTests;
import com.antigenomics.mageri.core.Mig;
import com.antigenomics.mageri.core.input.SMig;
import com.antigenomics.mageri.generators.RandomReferenceGenerator;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;

import static com.antigenomics.mageri.generators.RandomUtil.randomSequence;

@SuppressWarnings("unchecked")
public class AssemblerBasicTest {
    @Test
    @Ignore("TODO")
    public void parallelTest() {
    }

    @Test
    @Category(FastTests.class)
    public void randomMutationsSingleIndelTest() {
        RandomMigGenerator migGenerator = new RandomMigGenerator();

        migGenerator.setMaxRandomFlankSize(5);

        String mode = "Single, With indels";

        randomMutationsTest(migGenerator,
                PercentRangeAssertion.createLowerBound("Reads assembled", mode, 95),
                PercentRangeAssertion.createUpperBound("Reads dropped", mode, 5),
                PercentRangeAssertion.createLowerBound("MIGs assembled", mode, 95),
                PercentRangeAssertion.createUpperBound("MIGs dropped", mode, 1),
                PercentRangeAssertion.createUpperBound("Incorrect consensus", mode, 20),
                false, true);
    }

    @Test
    @Category(FastTests.class)
    public void randomMutationsSingleNoIndelTest() {
        RandomMigGenerator migGenerator = new RandomMigGenerator();

        migGenerator.setMaxRandomFlankSize(5);
        migGenerator.setMutationGenerator(MutationGenerator.NO_INDEL);
        String mode = "Single, No indels";

        randomMutationsTest(migGenerator,
                PercentRangeAssertion.createLowerBound("Reads assembled", mode, 95),
                PercentRangeAssertion.createUpperBound("Reads dropped", mode, 5),
                PercentRangeAssertion.createLowerBound("MIGs assembled", mode, 95),
                PercentRangeAssertion.createUpperBound("MIGs dropped", mode, 1),
                PercentRangeAssertion.createUpperBound("Incorrect consensus", mode, 1),
                false, false);
    }

    @Test
    @Category(FastTests.class)
    public void randomMutationsPairedIndelsTest() {
        RandomMigGenerator migGenerator = new RandomMigGenerator();

        migGenerator.setMaxRandomFlankSize(5);
        migGenerator.setMutationGenerator(MutationGenerator.DEFAULT);
        String mode = "Paired, With indels";

        randomMutationsTest(migGenerator,
                PercentRangeAssertion.createLowerBound("Reads assembled", mode, 95),
                PercentRangeAssertion.createUpperBound("Reads dropped", mode, 5),
                PercentRangeAssertion.createLowerBound("MIGs assembled", mode, 95),
                PercentRangeAssertion.createUpperBound("MIGs dropped", mode, 1),
                PercentRangeAssertion.createUpperBound("Incorrect consensus", mode, 30),
                true, true);

        migGenerator.setMutationGenerator(MutationGenerator.NO_INDEL);
        mode = "Paired, No indels";

        randomMutationsTest(migGenerator,
                PercentRangeAssertion.createLowerBound("Reads assembled", mode, 85),
                PercentRangeAssertion.createUpperBound("Reads dropped", mode, 10),
                PercentRangeAssertion.createLowerBound("MIGs assembled", mode, 95),
                PercentRangeAssertion.createUpperBound("MIGs dropped", mode, 1),
                PercentRangeAssertion.createUpperBound("Incorrect consensus", mode, 10),
                true, false);
    }

    @Test
    @Category(FastTests.class)
    public void randomMutationsPairedNoIndelsTest() {
        RandomMigGenerator migGenerator = new RandomMigGenerator();

        migGenerator.setMaxRandomFlankSize(5);
        migGenerator.setMutationGenerator(MutationGenerator.NO_INDEL);
        String mode = "Paired, No indels";

        randomMutationsTest(migGenerator,
                PercentRangeAssertion.createLowerBound("Reads assembled", mode, 85),
                PercentRangeAssertion.createUpperBound("Reads dropped", mode, 10),
                PercentRangeAssertion.createLowerBound("MIGs assembled", mode, 95),
                PercentRangeAssertion.createUpperBound("MIGs dropped", mode, 1),
                PercentRangeAssertion.createUpperBound("Incorrect consensus", mode, 10),
                true, false);
    }

    public void randomMutationsTest(RandomMigGenerator migGenerator,
                                    PercentRangeAssertion readAssembly,
                                    PercentRangeAssertion readDropping,
                                    PercentRangeAssertion migAssembly,
                                    PercentRangeAssertion migDropping,
                                    PercentRangeAssertion migIncorrect,
                                    boolean paired, boolean withIndels) {
        int nRepetitions = 1000;
        RandomReferenceGenerator referenceGenerator = new RandomReferenceGenerator();

        if (paired) {
            referenceGenerator.setReferenceSizeMin(referenceGenerator.getReferenceSizeMin() * 2);
            referenceGenerator.setReferenceSizeMax(referenceGenerator.getReferenceSizeMax() * 2);
        }

        PreprocessorParameters preprocessorParameters = PreprocessorParameters.DEFAULT;
        AssemblerParameters assemblerParameters = withIndels ? AssemblerParameters.TORRENT454 : AssemblerParameters.DEFAULT;

        Assembler assembler = paired ? new PAssembler(preprocessorParameters, assemblerParameters) :
                new SAssembler(preprocessorParameters, assemblerParameters);
        Consensus consensus;

        int migsTotal = 0, migsAssembled = 0, migsDropped = 0, migsIncorrectlyAssembled = 0;

        for (int i = 0; i < nRepetitions; i++) {
            NucleotideSequence core = referenceGenerator.nextSequence();
            MigWithMutations randomMig = migGenerator.nextMig(core);

            Mig mig = paired ? randomMig.getPMig() : randomMig.getSMig();

            migsTotal++;

            consensus = assembler.assemble(mig);

            if (consensus != null) {
                String coreStr = core.toString();

                migsAssembled++;

                // Consensus could be larger or smaller than core due to indels
                if (incorrectAssembly(coreStr, consensus, paired)) {
                    migsIncorrectlyAssembled++;
                }
            } else {
                migsDropped++;
            }
        }

        readAssembly.assertInRange(assembler.getReadsAssembled(), assembler.getReadsTotal());
        readDropping.assertInRange(assembler.getReadsDroppedErrorR1(), assembler.getReadsTotal());
        readDropping.assertInRange(assembler.getReadsDroppedShortR1(), assembler.getReadsTotal());
        readDropping.assertInRange(assembler.getReadsDroppedErrorR2(), assembler.getReadsTotal());
        readDropping.assertInRange(assembler.getReadsDroppedShortR2(), assembler.getReadsTotal());
        migAssembly.assertInRange(migsAssembled, migsTotal);
        migDropping.assertInRange(migsDropped, migsTotal);
        migIncorrect.assertInRange(migsIncorrectlyAssembled, migsTotal);
    }

    private static boolean incorrectAssembly(String coreStr, Consensus consensus, boolean paired) {
        return paired ? incorrectAssembly(coreStr, ((PConsensus) consensus).getConsensus1(),
                ((PConsensus) consensus).getConsensus2()) : incorrectAssembly(coreStr, (SConsensus) consensus);

    }

    private static boolean incorrectAssembly(String coreStr, SConsensus... consensuses) {
        for (SConsensus consensus : consensuses) {
            String consensusSequence = consensus.getConsensusSQPair().getSequence().toString();
            if (!coreStr.contains(consensusSequence) && !consensusSequence.contains(coreStr)) {
                return true;
            }
        }
        return false;
    }

    private static final QualityProvider qualityProvider = new QualityProvider((byte) 30);
    private static final NucleotideSQPair
            read1 = new NucleotideSQPair(
            "ATAGCAGAAATAAAAGAAAAGATTGGAACTAGTCAGATAGCAGAAATAAAAGAAAAGATTGGAACTAGTCAG",
            "HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH"),
            read2 = new NucleotideSQPair(
                    "ATAACAGAAATAAAAGAAAAGATTGGAACTAGTCAGATAGCAGAAATAAAAGAAAAGATTGGAACTAGTCAG",
                    "HHH#HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH"),
            read3 = new NucleotideSQPair(
                    "ATAACAGAAATAAAAGAAAAGATTGGAACTAGTCAGATAGCAGAAATAAAAGAAAAGATTGGAACTAGTCAG",
                    "HHHJHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");

    @Test
    @Category(FastTests.class)
    public void fixedMutationCasesTest() {
        fixedMutationCasesTest("DEFAULT", new SAssembler(PreprocessorParameters.DEFAULT, AssemblerParameters.DEFAULT));
        fixedMutationCasesTest("TORRENT454", new SAssembler(PreprocessorParameters.DEFAULT, AssemblerParameters.TORRENT454));
    }

    public void fixedMutationCasesTest(String presetName, Assembler assembler) {
        List<Read> reads;
        Mig mig;
        Consensus consensus;

        // No mutations
        System.out.println("Testing " + presetName + " Assembler. Case: no mutations");
        reads = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            reads.add(new Read(read1, qualityProvider));
        }
        mig = new SMig(null, randomSequence(12), reads);
        consensus = (SConsensus) assembler.process(mig).getResult();
        Assert.assertEquals("Correct consensus should be assembled",
                ((SConsensus) consensus).getConsensusSQPair().getSequence(), read1.getSequence());

        // Bad qual mutation, not recorded at all, dont affect consensus
        System.out.println("Testing " + presetName + " Assembler. Case: frequent mutation with bad sequencing quality");
        reads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            reads.add(new Read(read1, qualityProvider));
        }
        for (int i = 0; i < 20; i++) {
            reads.add(new Read(read2, qualityProvider));
        }
        mig = new SMig(null, randomSequence(12), reads);
        consensus = (SConsensus) assembler.process(mig).getResult();
        Assert.assertEquals("Correct consensus should be assembled",
                ((SConsensus) consensus).getConsensusSQPair().getSequence(), read1.getSequence());

        // Good qual mutation, rare - recorded in reads only
        System.out.println("Testing " + presetName + " Assembler. Case: rare mutation with good sequencing quality");
        reads = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            reads.add(new Read(read1, qualityProvider));
        }
        for (int i = 0; i < 10; i++) {
            reads.add(new Read(read3, qualityProvider));
        }
        mig = new SMig(null, randomSequence(12), reads);
        consensus = (SConsensus) assembler.process(mig).getResult();
        Assert.assertEquals("Correct consensus should be assembled",
                ((SConsensus) consensus).getConsensusSQPair().getSequence(), read1.getSequence());

        // Good qual mutation, frequent - recorded both in reads and in consensus
        System.out.println("Testing " + presetName + " Assembler. Case: dominating mutation with good sequencing quality");
        reads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            reads.add(new Read(read1, qualityProvider));
        }
        for (int i = 0; i < 20; i++) {
            reads.add(new Read(read3, qualityProvider));
        }
        mig = new SMig(null, randomSequence(12), reads);
        consensus = (SConsensus) assembler.process(mig).getResult();
        Assert.assertEquals("Incorrect consensus should be assembled",
                ((SConsensus) consensus).getConsensusSQPair().getSequence(), read3.getSequence());
    }
}
