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
package com.milaboratory.oncomigec.core.haplotype;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.align.processor.aligners.ExtendedExomeAligner;
import com.milaboratory.oncomigec.core.align.reference.ReferenceLibrary;
import com.milaboratory.oncomigec.core.assemble.entity.Consensus;
import com.milaboratory.oncomigec.core.assemble.processor.Assembler;
import com.milaboratory.oncomigec.core.assemble.processor.PAssembler;
import com.milaboratory.oncomigec.core.consalign.entity.AlignedConsensus;
import com.milaboratory.oncomigec.core.consalign.processor.ConsensusAligner;
import com.milaboratory.oncomigec.core.consalign.processor.PConsensusAligner;
import com.milaboratory.oncomigec.core.correct.CorrectedConsensus;
import com.milaboratory.oncomigec.core.correct.Corrector;
import com.milaboratory.oncomigec.core.io.entity.PMig;
import com.milaboratory.oncomigec.core.io.readers.PMigReader;
import com.milaboratory.oncomigec.util.testing.TestResources;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HaplotypeTreeDataTest {
    //@Test
    public void dataTest() throws Exception {
        String sampleName = "21_SPIKE-1R";//"BRAF-15";//// "SPIKE-2"

        // Reader, from pre-processed data (no embedded checkout)
        PMigReader reader = new PMigReader(
                TestResources.getResource(sampleName + "_R1.fastq"),
                TestResources.getResource(sampleName + "_R2.fastq"),
                sampleName);

        // Consensus assembler
        Assembler assembler = new PAssembler();
        ReferenceLibrary referenceLibrary =
                new ReferenceLibrary(TestResources.getResource("refs.fa"));

        // Aligner
        ConsensusAligner aligner = new PConsensusAligner(new ExtendedExomeAligner(referenceLibrary));
        PMig mig;

        // Main processing
        List<AlignedConsensus> alignmentDataList = new ArrayList<>();
        int nMigs = 0, nAssembledMigs = 0, nAlignedMigs = 0;
        reader.setCurrentSample(sampleName);
        reader.setSizeThreshold(32);
        while ((mig = reader.take()) != null) {
            Consensus consensus = assembler.assemble(mig);
            if (consensus != null) {
                nAssembledMigs++;
                AlignedConsensus alignmentData = aligner.align(consensus);
                if (alignmentData != null) {
                    nAlignedMigs++;
                    alignmentDataList.add(alignmentData);
                }
            }
            nMigs++;
        }
        System.out.println();
        System.out.println("#MIGs processed = " + nMigs +
                ", assembled = " + nAssembledMigs +
                ", aligned = " + nAlignedMigs);

        //System.out.println();
        //System.out.println(assembler);

        System.out.println();
        System.out.println(aligner.getAlignerReferenceLibrary());

        // Find major and minor mutations
        Corrector corrector = new Corrector(aligner.getAlignerReferenceLibrary());

        // Error statistics for haplotype filtering using binomial test
        //HaplotypeErrorStatistics errorStatistics =
        //        new SimpleHaplotypeErrorStatistics(corrector.getCorrectorReferenceLibrary());

        // Haplotype 1-mm graph
        HaplotypeTree haplotypeTree = null;//new HaplotypeTree(errorStatistics,
        //HaplotypeTreeParameters.NO_PVALUE_THRESHOLD);

        // Correction processing (MIGEC)
        for (AlignedConsensus alignmentData : alignmentDataList) {
            CorrectedConsensus correctedConsensus = corrector.correct(alignmentData);
            if (correctedConsensus != null)
                haplotypeTree.add(correctedConsensus);
        }

        System.out.println();
        System.out.println(corrector.getCorrectorReferenceLibrary());

        // Statistical filtering of haplotypes
        haplotypeTree.calculatePValues();

        System.out.println();
        System.out.println(haplotypeTree);

        Set<Haplotype> finalHaplotypes = haplotypeTree.getHaplotypes(0.05);
        Set<NucleotideSequence> finalHaplotypeSequences = new HashSet<>();
        for (Haplotype hsd : finalHaplotypes)
            finalHaplotypeSequences.add(hsd.getHaplotypeSequence());

        System.out.println();
        //System.out.println(errorStatistics);

        // Tests
        Assert.assertTrue("Haplotype 1 (ref) found", finalHaplotypeSequences.contains(
                new NucleotideSequence("TGATCTTGACGTTGTAGATGAGGCAGCCGTTCTGGAG" +
                        "GCTGGTGTCCTGGGTAGCGGTCAGCACGCCCCCGTCTTCGTATGTGGTGATT" +
                        "CTCTCCCATGTGAAGCCCTCAGGGAAGGACTGCTTAAAGA")
        ));

        Assert.assertTrue("Haplotype 2 found", finalHaplotypeSequences.contains(
                new NucleotideSequence("TGATCTTGACGTTGTAGATGAGGCAGCCGTCCTGGAG" +
                        "GCTGGTGTCCTGGGTAGCGGTCAGCACGCCCCCGTCTTCGTATGTGGTGACT" +
                        "CTCTCCCATGTGAAGCCCTCGGGGAAGGACTGCTTAAAGA")
        ));

        Assert.assertFalse("Spurious haplotype 1 filtered", finalHaplotypeSequences.contains(
                new NucleotideSequence("TGATCTTGACGTTGTAGATGAGGCAGCCGTCCTGGAG" +
                        "GCTGGTGTCCTGGGTAGCGGTCAGCACGCCCCCGTCTTCGTATGTGGTGACT" +
                        "CTCTCCCATGTGAAGCCCTCAGGGAAGGACTGCTTAAAGA")
        ));

        Assert.assertFalse("Spurious haplotype 2 filtered", finalHaplotypeSequences.contains(
                new NucleotideSequence("TGATCTTGACGTTGTAGATGAGGCAGCCGTTCTGGAG" +
                        "GCTGGTGTCCTGGGTAGCGGTCAGCACGCCCCCGTCTTCGTATGTGGTGACT" +
                        "CTCTCCCATGTGAAGCCCTCAGGGAAGGACTGCTTAAAGA")
        ));

        //
    }
}
