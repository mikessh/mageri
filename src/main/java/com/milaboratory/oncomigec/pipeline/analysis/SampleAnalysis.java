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
import com.milaboratory.oncomigec.core.ReadSpecific;
import com.milaboratory.oncomigec.core.assemble.entity.Consensus;
import com.milaboratory.oncomigec.core.assemble.processor.Assembler;
import com.milaboratory.oncomigec.core.consalign.entity.AlignedConsensus;
import com.milaboratory.oncomigec.core.consalign.entity.AlignerReferenceLibrary;
import com.milaboratory.oncomigec.core.consalign.mutations.MutationsAndCoverage;
import com.milaboratory.oncomigec.core.consalign.processor.ConsensusAligner;
import com.milaboratory.oncomigec.core.correct.CorrectedConsensus;
import com.milaboratory.oncomigec.core.correct.Corrector;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.haplotype.HaplotypeTree;
import com.milaboratory.oncomigec.core.io.entity.Mig;
import com.milaboratory.oncomigec.core.io.readers.MigReader;
import com.milaboratory.oncomigec.model.classifier.BaseVariantClassifier;
import com.milaboratory.oncomigec.model.classifier.VariantClassifier;
import com.milaboratory.oncomigec.model.variant.VariantContainer;
import com.milaboratory.oncomigec.model.variant.VariantLibrary;
import com.milaboratory.oncomigec.pipeline.MigecCli;
import com.milaboratory.oncomigec.util.ProcessorResultWrapper;
import org.apache.commons.math.MathException;

import java.util.LinkedList;
import java.util.List;

public class SampleAnalysis implements ReadSpecific {
    private static final boolean ENABLE_BUFFERING = true;
    protected final boolean paired;

    protected final ProjectAnalysis parent;
    protected final Sample sample;

    protected final MigReader reader;
    protected final Assembler assembler;
    protected final ConsensusAligner aligner;
    protected Corrector corrector;
    protected VariantLibrary variantLibrary;
    protected HaplotypeTree haplotypeTree;
    protected VariantClassifier variantClassifier;

    private boolean firstStageRan = false, secondStageRan = false;

    private final List<AlignedConsensus> alignmentDataList = new LinkedList<>();

    @SuppressWarnings("unchecked")
    protected SampleAnalysis(ProjectAnalysis parent,
                             Sample sample,
                             MigReader reader,
                             Assembler assembler,
                             ConsensusAligner consensusAligner) {
        this.parent = parent;
        this.sample = sample;
        this.reader = reader;
        this.paired = reader.isPairedEnd();

        if (assembler.isPairedEnd() != paired ||
                consensusAligner.isPairedEnd() != paired)
            throw new RuntimeException("All read-specific pipeline steps should have the same paired-end property.");

        this.assembler = assembler;
        this.aligner = consensusAligner;
        this.variantClassifier = BaseVariantClassifier.BUILT_IN;
    }

    public int getOverSeq(String sampleName) {
        return parent.getPresets().forceOverseq() ? parent.getPresets().getDefaultOverseq() :
                reader.getUmiHistogram(sampleName).getMigSizeThreshold();
    }

    @SuppressWarnings("unchecked")
    public void runFirstStage() throws Exception {
        if (firstStageRan)
            return;

        int overSeq = getOverSeq(sample.getName());

        reader.setCurrentSample(sample.getName());
        reader.setSizeThreshold(overSeq);

        if (parent.getPresets().filterMismatchUmis())
            reader.setMinMismatchRatio(parent.getPresets().getUmiMismatchFilterRatio());

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
                            MigecCli.print2("Running first stage of MIGEC for " + sample.getName() +
                                    ", " + count + " MIGs processed..");
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

        MigecCli.print2("Finished first stage of MIGEC for " + sample.getName() +
                ", " + countingInput.getCount() + " MIGs processed in total");
    }

    public void runSecondStage() throws MathException {
        if (!firstStageRan)
            throw new RuntimeException("Should run first stage first.");

        if (secondStageRan)
            return;

        // Find major and minor mutations
        this.corrector = new Corrector(aligner.getAlignerReferenceLibrary(),
                parent.getPresets().getCorrectorParameters(),
                variantClassifier);

        // Error statistics for haplotype filtering using binomial test
        // Store here for output summary purposes
        variantLibrary = corrector.getCorrectorReferenceLibrary().getVariantLibrary();

        // Haplotype 1-mm graph
        this.haplotypeTree = new HaplotypeTree(
                corrector.getCorrectorReferenceLibrary(),
                parent.getPresets().getHaplotypeTreeParameters());

        // Correction processing (MIGEC)
        for (AlignedConsensus alignmentData : alignmentDataList) {
            CorrectedConsensus correctedConsensus = corrector.correct(alignmentData);
            if (correctedConsensus != null)
                haplotypeTree.add(correctedConsensus);
        }

        // Haplotype filtering
        haplotypeTree.calculatePValues();

        secondStageRan = true;
    }

    public VariantContainer dumpMinorVariants(Reference reference, double threshold) {
        if (!firstStageRan)
            throw new RuntimeException("Should run first stage first.");

        AlignerReferenceLibrary alignerReferenceLibrary = aligner.getAlignerReferenceLibrary();
        MutationsAndCoverage mutationsAndCoverage = alignerReferenceLibrary.getMutationsAndCoverage(reference);
        VariantContainer variantContainer = new VariantContainer(mutationsAndCoverage, threshold);

        return variantContainer;
    }

    public MigReader getReader() {
        return reader;
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

    public HaplotypeTree getHaplotypeTree() {
        return haplotypeTree;
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

    @Override
    public boolean isPairedEnd() {
        return paired;
    }
}
