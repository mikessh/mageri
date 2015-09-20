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
import com.milaboratory.mageri.FastTests;
import com.milaboratory.mageri.PercentRangeAssertion;
import com.milaboratory.mageri.core.Mig;
import com.milaboratory.mageri.core.input.SMig;
import com.milaboratory.mageri.core.input.index.QualityProvider;
import com.milaboratory.mageri.core.input.index.Read;
import com.milaboratory.mageri.generators.MigWithMutations;
import com.milaboratory.mageri.generators.MutationGenerator;
import com.milaboratory.mageri.generators.RandomMigGenerator;
import com.milaboratory.mageri.generators.RandomReferenceGenerator;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;

import static com.milaboratory.mageri.generators.RandomUtil.randomSequence;

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
                PercentRangeAssertion.createLowerBound("Reads assembled", mode, 90),
                PercentRangeAssertion.createUpperBound("Reads dropped", mode, 5),
                PercentRangeAssertion.createLowerBound("MIGs assembled", mode, 95),
                PercentRangeAssertion.createUpperBound("MIGs dropped", mode, 1),
                // todo: note indel-proof assembler not implemented yet
                PercentRangeAssertion.createUpperBound("Incorrect consensus", mode, 40),
                false);
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
                false);
    }

    @Test
    @Category(FastTests.class)
    public void randomMutationsPairedIndelsTest() {
        RandomMigGenerator migGenerator = new RandomMigGenerator();

        migGenerator.setMaxRandomFlankSize(5);
        migGenerator.setMutationGenerator(MutationGenerator.DEFAULT);
        String mode = "Paired, With indels";

        randomMutationsTest(migGenerator,
                PercentRangeAssertion.createLowerBound("Reads assembled", mode, 80),
                PercentRangeAssertion.createUpperBound("Reads dropped", mode, 20),
                PercentRangeAssertion.createLowerBound("MIGs assembled", mode, 95),
                PercentRangeAssertion.createUpperBound("MIGs dropped", mode, 1),
                // todo: note indel-proof assembler not implemented yet
                PercentRangeAssertion.createUpperBound("Incorrect consensus", mode, 50),
                true);

        migGenerator.setMutationGenerator(MutationGenerator.NO_INDEL);
        mode = "Paired, No indels";

        randomMutationsTest(migGenerator,
                PercentRangeAssertion.createLowerBound("Reads assembled", mode, 85),
                PercentRangeAssertion.createUpperBound("Reads dropped", mode, 10),
                PercentRangeAssertion.createLowerBound("MIGs assembled", mode, 95),
                PercentRangeAssertion.createUpperBound("MIGs dropped", mode, 1),
                PercentRangeAssertion.createUpperBound("Incorrect consensus", mode, 10),
                true);
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
                true);
    }

    public void randomMutationsTest(RandomMigGenerator migGenerator,
                                    PercentRangeAssertion readAssembly,
                                    PercentRangeAssertion readDropping,
                                    PercentRangeAssertion migAssembly,
                                    PercentRangeAssertion migDropping,
                                    PercentRangeAssertion migIncorrect,
                                    boolean paired) {
        int nRepetitions = 1000;
        RandomReferenceGenerator referenceGenerator = new RandomReferenceGenerator();

        if (paired) {
            referenceGenerator.setReferenceSizeMin(referenceGenerator.getReferenceSizeMin() * 2);
            referenceGenerator.setReferenceSizeMax(referenceGenerator.getReferenceSizeMax() * 2);
        }

        Assembler assembler = paired ? new PAssembler() : new SAssembler();
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
