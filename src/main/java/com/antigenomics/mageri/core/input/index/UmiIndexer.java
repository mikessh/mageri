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

package com.antigenomics.mageri.core.input.index;

import cc.redberry.pipe.Processor;
import com.antigenomics.mageri.preprocessing.CheckoutProcessor;
import com.antigenomics.mageri.preprocessing.CheckoutResult;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.antigenomics.mageri.misc.ProcessorResultWrapper;

public class UmiIndexer implements Processor<SequencingRead, ProcessorResultWrapper<IndexingInfo>> {
    private final CheckoutProcessor checkoutProcessor;
    private final byte umiQualityThreshold;
    private final ReadWrappingFactory readWrappingFactory;

    public UmiIndexer(CheckoutProcessor checkoutProcessor,
                      byte umiQualityThreshold,
                      ReadWrappingFactory readWrappingFactory) {
        this.checkoutProcessor = checkoutProcessor;
        this.umiQualityThreshold = umiQualityThreshold;
        this.readWrappingFactory = readWrappingFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ProcessorResultWrapper<IndexingInfo> process(SequencingRead milibRead) {
        CheckoutResult result = checkoutProcessor.checkout(milibRead);

        if (result != null && result.isGood(umiQualityThreshold)) {
            String sampleName = result.getSampleName();
            NucleotideSequence umi = result.getUmi();
            ReadContainer readContainer = readWrappingFactory.wrap(milibRead);

            ReadInfo readInfo = new ReadInfo(readContainer, result);

            return new ProcessorResultWrapper<>(new IndexingInfo(readInfo, sampleName, umi));
        }

        return ProcessorResultWrapper.BLANK;
    }

    public CheckoutProcessor getCheckoutProcessor() {
        return checkoutProcessor;
    }
}
