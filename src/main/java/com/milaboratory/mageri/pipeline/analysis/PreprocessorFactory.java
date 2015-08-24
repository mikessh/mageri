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

import com.milaboratory.mageri.core.PipelineBlock;
import com.milaboratory.mageri.core.input.PreprocessorParameters;
import com.milaboratory.mageri.pipeline.RuntimeParameters;
import com.milaboratory.mageri.pipeline.input.Input;
import com.milaboratory.mageri.preprocessing.CheckoutProcessor;
import com.milaboratory.mageri.preprocessing.DemultiplexParameters;
import com.milaboratory.mageri.preprocessing.PCheckoutProcessor;

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

    public Preprocessor create(Input input, SampleGroup sampleGroup, RuntimeParameters runtimeParameters) throws IOException, InterruptedException {
        Preprocessor preprocessor = new Preprocessor(input, sampleGroup,
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
        return "sample.group\tsample.name\tmaster.found\tslave.found\tmaster.first\tmig.size.threshold";
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
