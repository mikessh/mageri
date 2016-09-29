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
import com.antigenomics.mageri.misc.ProcessorResultWrapper;
import com.antigenomics.mageri.pipeline.Presets;
import com.antigenomics.mageri.pipeline.RuntimeParameters;
import com.antigenomics.mageri.pipeline.input.Input;

import java.io.IOException;

public class ProjectAnalysisRaw extends ProjectAnalysis {
    public ProjectAnalysisRaw(Input input) throws IOException {
        this(input, Presets.DEFAULT, RuntimeParameters.DEFAULT);
    }

    public ProjectAnalysisRaw(Input input,
                           Presets presets,
                           RuntimeParameters runtimeParameters) throws IOException {
        super(input, presets, runtimeParameters);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() throws Exception {
        sout("Started analysis.", 1);

        for (SampleGroup sampleGroup : project.getSampleGroups()) {
            sout("Processing sample group " + sampleGroup.getName() + ".", 1);
            final RawReadPreprocessor preprocessor = preprocessorFactory.createNoUmi(input, sampleGroup, runtimeParameters);
            preprocessor.start();

            Thread[] analysisThreads = new Thread[sampleGroup.getSamples().size()];

            for (int i = 0; i < sampleGroup.getSamples().size(); i++) {
                final Sample sample = sampleGroup.getSamples().get(i);
                final OutputPort<ProcessorResultWrapper<Consensus>> inputPort = preprocessor.createRaw(sample);
                final SampleAnalysisRaw sampleAnalysis = new SampleAnalysisRaw(
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

            preprocessor.stop();

            for (Thread analysisThread : analysisThreads) {
                analysisThread.join();
            }
        }

        sout("Done.", 1);

        write();
    }
}
