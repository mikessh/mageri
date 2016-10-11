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
import com.antigenomics.mageri.core.variant.model.MinorBasedErrorModel;
import com.antigenomics.mageri.generators.ModelMigGenerator;
import com.antigenomics.mageri.generators.ModelMigGeneratorFactory;
import com.antigenomics.mageri.generators.RandomReferenceGenerator;
import org.junit.Test;

public class MinorBasedErrorModelTest {
    @Test
    public void test() {
        ModelMigGeneratorFactory modelMigGeneratorFactory = new ModelMigGeneratorFactory();

        int nMigs = 1000;

        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator();
        randomReferenceGenerator.setReferenceSizeMin(100);
        randomReferenceGenerator.setReferenceSizeMax(100);

        final ReferenceLibrary referenceLibrary = randomReferenceGenerator.nextReferenceLibrary(1);
        final Reference reference = referenceLibrary.getAt(0);
        final Assembler assembler = new SAssembler();
        final Aligner aligner = new ExtendedKmerAligner(referenceLibrary);

        final ConsensusAligner consensusAligner = new SConsensusAligner(aligner);
        final ModelMigGenerator modelMigGenerator = modelMigGeneratorFactory.create(reference.getSequence());

        for (int j = 0; j < nMigs; j++) {
            Mig mig = modelMigGenerator.nextMig();
            Consensus consensus = assembler.assemble(mig);
            if (consensus != null) {
                consensusAligner.align(consensus);
            }
        }

        MutationsTable mutationsTable = consensusAligner.getAlignerTable(reference);
        MinorBasedErrorModel errorModel = new MinorBasedErrorModel(
                VariantCallerParameters.DEFAULT.withOrder(0),
                mutationsTable, assembler.getMinorCaller());

        for (int i = 0; i < reference.size(); i++) {
            int base = reference.codeAt(i);
            for (int j = 0; j < 4; j++) {
                if (base != j) {
                    System.out.println(
                            modelMigGeneratorFactory.getPcrErrorGenerator().getSubstitutionModel().getValue(base, j) + "\t" +
                                    errorModel.computeErrorRate(i, base, j).getErrorRate());
                }
            }
        }
    }
}
