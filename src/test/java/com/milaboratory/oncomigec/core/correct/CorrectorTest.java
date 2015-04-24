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
package com.milaboratory.oncomigec.core.correct;

import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.oncomigec.core.mapping.alignment.ExtendedKmerAligner;
import com.milaboratory.oncomigec.core.assemble.SConsensus;
import com.milaboratory.oncomigec.core.assemble.SAssembler;
import com.milaboratory.oncomigec.core.mapping.PAlignedConsensus;
import com.milaboratory.oncomigec.core.mapping.ConsensusAligner;
import com.milaboratory.oncomigec.core.mapping.SConsensusAligner;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.core.input.SMig;
import com.milaboratory.oncomigec.core.variant.VariantCallerParameters;
import com.milaboratory.oncomigec.misc.testing.generators.GeneratorMutationModel;
import com.milaboratory.oncomigec.misc.testing.generators.RandomMigGenerator;
import com.milaboratory.oncomigec.misc.testing.generators.RandomReferenceGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class CorrectorTest {
    private final int nReferences = 10, nMigs = 100;
    private final double positionIndependentNoIndelMigecFoldThreshold = 20.0;

    @Test
    public void randomizedPositionIndependentNoIndelTest() throws IOException {
       /* RandomMigGenerator migGenerator = new RandomMigGenerator();
        migGenerator.setGeneratorMutationModel(GeneratorMutationModel.NO_INDEL);
        RandomReferenceGenerator referenceGenerator = new RandomReferenceGenerator();

        double averageStage0ErrorFrequency = 0, averageStage1ErrorFrequency = 0, averageStage2ErrorFrequency = 0;
        for (int i = 0; i < nReferences; i++) {
            SAssembler assembler = new SAssembler();
            ReferenceLibrary referenceLibrary = referenceGenerator.nextReferenceLibrary(1);
            Reference reference = referenceLibrary.getReferences().get(0);
            ConsensusAligner consensusAligner = new SConsensusAligner(new ExtendedKmerAligner(referenceLibrary));
            List<PAlignedConsensus> alignedConsensuses = new LinkedList<>();

            double stage0ErrorFrequency = 0, stage1ErrorFrequency = 0, stage2ErrorFrequency = 0;
            for (int j = 0; j < nMigs; j++) {
                RandomMigGenerator.RandomMigGeneratorResult randomMigResult = migGenerator.nextMigPCR(reference);
                stage0ErrorFrequency += randomMigResult.getPcrMutations().length;

                SMig mig = randomMigResult.getMig();
                SConsensus consensus = assembler.assemble(mig);

                if (consensus != null) {
                    PAlignedConsensus alignedConsensus = consensusAligner.align(consensus);
                    if (alignedConsensus != null) {
                        stage1ErrorFrequency += alignedConsensus.getMajorMutations().substitutionCount();
                        alignedConsensuses.add(alignedConsensus);
                    }
                }
            }
            stage0ErrorFrequency /= nMigs;
            stage0ErrorFrequency /= reference.getSequence().size();
            averageStage0ErrorFrequency += stage0ErrorFrequency;

            stage1ErrorFrequency /= alignedConsensuses.size();
            stage1ErrorFrequency /= reference.getSequence().size();
            averageStage1ErrorFrequency += stage1ErrorFrequency;

            Corrector corrector = new Corrector(consensusAligner.getAlignerTable(),
                    VariantCallerParameters.FILTERING, new ErrorLibrary(true));

            for (PAlignedConsensus alignedConsensus : alignedConsensuses) {
                CorrectedConsensus correctedConsensus = corrector.correct(alignedConsensus);
                if (correctedConsensus != null)
                    stage2ErrorFrequency += Mutations.substitutions(correctedConsensus.getMutations());
            }
            stage2ErrorFrequency /= alignedConsensuses.size();
            stage2ErrorFrequency /= reference.getSequence().size();
            averageStage2ErrorFrequency += stage2ErrorFrequency;
        }
        averageStage0ErrorFrequency /= nReferences;
        averageStage1ErrorFrequency /= nReferences;
        averageStage2ErrorFrequency /= nReferences;

        System.out.println("Position-independent MIGEC test");
        System.out.println("Base error frequency = " + averageStage0ErrorFrequency);
        System.out.println("Consensus error frequency = " + averageStage1ErrorFrequency);
        System.out.println("Corrected error frequency = " + averageStage2ErrorFrequency);

        double fold = averageStage1ErrorFrequency / averageStage2ErrorFrequency;

        Assert.assertTrue("Position-independent MIGEC gives >= " +
                        positionIndependentNoIndelMigecFoldThreshold + "-fold less errors than consensus assembly",
                fold >= positionIndependentNoIndelMigecFoldThreshold
        );*/
    }
}