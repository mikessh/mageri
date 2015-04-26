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

import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.PercentRangeAssertion;
import com.milaboratory.oncomigec.generators.GeneratorMutationModel;
import com.milaboratory.oncomigec.generators.RandomMigGenerator;
import com.milaboratory.oncomigec.generators.RandomReferenceGenerator;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class AssemblerMinorTest {
    @Test
    public void minorTest() {
        minorTest(true,
                PercentRangeAssertion.createLowerBound("Specificity", "No indel minor recovery", 95),
                PercentRangeAssertion.createLowerBound("Sensitivity", "No indel minor recovery", 95));

        System.out.println("[Indel-compatible assembler not implemented yet: no read dropping in default assembler]");
        minorTest(false,
                PercentRangeAssertion.createDummy("Specificity", "Minor recovery"),
                PercentRangeAssertion.createDummy("Sensitivity", "Minor recovery"));
    }

    public void minorTest(boolean noIndel,
                          PercentRangeAssertion specificity, PercentRangeAssertion sensitivity) {
        int nRepetitions = 1000;
        RandomMigGenerator migGenerator = new RandomMigGenerator();
        if (noIndel) {
            migGenerator.setGeneratorMutationModel(GeneratorMutationModel.NO_INDEL);
        }
        migGenerator.setMaxRandomFlankSize(10);
        RandomReferenceGenerator referenceGenerator = new RandomReferenceGenerator();
        SAssembler assembler = new SAssembler();
        SConsensus consensus;

        int minorsTN = 0, minorsTP = 0, minorsFP = 0, minorsFN = 0;
        for (int i = 0; i < nRepetitions; i++) {
            NucleotideSequence core = referenceGenerator.nextSequence();
            RandomMigGenerator.RandomMigGeneratorResult randomMig = migGenerator.nextMig(core);

            consensus = assembler.assemble(randomMig.getMig());

            if (consensus != null) {
                Set<Integer> minors = new HashSet<>();

                for (int minor : randomMig.getMinorMutationCounts().keySet()) {
                    if (Mutations.isSubstitution(minor)) {
                        minors.add(minor);
                    }
                }

                int minorsExpectedCount = minors.size(),
                        minorsObservedCount = consensus.getMinors().size(),
                        minorsPossibleCount = consensus.getConsensusSQPair().size() * 3;

                minors.retainAll(consensus.getMinors());
                int overlap = minors.size();

                minorsTP += overlap;
                minorsFP += minorsObservedCount - overlap;
                minorsFN += minorsExpectedCount - overlap;
                minorsTN += minorsPossibleCount - minorsExpectedCount - minorsObservedCount + overlap;
            }
        }

        specificity.assertInRange(minorsTN, minorsFP + minorsTN);
        sensitivity.assertInRange(minorsTP, minorsTP + minorsFN);
    }
}
