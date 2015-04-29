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
 * Last modified on 13.3.2015 by mikesh
 */

package com.milaboratory.oncomigec.pipeline.analysis;

import com.milaboratory.oncomigec.core.Mig;
import com.milaboratory.oncomigec.core.ReadSpecific;
import com.milaboratory.oncomigec.core.input.*;
import com.milaboratory.oncomigec.pipeline.RuntimeParameters;
import com.milaboratory.oncomigec.pipeline.input.CheckoutRule;
import com.milaboratory.oncomigec.pipeline.input.Input;
import com.milaboratory.oncomigec.pipeline.input.InputChunk;
import com.milaboratory.oncomigec.preprocessing.CheckoutProcessor;
import com.milaboratory.oncomigec.preprocessing.DemultiplexParameters;

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