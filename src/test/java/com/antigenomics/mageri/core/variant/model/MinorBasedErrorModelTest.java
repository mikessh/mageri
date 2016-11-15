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

package com.antigenomics.mageri.core.variant.model;

import com.antigenomics.mageri.ComplexRandomTests;
import com.antigenomics.mageri.core.Mig;
import com.antigenomics.mageri.core.assemble.Assembler;
import com.antigenomics.mageri.core.assemble.Consensus;
import com.antigenomics.mageri.core.assemble.SAssembler;
import com.antigenomics.mageri.core.genomic.Reference;
import com.antigenomics.mageri.core.genomic.ReferenceLibrary;
import com.antigenomics.mageri.core.mapping.ConsensusAligner;
import com.antigenomics.mageri.core.mapping.MutationsTable;
import com.antigenomics.mageri.core.mapping.SConsensusAligner;
import com.antigenomics.mageri.core.mapping.alignment.Aligner;
import com.antigenomics.mageri.core.mapping.alignment.ExtendedKmerAligner;
import com.antigenomics.mageri.core.variant.VariantCallerParameters;
import com.antigenomics.mageri.generators.ModelMigGenerator;
import com.antigenomics.mageri.generators.RandomReferenceGenerator;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class MinorBasedErrorModelTest {
    @Test
    @Category(ComplexRandomTests.class)
    public void test() {
        int nMigs = 50000, migSize = 16;

        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator();
        randomReferenceGenerator.setReferenceSizeMin(50);
        randomReferenceGenerator.setReferenceSizeMax(50);

        ReferenceLibrary referenceLibrary = randomReferenceGenerator.nextReferenceLibrary(1);
        Reference reference = referenceLibrary.getAt(0);
        Assembler assembler = new SAssembler();
        Aligner aligner = new ExtendedKmerAligner(referenceLibrary);

        ConsensusAligner consensusAligner = new SConsensusAligner(aligner);

        VariantCallerParameters variantCallerParameters =
                VariantCallerParameters.DEFAULT.withModelMinorCountThreshold(0);

        ModelMigGenerator modelMigGenerator = new ModelMigGenerator(variantCallerParameters,
                reference, migSize);

        for (int j = 0; j < nMigs; j++) {
            Mig mig = modelMigGenerator.nextMig();
            Consensus consensus = assembler.assemble(mig);
            if (consensus != null) {
                consensusAligner.align(consensus);
            }
        }

        MutationsTable mutationsTable = consensusAligner.getAlignerTable(reference);
        MinorBasedErrorModel errorModel = new MinorBasedErrorModel(variantCallerParameters,
                mutationsTable, assembler.getMinorCaller());

        for (int i = 0; i < reference.size(); i++) {
            int base = reference.codeAt(i);
            for (int j = 0; j < 4; j++) {
                if (base != j) {
                    double errorRateEst = errorModel.computeErrorRate(i, base, j, true).getErrorRate(),
                            errorRateExp = modelMigGenerator.getPcrMutationGenerator().getSubstitutionModel().getValue(base, j);
                    System.out.println("Substitution " + i + ":" +
                            NucleotideAlphabet.INSTANCE.symbolFromCode((byte) base) +
                            ">" + NucleotideAlphabet.INSTANCE.symbolFromCode((byte) j) +
                            ". Error rate expected = " + errorRateExp + ", estimated = " + errorRateEst);
                    if (errorRateExp < 1e-6) {
                        Assert.assertTrue("Small error rate in absence of errors",
                                errorRateEst < 1e-5);
                    } else {
                        Assert.assertTrue("No more than order of magnitude difference between expected PCR " +
                                        "error rate and its estimate",
                                Math.abs(Math.log10(errorRateEst) - Math.log10(errorRateExp)) <= 1.0);
                    }
                }
            }
        }
    }
}
