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
package com.milaboratory.mageri.core.mapping;

import com.milaboratory.mageri.DoubleRangeAssertion;
import com.milaboratory.mageri.FastTests;
import com.milaboratory.mageri.PercentRangeAssertion;
import com.milaboratory.mageri.core.Mig;
import com.milaboratory.mageri.core.assemble.Assembler;
import com.milaboratory.mageri.core.assemble.Consensus;
import com.milaboratory.mageri.core.assemble.PAssembler;
import com.milaboratory.mageri.core.assemble.SAssembler;
import com.milaboratory.mageri.core.genomic.Reference;
import com.milaboratory.mageri.core.genomic.ReferenceLibrary;
import com.milaboratory.mageri.core.mapping.alignment.Aligner;
import com.milaboratory.mageri.core.mapping.alignment.ExtendedKmerAligner;
import com.milaboratory.mageri.generators.MutationGenerator;
import com.milaboratory.mageri.generators.MigWithMutations;
import com.milaboratory.mageri.generators.RandomMigGenerator;
import com.milaboratory.mageri.generators.RandomReferenceGenerator;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Random;

public class ConsensusAlignerTest {
    private final static Random rnd = new Random(480011);

    @Test
    @Ignore("TODO")
    public void parallelTest() {
    }

    @Test
    @Ignore("TODO")
    public void majorMutationsTest() {
    }

    @Test
    @Category(FastTests.class)
    public void test() {
        int nReferences = 500;

        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator();
        ReferenceLibrary referenceLibrary;
        StatsByRef statsByRef;
        String setting;

        setting = "Single-end";
        randomReferenceGenerator.setReferenceSizeMin(100);
        randomReferenceGenerator.setReferenceSizeMax(150);
        referenceLibrary = randomReferenceGenerator.nextReferenceLibrary(nReferences);
        statsByRef = new StatsByRef(referenceLibrary);
        ConsensusAligner consensusAligner = alignerTest(randomReferenceGenerator,
                statsByRef, setting);
        checkMutationsTable(statsByRef, consensusAligner, setting);

        setting = "Paired-end, overlapping";
        randomReferenceGenerator.setReferenceSizeMin(200);
        randomReferenceGenerator.setReferenceSizeMax(300);
        referenceLibrary = randomReferenceGenerator.nextReferenceLibrary(nReferences);
        statsByRef = new StatsByRef(referenceLibrary);
        consensusAligner = alignerTest(randomReferenceGenerator,
                statsByRef, setting, -10, 10);
        checkMutationsTable(statsByRef, consensusAligner, setting);

        setting = "Paired-end, non-overlapping";
        statsByRef = new StatsByRef(referenceLibrary);
        consensusAligner = alignerTest(randomReferenceGenerator,
                statsByRef, setting, -20, 10);
        checkMutationsTable(statsByRef, consensusAligner, setting);
    }

    public ConsensusAligner alignerTest(RandomReferenceGenerator randomReferenceGenerator,
                                        StatsByRef statsByRef,
                                        String setting) {
        return alignerTest(randomReferenceGenerator, statsByRef, setting, false, -1, -1);
    }

    public ConsensusAligner alignerTest(RandomReferenceGenerator randomReferenceGenerator,
                                        StatsByRef statsByRef,
                                        String setting, int overlapMin, int overlapMax) {
        return alignerTest(randomReferenceGenerator, statsByRef, setting, true, overlapMin, overlapMax);
    }


    public ConsensusAligner alignerTest(RandomReferenceGenerator randomReferenceGenerator,
                                        StatsByRef statsByRef,
                                        String setting, boolean paired,
                                        int overlapMin, int overlapMax) {
        System.out.println("Consensus alignment test");

        int nMigs = 3000, maxOffset = 11, maxTrim = 11;

        RandomMigGenerator randomMigGenerator = new RandomMigGenerator();

        // NOTE: todo: change when indel-proof assembler is finished
        randomMigGenerator.setMutationGenerator(MutationGenerator.NO_INDEL);

        Assembler assembler = paired ? new PAssembler() : new SAssembler();
        Aligner aligner = new ExtendedKmerAligner(statsByRef.getReferenceLibrary());
        ConsensusAligner consensusAligner = paired ? new PConsensusAligner(aligner) :
                new SConsensusAligner(aligner);

        int correctMappings = 0, totalMappings = 0;

        for (int j = 0; j < nMigs; j++) {
            Reference reference = randomReferenceGenerator.nextReference(statsByRef.getReferenceLibrary());

            int offset = rnd.nextInt(maxOffset),
                    trim = rnd.nextInt(maxTrim);

            MigWithMutations MigWithMutations = randomMigGenerator.nextMigWithMajorMutations(
                    reference.getSequence().getRange(offset, reference.getSequence().size() - trim));

            Mig mig = paired ? MigWithMutations.getPMig(overlapMin, overlapMax) : MigWithMutations.getSMig();

            Consensus consensus = assembler.assemble(mig);

            if (consensus != null) {
                totalMappings++;
                statsByRef.update(MigWithMutations, reference, offset);
                AlignedConsensus alignedConsensus = consensusAligner.align(consensus);

                if (alignedConsensus.isAligned() && alignedConsensus.isMapped() &&
                        !alignedConsensus.isChimeric() &&
                        (alignedConsensus.isPairedEnd() ?
                                ((PAlignedConsensus) alignedConsensus).getAlignmentResult1().getReference() :
                                ((SAlignedConsensus) alignedConsensus).getAlignmentResult().getReference()).
                                equals(reference)) {
                    correctMappings++;
                }
            }
        }

        PercentRangeAssertion.createLowerBound("Correct mapping rate", setting, 95).
                assertInRange(correctMappings, totalMappings);

        return consensusAligner;
    }

    public void checkMutationsTable(StatsByRef statsByRef,
                                    ConsensusAligner consensusAligner,
                                    String setting) {
        System.out.println("Mutation table test");

        double majorKLDiv = 0, minorKLDiv = 0;
        int nMajorCells = 0, nMinorCells = 0;
        int noMajor = 0, noMinor = 0,
                falsePositiveMajor = 0, falsePositiveMinor = 0,
                refBasesObs = 0, refBasesExp = 0;

        for (Reference reference : consensusAligner.getReferenceLibrary().getReferences()) {
            int n = reference.getSequence().size();

            int[][] expectedMajorCounts = statsByRef.getMajorCounts(reference),
                    expectedMinorCounts = statsByRef.getMinorCounts(reference);

            MutationsTable table = consensusAligner.getAlignerTable(reference);

            for (int i = 0; i < n; i++) {
                int otherMajorCount = 0;
                for (byte bp = 0; bp < 4; bp++) {
                    boolean isRef = reference.getSequence().codeAt(i) == bp;

                    int majorObs = table.getMajorMigCount(i, bp),
                            minorObs = table.getMinorMigCount(i, bp);

                    if (!isRef) {
                        int majorExp = expectedMajorCounts[i][bp],
                                minorExp = expectedMinorCounts[i][bp];

                        if (majorExp == 0) {
                            falsePositiveMajor += majorObs;
                            noMajor += table.getMigCoverage(i);
                        } else {
                            majorKLDiv += majorObs == 0 ? 0 : (majorObs * Math.log(majorObs / (double) majorExp));
                            nMajorCells++;
                        }

                        otherMajorCount += majorExp;

                        if (minorExp == 0) {
                            falsePositiveMinor += minorObs;
                            noMinor += table.getMigCoverage(i);
                        } else {
                            minorKLDiv += minorObs == 0 ? 0 : (minorObs * Math.log(minorObs / (double) minorExp));
                            if (minorKLDiv == Double.NEGATIVE_INFINITY) {
                                System.out.println();
                            }
                            nMinorCells++;
                        }
                    } else {
                        refBasesObs += majorObs;
                        refBasesExp += table.getMigCoverage(i);
                    }
                }
                refBasesExp -= otherMajorCount;
            }
        }

        DoubleRangeAssertion.createUpperBound("Minor count KLdiv", setting, 0.05).
                assertInRange(minorKLDiv / nMinorCells);

        DoubleRangeAssertion.createUpperBound("Major count KLdiv", setting, 0.05).
                assertInRange(majorKLDiv / nMajorCells);

        PercentRangeAssertion.createUpperBound("False positive minor", setting, 5).
                assertInRange(falsePositiveMinor, noMinor);

        PercentRangeAssertion.createUpperBound("False positive major", setting, 5).
                assertInRange(falsePositiveMajor, noMajor);

        PercentRangeAssertion.createLowerBound("Ref bases coincidence", setting, 95).
                assertInRange(refBasesObs, refBasesExp);
    }
}
