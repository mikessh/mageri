/**
 * Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)
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
 */

package com.milaboratory.oncomigec.preproc.demultiplex.processor;

import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.oncomigec.preproc.demultiplex.barcode.BarcodeSearcher;
import com.milaboratory.oncomigec.preproc.demultiplex.barcode.BarcodeSearcherResult;
import com.milaboratory.oncomigec.preproc.demultiplex.barcode.SlidingBarcodeSearcher;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.SCheckoutResult;

public class SPositionalExtractor extends CheckoutProcessor<SSequencingRead, SCheckoutResult> {
    private final String sampleName;
    private final SlidingBarcodeSearcher masterBarcode;

    public SPositionalExtractor(String sampleName, SlidingBarcodeSearcher masterBarcode) {
        super(new String[]{sampleName}, new BarcodeSearcher[]{masterBarcode});
        this.sampleName = sampleName;
        this.masterBarcode = masterBarcode;
    }

    @Override
    public SCheckoutResult checkoutImpl(SSequencingRead sequencingRead) {
        BarcodeSearcherResult result = masterBarcode.search(sequencingRead.getData());

        if (result == null) {
            return null;
        } else
            return new SCheckoutResult(0, sampleName, result);
    }

    @Override
    public boolean isPairedEnd() {
        return false;
    }
}
