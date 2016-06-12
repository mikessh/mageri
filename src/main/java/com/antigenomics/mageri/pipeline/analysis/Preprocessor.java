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

import com.antigenomics.mageri.core.input.*;
import com.antigenomics.mageri.pipeline.RuntimeParameters;
import com.antigenomics.mageri.pipeline.input.CheckoutRule;
import com.antigenomics.mageri.pipeline.input.Input;
import com.antigenomics.mageri.pipeline.input.InputChunk;
import com.antigenomics.mageri.preprocessing.CheckoutProcessor;
import com.antigenomics.mageri.preprocessing.DemultiplexParameters;
import com.antigenomics.mageri.core.Mig;
import com.antigenomics.mageri.core.ReadSpecific;

import java.io.IOException;
import java.io.Serializable;

public class Preprocessor<MigType extends Mig> implements ReadSpecific, Serializable {
    private final PreprocessorParameters preprocessorParameters;
    private final SampleGroup sampleGroup;
    private final MigReader migReader;

    public Preprocessor(Input input, SampleGroup sampleGroup) throws IOException, InterruptedException {
        this(input, sampleGroup, DemultiplexParameters.DEFAULT, PreprocessorParameters.DEFAULT);
    }

    public Preprocessor(Input input, SampleGroup sampleGroup,
                        DemultiplexParameters demultiplexParameters,
                        PreprocessorParameters preprocessorParameters) throws IOException, InterruptedException {
        this(input, sampleGroup, demultiplexParameters, preprocessorParameters, RuntimeParameters.DEFAULT);
    }

    public Preprocessor(Input input, SampleGroup sampleGroup,
                        DemultiplexParameters demultiplexParameters,
                        PreprocessorParameters preprocessorParameters,
                        RuntimeParameters runtimeParameters) throws IOException, InterruptedException {
        this.preprocessorParameters = preprocessorParameters;
        this.sampleGroup = sampleGroup;
        InputChunk inputChunk = input.getByName(sampleGroup.getName());

        CheckoutRule checkoutRule = inputChunk.getCheckoutRule();

        checkoutRule.setDemultiplexParameters(demultiplexParameters);

        this.migReader = inputChunk.isPairedEnd() ?
                new PMigReader(inputChunk.getInputStream1(), inputChunk.getInputStream2(),
                        checkoutRule.getProcessor(), preprocessorParameters, runtimeParameters)
                :
                new SMigReader(inputChunk.getInputStream1(),
                        checkoutRule.getProcessor(), preprocessorParameters, runtimeParameters);
    }

    public MigSizeDistribution getUmiHistogram(Sample sample) {
        if (!sampleGroup.getSamples().contains(sample))
            throw new RuntimeException("Sample " + sample + " not found in sample group " + sampleGroup);

        return migReader.getUmiHistogram(sample.getName());
    }

    @SuppressWarnings("unchecked")
    public MigOutputPort<MigType> create(Sample sample) {
        if (!sampleGroup.getSamples().contains(sample))
            throw new RuntimeException("Sample " + sample + " not found in sample group " + sampleGroup);

        String sampleName = sample.getName();

        return new MigOutputPort<>(migReader, sample, getOverSeq(sampleName));
    }

    public int getOverSeq(String sampleName) {
        return preprocessorParameters.forceOverseq() ?
                preprocessorParameters.getDefaultOverseq() :
                migReader.getUmiHistogram(sampleName).getMigSizeThreshold();
    }

    public SampleGroup getSampleGroup() {
        return sampleGroup;
    }

    public CheckoutProcessor getCheckoutProcessor() {
        return migReader.getCheckoutProcessor();
    }

    @Override
    public boolean isPairedEnd() {
        return migReader.isPairedEnd();
    }
}
