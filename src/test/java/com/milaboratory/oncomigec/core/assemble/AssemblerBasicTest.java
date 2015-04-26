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
package com.milaboratory.oncomigec.core.assemble;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.PercentRangeAssertion;
import com.milaboratory.oncomigec.core.Mig;
import com.milaboratory.oncomigec.core.input.SMig;
import com.milaboratory.oncomigec.core.input.index.QualityProvider;
import com.milaboratory.oncomigec.core.input.index.Read;
import com.milaboratory.oncomigec.generators.GeneratorMutationModel;
import com.milaboratory.oncomigec.generators.MigWithMutations;
import com.milaboratory.oncomigec.generators.RandomMigGenerator;
import com.milaboratory.oncomigec.generators.RandomReferenceGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.milaboratory.oncomigec.misc.Util.randomSequence;

@SuppressWarnings("unchecked")
public class AssemblerBasicTest {
    @Test
    public void randomMutationsTest() {
        RandomMigGenerator migGenerator = new RandomMigGenerator();

        migGenerator.setMaxRandomFlankSize(5);

        String mode = "With indels";

        randomMutationsTest(migGenerator,
                PercentRangeAssertion.createLowerBound("Reads assembled", mode, 90),
                PercentRangeAssertion.createLowerBound("MIGs assembled", mode, 95),
                PercentRangeAssertion.createUpperBound("MIGs dropped", mode, 1),
                PercentRangeAssertion.createUpperBound("Incorrect consensus", mode, 20));

        migGenerator.setGeneratorMutationModel(GeneratorMutationModel.NO_INDEL);
        mode = "No indels";

        randomMutationsTest(migGenerator,
                PercentRangeAssertion.createLowerBound("Reads assembled", mode, 95),
                PercentRangeAssertion.createLowerBound("MIGs assembled", mode, 95),
                PercentRangeAssertion.createUpperBound("MIGs dropped", mode, 1),
                PercentRangeAssertion.createUpperBound("Incorrect consensus", mode, 5));
    }

    public void randomMutationsTest(RandomMigGenerator migGenerator,
                                    PercentRangeAssertion readAssembly,
                                    PercentRangeAssertion migAssembly,
                                    PercentRangeAssertion migDropping,
                                    PercentRangeAssertion migIncorrect) {
        int nRepetitions = 1000;
        RandomReferenceGenerator referenceGenerator = new RandomReferenceGenerator();
        SAssembler assembler = new SAssembler();
        SConsensus consensus;

        int readsTotal = 0, readsAssembled = 0,
                migsTotal = 0, migsAssembled = 0, migsDropped = 0, migsIncorrectlyAssembled = 0;

        for (int i = 0; i < nRepetitions; i++) {
            NucleotideSequence core = referenceGenerator.nextSequence();
            MigWithMutations randomMig = migGenerator.nextMig(core);

            SMig mig = randomMig.getMig();

            migsTotal++;

            consensus = assembler.assemble(mig);

            if (consensus != null) {
                String consensusSequence = consensus.getConsensusSQPair().getSequence().toString(),
                        coreStr = core.toString();

                readsAssembled += consensus.getAssembledSize();
                readsTotal += consensus.getTrueSize();

                migsAssembled++;

                // Consensus could be larger or smaller than core due to indels
                if (!coreStr.contains(consensusSequence) && !consensusSequence.contains(coreStr)) {
                    migsIncorrectlyAssembled++;
                }
            } else {
                migsDropped++;
            }
        }

        readAssembly.assertInRange(readsAssembled, readsTotal);
        migAssembly.assertInRange(migsAssembled, migsTotal);
        migDropping.assertInRange(migsDropped, migsTotal);
        migIncorrect.assertInRange(migsIncorrectlyAssembled, migsTotal);
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
    public void fixedMutationCasesTest() {
        Assembler assembler = new SAssembler();
        List<Read> reads;
        Mig mig;
        Consensus consensus;

        // No mutations
        System.out.println("Testing Assembler. Case: no mutations");
        reads = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            reads.add(new Read(read1, qualityProvider));
        }
        mig = new SMig(null, randomSequence(12), reads);
        consensus = (SConsensus) assembler.process(mig).getResult();
        Assert.assertEquals("Correct consensus should be assembled",
                ((SConsensus) consensus).getConsensusSQPair().getSequence(), read1.getSequence());

        // Bad qual mutation, not recorded at all, dont affect consensus
        System.out.println("Testing Assembler. Case: frequent mutation with bad sequencing quality");
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
        System.out.println("Testing Assembler. Case: rare mutation with good sequencing quality");
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
        System.out.println("Testing Assembler. Case: dominating mutation with good sequencing quality");
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
