/**
 * Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)
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
 */

package com.milaboratory.migec2.benchmark;

import com.milaboratory.migec2.core.align.processor.Aligner;
import com.milaboratory.migec2.core.align.processor.AlignerFactory;
import com.milaboratory.migec2.core.align.reference.ReferenceLibrary;
import com.milaboratory.migec2.core.assemble.entity.Consensus;
import com.milaboratory.migec2.core.assemble.processor.Assembler;
import com.milaboratory.migec2.core.assemble.processor.PAssembler;
import com.milaboratory.migec2.core.assemble.processor.SAssembler;
import com.milaboratory.migec2.core.consalign.entity.AlignedConsensus;
import com.milaboratory.migec2.core.consalign.processor.ConsensusAligner;
import com.milaboratory.migec2.core.consalign.processor.PConsensusAligner;
import com.milaboratory.migec2.core.consalign.processor.SConsensusAligner;
import com.milaboratory.migec2.core.correct.CorrectedConsensus;
import com.milaboratory.migec2.core.correct.Corrector;
import com.milaboratory.migec2.core.haplotype.Haplotype;
import com.milaboratory.migec2.core.haplotype.HaplotypeTree;
import com.milaboratory.migec2.core.haplotype.HaplotypeTreeParameters;
import com.milaboratory.migec2.core.haplotype.misc.HaplotypeErrorStatistics;
import com.milaboratory.migec2.core.haplotype.misc.SimpleHaplotypeErrorStatistics;
import com.milaboratory.migec2.core.io.entity.Mig;
import com.milaboratory.migec2.datasim.MigGenerator;
import com.milaboratory.migec2.datasim.SMigGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BenchmarkRunner {
    private final MigGenerator migGenerator;
    private final Assembler assembler;
    private final ConsensusAligner consensusAligner;
    private final ReferenceLibrary referenceLibrary;

    public BenchmarkRunner(MigGenerator migGenerator, AlignerFactory alignerFactory) {
        this.migGenerator = migGenerator;
        this.referenceLibrary = migGenerator.getReferenceLibrary();
        Aligner aligner = alignerFactory.fromReferenceLibrary(referenceLibrary);

        boolean sMig = migGenerator instanceof SMigGenerator;

        this.assembler = sMig ? new SAssembler() : new PAssembler();
        this.consensusAligner = sMig ? new SConsensusAligner(aligner) : new PConsensusAligner(aligner);
    }

    public BenchmarkStatistics run(int numberOfMolecules) throws Exception {
        // Main processing
        List<AlignedConsensus> alignmentDataList = new ArrayList<>();

        int nAssembledMigs = 0, nAlignedMigs = 0;

        for (int i = 0; i < numberOfMolecules; i++) {
            Mig mig = migGenerator.take();
            Consensus consensus = assembler.assemble(mig);

            if (consensus != null) {
                nAssembledMigs++;
                AlignedConsensus alignmentData = consensusAligner.align(consensus);

                if (alignmentData != null) {
                    nAlignedMigs++;
                    alignmentDataList.add(alignmentData);
                }
            }
        }

        // Find major and minor mutations
        Corrector corrector = new Corrector(consensusAligner.getAlignerReferenceLibrary());

        // Error statistics for haplotype filtering using binomial test
        HaplotypeErrorStatistics errorStatistics =
                new SimpleHaplotypeErrorStatistics(corrector.getCorrectorReferenceLibrary());

        // Haplotype 1-mm graph
        HaplotypeTree haplotypeTree = new HaplotypeTree(errorStatistics,
                HaplotypeTreeParameters.NO_PVALUE_THRESHOLD);

        // Correction processing (MIGEC)
        for (AlignedConsensus alignmentData : alignmentDataList) {
            CorrectedConsensus correctedConsensus = corrector.correct(alignmentData);
            if (correctedConsensus != null)
                haplotypeTree.add(correctedConsensus);
        }

        // Statistical filtering of haplotypes
        haplotypeTree.calculatePValues();
        Set<Haplotype> finalHaplotypes = haplotypeTree.getHaplotypes(0.05);

        return null;
        //return new BenchmarkStatistics(numberOfMolecules, nAssembledMigs, nAlignedMigs, migGenerator.getMigGeneratorHistory());

        /*

        System.out.println("Total     = " + numberOfMolecules);
        System.out.println("Assembled = " + nAssembledMigs);
        System.out.println("Aligned   = " + nAlignedMigs);
        System.out.println("History\n" + migGenerator.getHistory().toString());




        System.out.println();
        System.out.println(corrector.getCorrectorReferenceLibrary());



        System.out.println();
        System.out.println(haplotypeTree);


        //Set<NucleotideSequence> finalHaplotypeSequences = new HashSet<>();
        //for (Haplotype hsd : finalHaplotypes)
        //    finalHaplotypeSequences.add(hsd.getHaplotypeSequence());

        System.out.println();
        System.out.println(errorStatistics); */
    }
}
