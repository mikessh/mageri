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
package com.milaboratory.oncomigec.core.assemble.processor;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.oncomigec.core.assemble.entity.Consensus;
import com.milaboratory.oncomigec.core.assemble.entity.SConsensus;
import com.milaboratory.oncomigec.core.io.entity.Mig;
import com.milaboratory.oncomigec.core.io.entity.SMig;
import com.milaboratory.oncomigec.util.Basics;
import com.milaboratory.oncomigec.util.testing.generators.RandomMigGenerator;
import com.milaboratory.oncomigec.util.testing.generators.RandomReferenceGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.milaboratory.oncomigec.util.Util.*;

public class AssemblerTest {
    private static final int nRepetitions = 1000, randomTestAssertThreshold = 70, randomTestAssertIDHThreshold = 60;

    @Test
    public void randomMutationsTest() {
        RandomMigGenerator migGenerator = new RandomMigGenerator();
        RandomReferenceGenerator referenceGenerator = new RandomReferenceGenerator();
        Assembler assembler = new SAssembler();
        Consensus consensus;

        int nAssembled = 0, nDroppedIndelHeavy = 0, nIncorrectIndelHeavy = 0;
        int indelHeavyMigs = 0;
        for (int i = 0; i < nRepetitions; i++) {
            NucleotideSequence core = referenceGenerator.nextSequence();
            RandomMigGenerator.RandomMigGeneratorResult randomMig = migGenerator.nextMig(core);

            boolean indelHeavy = randomMig.indelHeavy();
            if (randomMig.indelHeavy())
                indelHeavyMigs++;

            consensus = assembler.assemble(randomMig.getMig());

            if (consensus != null) {
                String consensusSequence = ((SConsensus) consensus).getConsensusSQPair().getSequence().toString(),
                        coreStr = core.toString();

                // Consensus could be larger or smaller than core due to indels
                if (coreStr.contains(consensusSequence) || consensusSequence.contains(coreStr))
                    nAssembled++;
                else if (indelHeavy)
                    nIncorrectIndelHeavy++;
            } else if (indelHeavy)
                nDroppedIndelHeavy++;
        }

        int percentAssembled = Basics.percent(nAssembled, nRepetitions),
                percentDroppedIndelHeavy = Basics.percent(nDroppedIndelHeavy, indelHeavyMigs),
                percentIncorrectIndelHeavy = Basics.percent(nIncorrectIndelHeavy, indelHeavyMigs);
        System.out.println("Indel heavy MIGs=" + Basics.percent(indelHeavyMigs, nRepetitions) + "%");
        System.out.println("MIGs assembled correctly=" + percentAssembled +
                "%; indel-heavy: dropped=" + percentDroppedIndelHeavy +
                "%, incorrectly assembled=" + percentIncorrectIndelHeavy + "%");
        Assert.assertTrue("At least " + randomTestAssertThreshold + "% MIGs assembled correctly",
                percentAssembled >= randomTestAssertThreshold);
        Assert.assertTrue("At least " + randomTestAssertIDHThreshold + "% indel-heavy MIGs assembled correctly",
                (100 - percentIncorrectIndelHeavy + percentDroppedIndelHeavy) >= randomTestAssertIDHThreshold);
    }

    private static final SSequencingRead
            read1 = createRead(
            "ATAGCAGAAATAAAAGAAAAGATTGGAACTAGTCAGATAGCAGAAATAAAAGAAAAGATTGGAACTAGTCAG",
            "HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH"),
            read2 = createRead(
                    "ATAACAGAAATAAAAGAAAAGATTGGAACTAGTCAGATAGCAGAAATAAAAGAAAAGATTGGAACTAGTCAG",
                    "HHH#HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH"),
            read3 = createRead(
                    "ATAACAGAAATAAAAGAAAAGATTGGAACTAGTCAGATAGCAGAAATAAAAGAAAAGATTGGAACTAGTCAG",
                    "HHHJHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");

    @Test
    public void fixedMutationCasesTest() {
        Assembler assembler = new SAssembler();
        List<SSequencingRead> reads;
        Mig mig;
        Consensus consensus;

        // No mutations
        System.out.println("Testing Assembler. Case: no mutations");
        reads = new ArrayList<>();
        for (int i = 0; i < 30; i++)
            reads.add(cloneRead(read1));
        mig = new SMig(reads, randomSequence(12));
        consensus = (SConsensus) assembler.process(mig).getResult();
        Assert.assertEquals("Correct consensus should be assembled",
                ((SConsensus) consensus).getConsensusSQPair().getSequence(), read1.getData().getSequence());

        // Bad qual mutation, not recorded at all, dont affect consensus
        System.out.println("Testing Assembler. Case: frequent mutation with bad sequencing quality");
        reads = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            reads.add(cloneRead(read1));
        for (int i = 0; i < 20; i++)
            reads.add(cloneRead(read2));
        mig = new SMig(reads, randomSequence(12));
        consensus = (SConsensus) assembler.process(mig).getResult();
        Assert.assertEquals("Correct consensus should be assembled",
                ((SConsensus) consensus).getConsensusSQPair().getSequence(), read1.getData().getSequence());

        // Good qual mutation, rare - recorded in reads only
        System.out.println("Testing Assembler. Case: rare mutation with good sequencing quality");
        reads = new ArrayList<>();
        for (int i = 0; i < 20; i++)
            reads.add(cloneRead(read1));
        for (int i = 0; i < 10; i++)
            reads.add(cloneRead(read3));
        mig = new SMig(reads, randomSequence(12));
        consensus = (SConsensus) assembler.process(mig).getResult();
        Assert.assertEquals("Correct consensus should be assembled",
                ((SConsensus) consensus).getConsensusSQPair().getSequence(), read1.getData().getSequence());

        // Good qual mutation, frequent - recorded both in reads and in consensus
        System.out.println("Testing Assembler. Case: dominating mutation with good sequencing quality");
        reads = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            reads.add(cloneRead(read1));
        for (int i = 0; i < 20; i++)
            reads.add(cloneRead(read3));
        mig = new SMig(reads, randomSequence(12));
        consensus = (SConsensus) assembler.process(mig).getResult();
        Assert.assertEquals("Incorrect consensus should be assembled",
                ((SConsensus) consensus).getConsensusSQPair().getSequence(), read3.getData().getSequence());
    }
}
