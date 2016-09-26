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

package com.antigenomics.mageri.pipeline.analysis;

import cc.redberry.pipe.OutputPort;
import com.antigenomics.mageri.core.assemble.Consensus;
import com.antigenomics.mageri.core.genomic.BedGenomicInfoProvider;
import com.antigenomics.mageri.core.input.MigOutputPort;
import com.antigenomics.mageri.core.mapping.AlignedConsensus;
import com.antigenomics.mageri.core.mapping.alignment.ExtendedKmerAlignerFactory;
import com.antigenomics.mageri.core.output.SamWriter;
import com.antigenomics.mageri.core.output.VcfWriter;
import com.antigenomics.mageri.core.variant.Variant;
import com.antigenomics.mageri.misc.ProcessorResultWrapper;
import com.antigenomics.mageri.pipeline.RuntimeParameters;
import com.antigenomics.mageri.pipeline.SerializationUtils;
import com.antigenomics.mageri.pipeline.input.Input;
import com.antigenomics.mageri.core.genomic.BasicGenomicInfoProvider;
import com.antigenomics.mageri.core.genomic.ReferenceLibrary;
import com.antigenomics.mageri.core.variant.VariantCaller;
import com.antigenomics.mageri.pipeline.Presets;
import com.antigenomics.mageri.pipeline.Speaker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ProjectAnalysis implements Serializable {
    protected final ReferenceLibrary referenceLibrary;
    protected final Project project;
    protected final Input input;
    protected final Presets presets;
    protected final RuntimeParameters runtimeParameters;
    protected String outputPath = null;
    protected boolean writeBinary = false;

    private final PreprocessorFactory preprocessorFactory;
    private transient final PipelineAssemblerFactory pipelineAssemblerFactory;
    private transient final PipelineConsensusAlignerFactory pipelineConsensusAlignerFactory;

    private final Map<Sample, SampleAnalysis> analysisBySample = new TreeMap<>();

    public ProjectAnalysis(Input input) throws IOException {
        this(input, Presets.DEFAULT, RuntimeParameters.DEFAULT);
    }

    public ProjectAnalysis(Input input,
                           Presets presets,
                           RuntimeParameters runtimeParameters) throws IOException {
        this.presets = presets;
        this.runtimeParameters = runtimeParameters;
        this.input = input;
        this.project = Project.fromInput(input);

        this.referenceLibrary =
                ReferenceLibrary.fromInput(input.getReferences(), input.hasBedInfo() ?
                        new BedGenomicInfoProvider(input.getBedFile(), input.getContigFile()) :
                        new BasicGenomicInfoProvider());

        this.preprocessorFactory = new PreprocessorFactory(presets.getDemultiplexParameters(),
                presets.getPreprocessorParameters());

        this.pipelineAssemblerFactory = new PipelineAssemblerFactory(presets.getPreprocessorParameters(),
                presets.getAssemblerParameters());

        ExtendedKmerAlignerFactory alignerFactory = new ExtendedKmerAlignerFactory(referenceLibrary);

        this.pipelineConsensusAlignerFactory = new PipelineConsensusAlignerFactory(alignerFactory,
                presets.getConsensusAlignerParameters());
    }

    private void sout(String message, int verbosityLevel) {
        Speaker.INSTANCE.sout("[" + project.getName() + "] " +
                message, verbosityLevel);
    }

    public void run() throws Exception {
        sout("Started analysis.", 1);

        for (SampleGroup sampleGroup : project.getSampleGroups()) {
            sout("Pre-processing sample group " + sampleGroup.getName() + ".", 1);
            Preprocessor preprocessor = preprocessorFactory.create(input, sampleGroup, runtimeParameters);

            sout("Running analysis for sample group " + sampleGroup.getName() + ".", 1);
            for (Sample sample : sampleGroup.getSamples()) {
                MigOutputPort inputPort = preprocessor.create(sample);

                SampleAnalysis sampleAnalysis = new SampleAnalysis(
                        this, sample, preprocessor.getUmiHistogram(sample),
                        inputPort,
                        pipelineAssemblerFactory.create(sample),
                        pipelineConsensusAlignerFactory.create(sample),
                        inputPort.isPairedEnd()
                );

                sampleAnalysis.run();

                analysisBySample.put(sample, sampleAnalysis);

                // don't need reads associated with current sample anymore in memory
                inputPort.clear();
            }
        }

        sout("Done.", 1);

        write();
    }

    @SuppressWarnings("unchecked")
    public void runNoUmi() throws Exception {
        sout("Started analysis.", 1);

        for (SampleGroup sampleGroup : project.getSampleGroups()) {
            sout("Processing sample group " + sampleGroup.getName() + ".", 1);
            final RawReadPreprocessor preprocessor = preprocessorFactory.createNoUmi(input, sampleGroup, runtimeParameters);
            preprocessor.start();

            Thread[] analysisThreads = new Thread[sampleGroup.getSamples().size()];

            for (int i = 0; i < sampleGroup.getSamples().size(); i++) {
                final Sample sample = sampleGroup.getSamples().get(i);
                final OutputPort<ProcessorResultWrapper<Consensus>> inputPort = preprocessor.createRaw(sample);
                final SampleAnalysis sampleAnalysis = new SampleAnalysis(
                        this, sample, null,
                        inputPort, null,
                        pipelineConsensusAlignerFactory.create(sample),
                        preprocessor.isPairedEnd()
                );

                analysisThreads[i] = new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    sampleAnalysis.run();
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                );

                analysisBySample.put(sample, sampleAnalysis);

                analysisThreads[i].start();
            }

            for (Thread analysisThread : analysisThreads) {
                analysisThread.join();
            }
        }

        sout("Done.", 1);

        write();
    }

    private void write() throws IOException {
        if (outputPath != null) {
            sout("Writing output.", 1);

            for (SampleGroup sampleGroup : project.getSampleGroups()) {
                for (Sample sample : sampleGroup.getSamples()) {
                    SampleAnalysis sampleAnalysis = getAnalysis(sample);

                    String prefix = sampleAnalysis.getOutputPrefix();

                    // Write SAM file
                    SamWriter samWriter = new SamWriter(sample,
                            new FileOutputStream(prefix + ".sam"), sampleAnalysis.getConsensusAligner(),
                            presets.getPlatform());

                    for (AlignedConsensus alignedConsensus : sampleAnalysis.getAlignmentDataList()) {
                        samWriter.write(alignedConsensus);
                    }

                    samWriter.close();

                    // Write VCF file
                    VariantCaller variantCaller = sampleAnalysis.getVariantCaller();
                    VcfWriter vcfWriter = new VcfWriter(sample,
                            new FileOutputStream(prefix + ".vcf"), variantCaller,
                            presets.getPlatform());

                    for (Variant variant : variantCaller.getVariants()) {
                        vcfWriter.write(variant);
                    }

                    vcfWriter.close();
                }
            }

            String outputPath = this.outputPath + project.getName();

            preprocessorFactory.writePlainText(outputPath);
            if(pipelineAssemblerFactory.wasUsed()) {
                pipelineAssemblerFactory.writePlainText(outputPath);
            }
            pipelineConsensusAlignerFactory.writePlainText(outputPath);

            if (writeBinary) {
                SerializationUtils.writeObjectToFile(new File(outputPath + ".mi"), this);
            }

            sout("Done.", 1);
        }
    }

    public ReferenceLibrary getReferenceLibrary() {
        return referenceLibrary;
    }

    public Project getProject() {
        return project;
    }

    public Presets getPresets() {
        return presets;
    }

    public RuntimeParameters getRuntimeParameters() {
        return runtimeParameters;
    }

    public SampleAnalysis getAnalysis(Sample sample) {
        return analysisBySample.get(sample);
    }

    public void setOutputPath(String outputPath) {
        if (!outputPath.endsWith(File.separator)) {
            outputPath += File.separator;
        }
        this.outputPath = outputPath;
    }

    public void setWriteBinary(boolean writeBinary) {
        this.writeBinary = writeBinary;
    }

    public Preprocessor getPreprocessor(SampleGroup sampleGroup) {
        return preprocessorFactory.getPreprocessor(sampleGroup);
    }

    public List<SampleAnalysis> getAnalysis(SampleGroup sampleGroup) {
        List<SampleAnalysis> analysises = new ArrayList<>();
        for (Sample sample : sampleGroup.getSamples()) {
            analysises.add(getAnalysis(sample));
        }
        return analysises;
    }
}
