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

package com.antigenomics.mageri.core.variant;

import com.antigenomics.mageri.DoubleRangeAssertion;
import com.antigenomics.mageri.FastTests;
import com.antigenomics.mageri.core.assemble.*;
import com.antigenomics.mageri.core.genomic.Reference;
import com.antigenomics.mageri.core.mapping.ConsensusAligner;
import com.antigenomics.mageri.core.variant.model.SubstitutionErrorMatrix;
import com.antigenomics.mageri.generators.MutationGenerator;
import com.antigenomics.mageri.generators.RandomMigGenerator;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import com.antigenomics.mageri.core.genomic.ReferenceLibrary;
import com.antigenomics.mageri.core.input.SMig;
import com.antigenomics.mageri.core.mapping.SConsensusAligner;
import com.antigenomics.mageri.generators.RandomReferenceGenerator;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class SubstitutionErrorMatrixTest {
    @Test
    @Category(FastTests.class)
    @SuppressWarnings("unchecked")
    public void test() {
        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator();

        randomReferenceGenerator.setReferenceSizeMin(200);
        randomReferenceGenerator.setReferenceSizeMax(200);

        ReferenceLibrary referenceLibrary = randomReferenceGenerator.nextReferenceLibrary(1);
        Reference reference = referenceLibrary.getAt(0);

        int migSize = 100;

        RandomMigGenerator randomMigGenerator = new RandomMigGenerator();
        MutationGenerator mutationGenerator = MutationGenerator.NO_INDEL;
        randomMigGenerator.setMutationGenerator(mutationGenerator);
        randomMigGenerator.setMigSizeMax(migSize);
        randomMigGenerator.setMigSizeMin(migSize);

        SAssembler assembler = new SAssembler(AssemblerParameters.DEFAULT, new DummyMinorCaller());
        ConsensusAligner aligner = new SConsensusAligner(referenceLibrary);

        for (int i = 0; i < 1000; i++) {
            SMig mig = randomMigGenerator.nextMig(reference).getSMig();
            SConsensus consensus = assembler.assemble(mig);
            if (consensus != null) {
                aligner.align(consensus);
            }
        }

        SubstitutionErrorMatrix substitutionErrorMatrix = SubstitutionErrorMatrix.fromMutationsTable(aligner.getAlignerTable(reference));

        DoubleRangeAssertion assertion =
                DoubleRangeAssertion.createRange("Observed vs expected substitution rate ratio",
                        "Random MIG test", 0.7, 1.3);

        double JITTER = 1e-6;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i != j) {
                    double observed = substitutionErrorMatrix.getRate(i, j, false) / migSize + JITTER,
                            expected = mutationGenerator.getSubstitutionModel().getValue(i, j) + JITTER;
                    System.out.println(
                            NucleotideAlphabet.INSTANCE.symbolFromCode((byte) i) + ">" +
                                    NucleotideAlphabet.INSTANCE.symbolFromCode((byte) j) + "\t" +
                                    observed + "\t" + expected);
                    assertion.assertInRange(observed / expected);
                }
            }
        }
    }
}
