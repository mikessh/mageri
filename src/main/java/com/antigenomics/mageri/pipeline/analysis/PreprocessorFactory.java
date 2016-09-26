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

import com.antigenomics.mageri.core.PipelineBlock;
import com.antigenomics.mageri.core.input.PreprocessorParameters;
import com.antigenomics.mageri.pipeline.RuntimeParameters;
import com.antigenomics.mageri.pipeline.input.Input;
import com.antigenomics.mageri.preprocessing.CheckoutProcessor;
import com.antigenomics.mageri.preprocessing.DemultiplexParameters;
import com.antigenomics.mageri.preprocessing.PCheckoutProcessor;

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

    public Preprocessor create(Input input, SampleGroup sampleGroup) throws IOException, InterruptedException {
        return create(input, sampleGroup, RuntimeParameters.DEFAULT);
    }

    public RawReadPreprocessor createNoUmi(Input input, SampleGroup sampleGroup) throws IOException, InterruptedException {
        return createNoUmi(input, sampleGroup, RuntimeParameters.DEFAULT);
    }

    public Preprocessor create(Input input, SampleGroup sampleGroup, RuntimeParameters runtimeParameters) throws IOException, InterruptedException {
        PreprocessorImpl preprocessor = new PreprocessorImpl(input, sampleGroup,
                demultiplexParameters,
                preprocessorParameters,
                runtimeParameters);

        preprocessorBySampleGroup.put(sampleGroup, preprocessor);

        return preprocessor;
    }

    public RawReadPreprocessor createNoUmi(Input input, SampleGroup sampleGroup, RuntimeParameters runtimeParameters) throws IOException, InterruptedException {
        RawReadPreprocessor preprocessor = new RawReadPreprocessor(input, sampleGroup,
                demultiplexParameters,
                preprocessorParameters,
                runtimeParameters);

        preprocessorBySampleGroup.put(sampleGroup, preprocessor);

        return preprocessor;
    }

    public Preprocessor getPreprocessor(SampleGroup sampleGroup) {
        return preprocessorBySampleGroup.get(sampleGroup);
    }

    @Override
    public String getHeader() {
        return "sample.group\tsample.name\ttotal\tmaster.found\tslave.found\tmaster.first\tmig.size.threshold";
    }

    @Override
    public String getBody() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Preprocessor preprocessor : preprocessorBySampleGroup.values()) {
            for (Sample sample : preprocessor.getSampleGroup().getSamples()) {
                CheckoutProcessor checkoutProcessor = preprocessor.getCheckoutProcessor();
                String sampleName = sample.getName();
                boolean paired = checkoutProcessor instanceof PCheckoutProcessor;
                stringBuilder.append(preprocessor.getSampleGroup().getName()).append("\t").
                        append(sampleName).append("\t").
                        append(checkoutProcessor.getTotal()).append("\t").
                        append(checkoutProcessor.getMasterCounter(sampleName)).append("\t").
                        append(paired ?
                                ((PCheckoutProcessor) checkoutProcessor).getSlaveCounter(sampleName) :
                                checkoutProcessor.getMasterCounter(sampleName)).append("\t").
                        append(paired ?
                                ((PCheckoutProcessor) checkoutProcessor).getMasterFirstRatio() :
                                "1").append("\t").
                        append(preprocessor.getOverSeq(sampleName)).append("\n");
            }
        }
        return stringBuilder.toString();
    }
}
