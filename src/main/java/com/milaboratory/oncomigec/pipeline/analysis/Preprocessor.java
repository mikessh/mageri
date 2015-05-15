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
