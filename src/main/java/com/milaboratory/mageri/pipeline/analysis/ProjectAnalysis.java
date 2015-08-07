/*
 * Copyright (c) 2014-2015, Bolotin Dmitry, Chudakov Dmitry, Shugay Mikhail
 * (here and after addressed as Inventors)
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact the Inventors using one of the following
 * email addresses: chudakovdm@mail.ru, chudakovdm@gmail.com
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */

package com.milaboratory.mageri.pipeline.analysis;

import com.milaboratory.mageri.core.genomic.BasicGenomicInfoProvider;
import com.milaboratory.mageri.core.genomic.BedGenomicInfoProvider;
import com.milaboratory.mageri.core.genomic.ReferenceLibrary;
import com.milaboratory.mageri.core.input.MigOutputPort;
import com.milaboratory.mageri.core.mapping.AlignedConsensus;
import com.milaboratory.mageri.core.mapping.alignment.ExtendedKmerAlignerFactory;
import com.milaboratory.mageri.core.output.SamWriter;
import com.milaboratory.mageri.core.output.VcfWriter;
import com.milaboratory.mageri.core.variant.Variant;
import com.milaboratory.mageri.core.variant.VariantCaller;
import com.milaboratory.mageri.pipeline.Presets;
import com.milaboratory.mageri.pipeline.RuntimeParameters;
import com.milaboratory.mageri.pipeline.SerializationUtils;
import com.milaboratory.mageri.pipeline.Speaker;
import com.milaboratory.mageri.pipeline.input.Input;

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
            final Preprocessor preprocessor = preprocessorFactory.create(input, sampleGroup, runtimeParameters);

            sout("Running analysis for sample group " + sampleGroup.getName() + ".", 1);
            for (Sample sample : sampleGroup.getSamples()) {
                final MigOutputPort inputPort = preprocessor.create(sample);

                SampleAnalysis sampleAnalysis = new SampleAnalysis(
                        this, sample, preprocessor.getUmiHistogram(sample),
                        inputPort,
                        pipelineAssemblerFactory.create(sample),
                        pipelineConsensusAlignerFactory.create(sample)
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
            pipelineAssemblerFactory.writePlainText(outputPath);
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
