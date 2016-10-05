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
package com.antigenomics.mageri.core.mapping;

import com.antigenomics.mageri.DoubleRangeAssertion;
import com.antigenomics.mageri.core.assemble.*;
import com.antigenomics.mageri.core.genomic.*;
import com.antigenomics.mageri.core.input.PreprocessorParameters;
import com.antigenomics.mageri.core.mapping.alignment.Aligner;
import com.antigenomics.mageri.core.mutations.Mutation;
import com.antigenomics.mageri.core.variant.Variant;
import com.antigenomics.mageri.core.variant.model.ErrorRateEstimate;
import com.antigenomics.mageri.generators.*;
import com.antigenomics.mageri.misc.ProcessorResultWrapper;
import com.antigenomics.mageri.pipeline.input.InputStreamWrapper;
import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.antigenomics.mageri.FastTests;
import com.antigenomics.mageri.PercentRangeAssertion;
import com.antigenomics.mageri.core.Mig;
import com.antigenomics.mageri.core.mapping.alignment.ExtendedKmerAligner;
import com.antigenomics.mageri.core.mutations.MutationArray;
import com.antigenomics.mageri.pipeline.analysis.Project;
import com.antigenomics.mageri.pipeline.analysis.Sample;
import com.antigenomics.mageri.pipeline.analysis.SampleGroup;
import com.antigenomics.mageri.pipeline.input.ResourceIOProvider;
import com.milaboratory.core.sequencing.io.fasta.FastaReader;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.util.*;

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
    public void krasTest() throws IOException {
        NucleotideSQPair nucleotideSQPair = new NucleotideSQPair(
                // ACCAGTAATATGCATATTAAAACAAGATTTACCTCTATTGTTGGATCATATTCGTCCACAAAATGATTCTGAATTAGCTGTATCGTCAAGGCACTCTTGCCTACGCCACcAGCT
                "ACCAGTAATATGCATATTAAAACAAGATTTACCTCTATTGTTGGATCATATTCGTCCACAAAATGATTCTGAATTAGCTGTATCGTCAAGGCACTCTTGCCTACGCCACAAGCT",
                "IIIIIIIIIIIIIIIIIIIIIIIIIGIIIIIIIIIIIIIIIIIIIIIIKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKIIIIIIIIIIIIIIIIIIIIIHIIIIIIIIIIII");

        SConsensus consensus = new SConsensus(
                new Sample("test", new SampleGroup("test", false, new Project("test"))),
                new NucleotideSequence("GGGAGTTGAGAGTT"),
                nucleotideSQPair, new HashSet<Integer>(),
                103, 103
        );

        ReferenceLibrary referenceLibrary = ReferenceLibrary.fromInput(
                ResourceIOProvider.INSTANCE.getWrappedStream("genomic/panel_refs.fa"),
                new BedGenomicInfoProvider(
                        ResourceIOProvider.INSTANCE.getWrappedStream("genomic/panel_refs.bed"),
                        ResourceIOProvider.INSTANCE.getWrappedStream("pipeline/contigs.txt")));

        SConsensusAligner consensusAligner = new SConsensusAligner(referenceLibrary);

        SAlignedConsensus alignedConsensus = consensusAligner.align(consensus);

        MutationArray mutationArray = alignedConsensus.getMutations();
        Assert.assertEquals(1, mutationArray.getLength());
        Assert.assertEquals("S109:C>A", mutationArray.getMutations().get(0).toString());
    }

    @Test
    @Category(FastTests.class)
    public void singleEndTest() {
        int nReferences = 500;

        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator();
        ReferenceLibrary referenceLibrary;
        StatsByRef statsByRef;
        String setting;
        ConsensusAligner consensusAligner;

        setting = "Single-end";
        randomReferenceGenerator.setReferenceSizeMin(100);
        randomReferenceGenerator.setReferenceSizeMax(150);
        referenceLibrary = randomReferenceGenerator.nextReferenceLibrary(nReferences);
        statsByRef = new StatsByRef(referenceLibrary);
        consensusAligner = alignerTest(randomReferenceGenerator, statsByRef, setting);
        checkMutationsTable(statsByRef, consensusAligner, setting);
    }

    @Test
    @Category(FastTests.class)
    public void pairedEndOverlappingTest() {
        int nReferences = 500;

        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator();
        ReferenceLibrary referenceLibrary;
        StatsByRef statsByRef;
        String setting;
        ConsensusAligner consensusAligner;

        setting = "Paired-end, overlapping";
        randomReferenceGenerator.setReferenceSizeMin(200);
        randomReferenceGenerator.setReferenceSizeMax(300);
        referenceLibrary = randomReferenceGenerator.nextReferenceLibrary(nReferences);
        statsByRef = new StatsByRef(referenceLibrary);
        consensusAligner = alignerTest(randomReferenceGenerator, statsByRef, setting, -10, 10);
        checkMutationsTable(statsByRef, consensusAligner, setting);
    }

    @Test
    @Category(FastTests.class)
    public void pairedEndNonOverlappingTest() {
        int nReferences = 500;

        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator();
        ReferenceLibrary referenceLibrary;
        StatsByRef statsByRef;
        String setting;
        ConsensusAligner consensusAligner;

        setting = "Paired-end, non-overlapping";
        randomReferenceGenerator.setReferenceSizeMin(200);
        randomReferenceGenerator.setReferenceSizeMax(300);
        referenceLibrary = randomReferenceGenerator.nextReferenceLibrary(nReferences);
        statsByRef = new StatsByRef(referenceLibrary);
        consensusAligner = alignerTest(randomReferenceGenerator, statsByRef, setting, -20, 10);
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
        //randomMigGenerator.setMutationGenerator(MutationGenerator.NO_INDEL);

        Assembler assembler = paired ? new PAssembler(AssemblerParameters.TORRENT454, PreprocessorParameters.DEFAULT) :
                new SAssembler(AssemblerParameters.TORRENT454, PreprocessorParameters.DEFAULT);
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


    @Test
    @Category(FastTests.class)
    public void partitionedReferenceLibraryMappingTest() throws IOException {
        ReferenceLibraryParameters parameters = ReferenceLibraryParameters.DEFAULT.withReadLength(150);

        GenomicInfoProvider genomicInfoProvider = new BedGenomicInfoProvider(
                ResourceIOProvider.INSTANCE.getWrappedStream("genomic/cgc_exons_flank50.bed"),
                ResourceIOProvider.INSTANCE.getWrappedStream("genomic/contigs_hg38.txt"));

        List<SSequencingRead> fastaRecords = getLongNonOverlappingRefs(ResourceIOProvider
                .INSTANCE.getWrappedStream("genomic/cgc_exons_flank50.fa"),
                parameters, genomicInfoProvider);

        ReferenceLibrary partitionedReferenceLibrary = new ReferenceLibrary(fastaRecords,
                genomicInfoProvider, parameters),
                nonPartitionedReferenceLibrary = new ReferenceLibrary(fastaRecords,
                        genomicInfoProvider, parameters.withSplitLargeReferences(false));

        System.out.println("Selected " + nonPartitionedReferenceLibrary.size() + " references");

        ReferenceLibraryReadSampler referenceLibraryReadSampler = new ReferenceLibraryReadSampler(nonPartitionedReferenceLibrary);
        MutationGenerator mutationGenerator = MutationGenerator.DEFAULT;

        SConsensusAligner consensusAlignerPartitioned = new SConsensusAligner(partitionedReferenceLibrary),
                consensusAlignerUnpartitioned = new SConsensusAligner(nonPartitionedReferenceLibrary);

        int nReads = 10000, nMappedBoth = 0,
                lowConfMatchParitioned = 0, lowConfMatchUnparitioned = 0,
                discordantRefs = 0, discordantMutationsSameRef = 0,
                badRefParitioned = 0, badRefUnpartitioned = 0;

        byte mapqThreshold = 20;

        for (int i = 0; i < nReads; i++) {
            ReferenceParentChildPair referencePCP = referenceLibraryReadSampler.nextReadWithParent();
            Reference reference = referencePCP.getParentReference();
            NucleotideSequence sequence = mutationGenerator.nextMutatedSequence(referencePCP.getChildSequence());

            SConsensus consensus = createConsensus(sequence);

            SAlignedConsensus result1 = align(consensusAlignerPartitioned, consensus),
                    result2 = align(consensusAlignerUnpartitioned, consensus);

            boolean goodMatch1 = result1.getAlignmentResult().getScore() > mapqThreshold,
                    goodMatch2 = result2.getAlignmentResult().getScore() > mapqThreshold;

            if (!goodMatch1) {
                lowConfMatchParitioned++;
            }
            if (!goodMatch2) {
                lowConfMatchUnparitioned++;
            }

            if (!result1.getAlignmentResult().getReference().getOriginalName()
                    .equals(reference.getOriginalName())) {
                badRefParitioned++;
            }
            if (!result2.getAlignmentResult().getReference().getOriginalName()
                    .equals(reference.getOriginalName())) {
                badRefUnpartitioned++;
            }

            if (goodMatch1 && goodMatch2) {
                nMappedBoth++;
                if (!result1.getAlignmentResult().getReference().getOriginalName()
                        .equals(result2.getAlignmentResult().getReference().getOriginalName())) {
                    discordantRefs++;
                } else {
                    Set<String> variants1 = getVariants(result1),
                            variants2 = getVariants(result2);

                    if (!variants1.equals(variants2)) {
                        discordantMutationsSameRef++;
                    }
                }
            }
        }


        PercentRangeAssertion.createUpperBound("Discordant refs",
                "Partitioned-nonpartitioned comparison", 2).
                assertInRange(discordantRefs, nMappedBoth);
        PercentRangeAssertion.createUpperBound("Discordant mutations same ref",
                "Partitioned-nonpartitioned comparison", 1).
                assertInRange(discordantMutationsSameRef, nMappedBoth);

        PercentRangeAssertion.createUpperBound("Low-confidence mappings", "Partitioned library", 10).
                assertInRange(lowConfMatchParitioned, nReads);
        PercentRangeAssertion.createUpperBound("Low-confidence mappings", "Unpartitioned library", 6).
                assertInRange(lowConfMatchUnparitioned, nReads);

        PercentRangeAssertion.createUpperBound("Wrong reference", "Partitioned library", 1).
                assertInRange(badRefParitioned, nReads);
        PercentRangeAssertion.createUpperBound("Wrong reference", "Unpartitioned library", 5).
                assertInRange(badRefUnpartitioned, nReads);
    }

    private SAlignedConsensus align(SConsensusAligner consensusAligner, SConsensus consensus) {
        return (SAlignedConsensus) consensusAligner.process(new ProcessorResultWrapper<>(consensus)).getResult();
    }

    private Set<String> getVariants(SAlignedConsensus result) {
        MutationArray mutations = result.getMutations();

        Set<String> variants = new HashSet<>();

        for (Mutation mutation : mutations.getMutations()) {
            variants.add(
                    new Variant(result.getAlignmentResult().getReference(), mutation,
                            0, 0, 0, 0, new NucleotideSequence(""), true, ErrorRateEstimate.createDummy(0))
                            .getGenomicString()
            );
        }

        return variants;
    }

    private SConsensus createConsensus(NucleotideSequence seq) {
        return new SConsensus(null, null, new NucleotideSQPair(seq), new HashSet<Integer>(), 1, 1);
    }

    private List<SSequencingRead> getLongNonOverlappingRefs(InputStreamWrapper input,
                                                            ReferenceLibraryParameters referenceLibraryParameters,
                                                            GenomicInfoProvider genomicInfoProvider) throws IOException {
        FastaReader reader = new FastaReader(input.getInputStream(), false);
        List<SSequencingRead> records = new ArrayList<>();
        SSequencingRead record;
        Set<String> addedGenes = new HashSet<>(),
                bins = new HashSet<>();

        while ((record = reader.take()) != null) {
            if (record.getData().size() > referenceLibraryParameters.getMaxReferenceLength()) {
                String geneName = record.getDescription().split("_")[0];
                GenomicInfo genomicInfo = genomicInfoProvider.get(record.getDescription(),
                        record.getData().getSequence());

                String bin = genomicInfo.getChrom() + ":" +
                        (genomicInfo.getStart() + genomicInfo.getEnd()) / 2 / 1000000;

                if (!addedGenes.contains(geneName) && !bins.contains(bin)) {
                    records.add(record);
                    addedGenes.add(geneName);
                    bins.add(bin);
                }
            }
        }

        return records;
    }
}
