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
import com.milaboratory.oncomigec.core.assemble.processor.PAssembler;
import com.milaboratory.oncomigec.core.assemble.processor.SAssembler;
import com.milaboratory.oncomigec.core.consalign.processor.PConsensusAligner;
import com.milaboratory.oncomigec.core.genomic.PrimerSet;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.core.io.readers.MigReader;
import com.milaboratory.oncomigec.pipeline.Presets;
import com.milaboratory.oncomigec.pipeline.RuntimeParameters;
import com.milaboratory.oncomigec.pipeline.input.Input;

import java.util.Map;
import java.util.TreeMap;

public class ProjectAnalysis {
    protected final ReferenceLibrary referenceLibrary;
    protected final PrimerSet primers;
    protected final Project project;
    protected final Presets presets;
    protected final RuntimeParameters runtimeParameters;
    protected final AssemblerFactory

    private final Map<Sample, SampleAnalysis> processorBySample = new TreeMap<>();

    public ProjectAnalysis(Input input,
                           Presets presets,
                           RuntimeParameters runtimeParameters) {
        this.presets = presets;
        this.runtimeParameters = runtimeParameters;
        this.project = Project.fromInput(input);

        this.referenceLibrary = ReferenceLibrary.fromInput(input.getReferences());
        this.primers = PrimerSet.fromInput(input.getPrimers());


        
    }
    
    public void preprocess() {
        final AssemblerFactory pAssemblerFactory = new PAssemblerFactory(presets.getAssemblerParameters()), 
                sAssemblerFactory = new SAssemblerFactory(presets.getAssemblerParameters());
        
        final ExtendedExomeAlignerFactory
        
        for (Sample sample : project.getSamples()) {
            MigReader reader = preprocess(sample);
            SampleAnalysis sampleAnalysis = reader.isPairedEnd() ? new SampleAnalysis(this, sample,
                    reader,
                    new PAssembler(presets.getAssemblerParameters()),
                    new PConsensusAligner()
            )
            //processorBySample.
        }
        
    }

    private MigReader prepareReader(Sample sample) {
        // use primers
        return null;

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
}
