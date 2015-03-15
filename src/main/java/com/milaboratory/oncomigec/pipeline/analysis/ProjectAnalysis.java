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

import com.milaboratory.oncomigec.core.align.processor.aligners.ExtendedExomeAlignerFactory;
import com.milaboratory.oncomigec.core.assemble.misc.AssemblerFactory;
import com.milaboratory.oncomigec.core.assemble.misc.PAssemblerFactory;
import com.milaboratory.oncomigec.core.assemble.misc.SAssemblerFactory;
import com.milaboratory.oncomigec.core.consalign.misc.ConsensusAlignerFactory;
import com.milaboratory.oncomigec.core.consalign.misc.PConsensusAlignerFactory;
import com.milaboratory.oncomigec.core.consalign.misc.SConsensusAlignerFactory;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.core.io.readers.MigOutputPort;
import com.milaboratory.oncomigec.pipeline.Presets;
import com.milaboratory.oncomigec.pipeline.RuntimeParameters;
import com.milaboratory.oncomigec.pipeline.input.Input;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ProjectAnalysis {
    protected final ReferenceLibrary referenceLibrary;
    protected final Project project;
    protected final Presets presets;
    protected final RuntimeParameters runtimeParameters;
    private final AssemblerFactory pAssemblerFactory, sAssemblerFactory;
    private final ConsensusAlignerFactory pConsensusAlignerFactory, sConsensusAlignerFactory;

    private final Map<Sample, SampleAnalysis> analysisBySample = new TreeMap<>();

    public ProjectAnalysis(Input input,
                           Presets presets,
                           RuntimeParameters runtimeParameters) {
        this.presets = presets;
        this.runtimeParameters = runtimeParameters;
        this.project = Project.fromInput(input);

        this.referenceLibrary = ReferenceLibrary.fromInput(input.getReferences());

        this.pAssemblerFactory = new PAssemblerFactory(presets.getAssemblerParameters());
        this.sAssemblerFactory = new SAssemblerFactory(presets.getAssemblerParameters());

        ExtendedExomeAlignerFactory alignerFactory = new ExtendedExomeAlignerFactory(referenceLibrary);

        this.pConsensusAlignerFactory = new PConsensusAlignerFactory(alignerFactory, presets.getConsensusAlignerParameters());
        this.sConsensusAlignerFactory = new SConsensusAlignerFactory(alignerFactory, presets.getConsensusAlignerParameters());

        // Create reader factory
    }

    public void run() throws Exception {
        for (SampleGroup sampleGroup : project.getSampleGroups()) {
            Preprocessor preprocessor = new Preprocessor(presets, sampleGroup);
            for (Sample sample : sampleGroup.getSamples()) {
                MigOutputPort reader = preprocessor.create(sample);

                SampleAnalysis sampleAnalysis = reader.isPairedEnd() ? new SampleAnalysis(
                        this, sample, preprocessor.getUmiHistogram(sample),
                        reader,
                        pAssemblerFactory.create(),
                        pConsensusAlignerFactory.create()
                ) : new SampleAnalysis(
                        this, sample, preprocessor.getUmiHistogram(sample),
                        reader,
                        sAssemblerFactory.create(),
                        sConsensusAlignerFactory.create()
                );

                sampleAnalysis.runFirstStage();

                sampleAnalysis.runSecondStage();

                analysisBySample.put(sample, sampleAnalysis);
            }
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

    public List<SampleAnalysis> getAnalysis(SampleGroup sampleGroup) {
        List<SampleAnalysis> analysises = new ArrayList<>();
        for (Sample sample : sampleGroup.getSamples()) {
            analysises.add(getAnalysis(sample));
        }
        return analysises;
    }
}
