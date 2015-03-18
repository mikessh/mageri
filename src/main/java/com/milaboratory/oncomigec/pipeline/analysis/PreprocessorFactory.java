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
 * Last modified on 17.3.2015 by mikesh
 */

package com.milaboratory.oncomigec.pipeline.analysis;

import com.milaboratory.oncomigec.core.PipelineBlock;
import com.milaboratory.oncomigec.core.io.misc.PreprocessorParameters;
import com.milaboratory.oncomigec.pipeline.RuntimeParameters;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.DemultiplexParameters;
import com.milaboratory.oncomigec.preproc.demultiplex.processor.CheckoutProcessor;
import com.milaboratory.oncomigec.preproc.demultiplex.processor.PAdapterExtractor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PreprocessorFactory extends PipelineBlock {
    private final DemultiplexParameters demultiplexParameters;
    private final PreprocessorParameters preprocessorParameters;
    private final Map<SampleGroup, Preprocessor> preprocessorBySampleGroup = new HashMap<>();

    public PreprocessorFactory(DemultiplexParameters demultiplexParameters, PreprocessorParameters preprocessorParameters) {
        super("checkout");
        this.demultiplexParameters = demultiplexParameters;
        this.preprocessorParameters = preprocessorParameters;
    }

    public Preprocessor create(SampleGroup sampleGroup) throws IOException, InterruptedException {
        return create(sampleGroup, RuntimeParameters.DEFAULT);
    }

    public Preprocessor create(SampleGroup sampleGroup, RuntimeParameters runtimeParameters) throws IOException, InterruptedException {
        Preprocessor preprocessor = new Preprocessor(sampleGroup,
                demultiplexParameters,
                preprocessorParameters,
                runtimeParameters);

        preprocessorBySampleGroup.put(sampleGroup, preprocessor);

        return preprocessor;
    }
    
    public Preprocessor getPreprocessor(SampleGroup sampleGroup){
        return preprocessorBySampleGroup.get(sampleGroup);
    }

    @Override
    public String getHeader() {
        return "sample.group\tsample.name\tmaster.found\tslave.found\tmaster.first\tmig.size.threshold";
    }

    @Override
    public String getBody() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Preprocessor preprocessor : preprocessorBySampleGroup.values()) {
            for (Sample sample : preprocessor.getSampleGroup().getSamples()) {
                CheckoutProcessor checkoutProcessor = preprocessor.getCheckoutProcessor();
                String sampleName = sample.getName();
                stringBuilder.append(preprocessor.getSampleGroup().getName()).append("\t").
                        append(sampleName).append("\t").
                        append(checkoutProcessor.getMasterCounter(sampleName)).append("\t").
                        append(checkoutProcessor instanceof PAdapterExtractor ?
                                ((PAdapterExtractor) checkoutProcessor).getSlaveCounter(sampleName) : "0").append("\t").
                        append(checkoutProcessor.getMasterFirstRatio()).append("\t").
                        append(preprocessor.getOverSeq(sampleName)).append("\n");
            }
        }
        return stringBuilder.toString();
    }
}
