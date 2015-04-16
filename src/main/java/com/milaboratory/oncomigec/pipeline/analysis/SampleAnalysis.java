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
 * Last modified on 12.3.2015 by mikesh
 */

package com.milaboratory.oncomigec.pipeline.analysis;

import cc.redberry.pipe.OutputPort;
import cc.redberry.pipe.blocks.Merger;
import cc.redberry.pipe.blocks.ParallelProcessor;
import cc.redberry.pipe.util.CountingOutputPort;
import com.milaboratory.oncomigec.misc.ReadSpecific;
import com.milaboratory.oncomigec.core.PipelineBlock;
import com.milaboratory.oncomigec.core.assemble.Consensus;
import com.milaboratory.oncomigec.core.assemble.Assembler;
import com.milaboratory.oncomigec.core.align.AlignedConsensus;
import com.milaboratory.oncomigec.core.align.AlignerTable;
import com.milaboratory.oncomigec.core.align.ConsensusAligner;
import com.milaboratory.oncomigec.core.correct.CorrectedConsensus;
import com.milaboratory.oncomigec.core.correct.Corrector;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.haplotype.HaplotypeAssembler;
import com.milaboratory.oncomigec.core.input.Mig;
import com.milaboratory.oncomigec.core.input.MigSizeDistribution;
import com.milaboratory.oncomigec.core.input.MigOutputPort;
import com.milaboratory.oncomigec.core.variant.VariantContainer;
import com.milaboratory.oncomigec.core.variant.VariantLibrary;
import com.milaboratory.oncomigec.pipeline.Speaker;
import com.milaboratory.oncomigec.misc.ProcessorResultWrapper;
import org.apache.commons.math.MathException;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SampleAnalysis implements ReadSpecific, Serializable {
    private static final boolean ENABLE_BUFFERING = true;
    protected final boolean paired;

    protected final ProjectAnalysis parent;
    protected final Sample sample;

    protected transient final MigOutputPort reader;
    protected transient VariantLibrary variantLibrary;

    protected final MigSizeDistribution migSizeDistribution;
    protected final Assembler assembler;
    protected final ConsensusAligner aligner;
    protected Corrector corrector;
    protected HaplotypeAssembler haplotypeAssembler;

    private boolean firstStageRan = false, secondStageRan = false;

    private final List<AlignedConsensus> alignmentDataList = new LinkedList<>();

    @SuppressWarnings("unchecked")
    protected SampleAnalysis(ProjectAnalysis parent,
                             Sample sample,
                             MigSizeDistribution migSizeDistribution,
                             MigOutputPort reader,
                             Assembler assembler,
                             ConsensusAligner consensusAligner) {
        this.parent = parent;
        this.migSizeDistribution = migSizeDistribution;
        this.sample = sample;
        this.reader = reader;
        this.paired = reader.isPairedEnd();

        if (assembler.isPairedEnd() != paired ||
                consensusAligner.isPairedEnd() != paired)
            throw new RuntimeException("All read-specific pipeline steps should have the same paired-end property.");

        this.assembler = assembler;
        this.aligner = consensusAligner;
    }

    private void sout(String message, int verbosityLevel) {
        Speaker.INSTANCE.sout("[" + parent.getProject().getName() + "] [" + sample.getFullName() + "] " +
                message, verbosityLevel);
    }

    @SuppressWarnings("unchecked")
    public void runFirstStage() throws Exception {
        if (firstStageRan)
            return;

        sout("Running first stage.", 1);

        OutputPort<Mig> input = reader;

        if (ENABLE_BUFFERING) {
            final Merger<Mig> bufferedInput = new Merger<>(524288);
            bufferedInput.merge(input);
            bufferedInput.start();
            input = bufferedInput;
        }

        final CountingOutputPort<Mig> countingInput = new CountingOutputPort<>(input);

        new Thread(new Runnable() {
            long prevCount = -1;

            @Override
            public void run() {
                try {
                    while (!countingInput.isClosed()) {
                        long count = countingInput.getCount();
                        if (prevCount != count) {
                            sout("Assemblying & aligning consensuses, " + count + " MIGs processed..", 2);
                            prevCount = count;
                        }
                        Thread.sleep(10000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // Assemble & align in parallel
        final OutputPort<ProcessorResultWrapper<Consensus>> assemblyResults =
                new ParallelProcessor<>(countingInput, assembler, parent.getRuntimeParameters().getNumberOfThreads());

        final OutputPort<ProcessorResultWrapper<AlignedConsensus>> alignerResults =
                new ParallelProcessor<>(assemblyResults, aligner, parent.getRuntimeParameters().getNumberOfThreads());

        ProcessorResultWrapper<AlignedConsensus> alignmentDataWrapped;
        while ((alignmentDataWrapped = alignerResults.take()) != null)
            if (alignmentDataWrapped.hasResult())
                alignmentDataList.add(alignmentDataWrapped.getResult());

        sout("Finished first stage, " + countingInput.getCount() + " MIGs processed in total.", 1);

        firstStageRan = true;
    }

    public void runSecondStage() throws MathException, IOException {
        if (!firstStageRan)
            throw new RuntimeException("Should run first stage first.");

        if (secondStageRan)
            return;

        sout("Running second stage.", 1);

        sout("Correcting variants.", 1);

        // Find major and minor mutations
        this.corrector = new Corrector(aligner.getAlignerTable(),
                parent.getPresets().getVariantCallerParameters());

        // Error statistics for haplotype filtering using binomial test
        // Store here for output summary purposes
        variantLibrary = corrector.getCorrectorReferenceLibrary().getVariantLibrary();

        sout("Assemblying haplotypes.", 1);

        // Haplotype 1-mm graph
        this.haplotypeAssembler = new HaplotypeAssembler(
                corrector.getCorrectorReferenceLibrary(),
                parent.getPresets().getHaplotypeAssemblerParameters());

        // Correction processing (MIGEC)
        for (AlignedConsensus alignmentData : alignmentDataList) {
            CorrectedConsensus correctedConsensus = corrector.correct(alignmentData);
            if (correctedConsensus != null)
                haplotypeAssembler.add(correctedConsensus);
        }

        // Haplotype filtering
        haplotypeAssembler.filterEscaped();

        secondStageRan = true;

        sout("Finished second stage, " + haplotypeAssembler.getFilteredHaplotypes().size() + " haplotypes assembled.", 1);
    }

    public VariantContainer dumpMinorVariants(Reference reference) {
        if (!firstStageRan)
            throw new RuntimeException("Should run first stage first.");

        AlignerTable alignerTable = aligner.getAlignerTable();
        AlignerTable substitutionsAndCoverage = alignerTable.getSubstitutionsAndCoverage(reference);

        if (substitutionsAndCoverage.wasUpdated())
            return new VariantContainer(substitutionsAndCoverage);
        else
            return null;
    }

    public MigOutputPort getReader() {
        return reader;
    }

    public MigSizeDistribution getMigSizeDistribution() {
        return migSizeDistribution;
    }

    public Assembler getAssembler() {
        return assembler;
    }

    public ConsensusAligner getAligner() {
        return aligner;
    }

    public Corrector getCorrector() {
        return corrector;
    }

    public Sample getSample() {
        return sample;
    }

    public ProjectAnalysis getParent() {
        return parent;
    }

    public HaplotypeAssembler getHaplotypeAssembler() {
        return haplotypeAssembler;
    }

    public VariantLibrary getVariantLibrary() {
        return variantLibrary;
    }

    public boolean isFirstStageRan() {
        return firstStageRan;
    }

    public boolean isSecondStageRan() {
        return secondStageRan;
    }

    public List<PipelineBlock> getBlocks() {
        return Arrays.asList(
                migSizeDistribution,
                assembler,
                aligner,
                corrector,
                haplotypeAssembler
        );
    }

    @Override
    public boolean isPairedEnd() {
        return paired;
    }
}
