/*
 * Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)
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
package com.milaboratory.oncomigec.preproc.demultiplex.processor;

import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.oncomigec.preproc.demultiplex.barcode.BarcodeSearcher;
import com.milaboratory.oncomigec.preproc.demultiplex.barcode.BarcodeSearcherResult;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.SCheckoutResult;

public final class SCheckoutProcessor extends CheckoutProcessor<SCheckoutResult, SSequencingRead> {
    public SCheckoutProcessor(String[] sampleNames, BarcodeSearcher[] masterBarcodes) {
        super(sampleNames, masterBarcodes);
    }

    @Override
    public SCheckoutResult checkout(SSequencingRead read) {
        totalCounter.incrementAndGet();

        for (int i = 0; i < sampleNames.length; i++) {
            BarcodeSearcherResult barcodeSearcherResult = masterBarcodes[i].search(read.getData());
            if (barcodeSearcherResult != null) {
                return new SCheckoutResult(i, sampleNames[i], barcodeSearcherResult);
            }
        }

        masterNotFoundCounter.incrementAndGet();

        return null;
    }

    @Override
    public boolean isPairedEnd() {
        return false;
    }
}
