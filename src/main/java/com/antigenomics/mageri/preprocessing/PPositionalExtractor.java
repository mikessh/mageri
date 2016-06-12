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

package com.antigenomics.mageri.preprocessing;

import com.antigenomics.mageri.preprocessing.barcode.BarcodeSearcherResult;
import com.milaboratory.core.sequencing.read.PSequencingRead;
import com.antigenomics.mageri.preprocessing.barcode.SlidingBarcodeSearcher;

public class PPositionalExtractor extends PCheckoutProcessor {
    public PPositionalExtractor(String sampleName,
                                SlidingBarcodeSearcher masterBarcode) {
        super(sampleName, masterBarcode);
    }

    public PPositionalExtractor(String sampleName,
                                String mask1) {
        this(sampleName, new SlidingBarcodeSearcher(mask1));
    }

    public PPositionalExtractor(String sampleName,
                                SlidingBarcodeSearcher masterBarcode,
                                SlidingBarcodeSearcher slaveBarcode) {
        super(sampleName, masterBarcode,
                // crucial optimization here, we create a wrapper to be used internally
                // with no need to do reverse complement on second read:
                slaveBarcode.getForSlave());
    }

    public PPositionalExtractor(String sampleName,
                                String mask1,
                                String mask2) {
        this(sampleName, new SlidingBarcodeSearcher(mask1), new SlidingBarcodeSearcher(mask2));
    }

    @Override
    public PCheckoutResult checkoutImpl(PSequencingRead sequencingRead) {
        BarcodeSearcherResult masterResult = masterBarcodes[0].search(sequencingRead.getData(0));

        if (masterResult == null) {
            return null;
        }

        return new PCheckoutResult(0, sampleNames[0], true, true,
                masterResult,
                slaveBarcodes[0] != null ?
                        // should be run only with *.getForSlave() SlidingBarcodeSearcher
                        slaveBarcodes[0].search(sequencingRead.getData(1)) :
                        BarcodeSearcherResult.BLANK_RESULT);
    }
}
