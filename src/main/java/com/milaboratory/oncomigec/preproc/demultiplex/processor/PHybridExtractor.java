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
 * Last modified on 15.3.2015 by mikesh
 */

package com.milaboratory.oncomigec.preproc.demultiplex.processor;

import com.milaboratory.core.sequencing.read.PSequencingRead;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.oncomigec.preproc.demultiplex.barcode.BarcodeSearcherResult;
import com.milaboratory.oncomigec.preproc.demultiplex.barcode.SeedAndExtendBarcodeSearcher;
import com.milaboratory.oncomigec.preproc.demultiplex.barcode.SlidingBarcodeSearcerR;
import com.milaboratory.oncomigec.preproc.demultiplex.barcode.SlidingBarcodeSearcher;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.PCheckoutResult;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.SCheckoutResult;

public class PHybridExtractor extends CheckoutProcessor<PSequencingRead, PCheckoutResult> {
    private final SlidingBarcodeSearcher slaveBarcode;

    public PHybridExtractor(String[] sampleNames,
                            SeedAndExtendBarcodeSearcher[] masterBarcodes,
                            SlidingBarcodeSearcher slaveBarcode) {
        super(sampleNames, masterBarcodes);
        this.slaveBarcode = new SlidingBarcodeSearcerR(slaveBarcode);
    }

    @Override
    public PCheckoutResult checkoutImpl(PSequencingRead read) {
        for (int i = 0; i < sampleNames.length; i++) {
            BarcodeSearcherResult barcodeSearcherResult = masterBarcodes[i].search(read.getData(0));
            if (barcodeSearcherResult != null) {
              //  return new SCheckoutResult(i, sampleNames[i], barcodeSearcherResult);
            }
        }

        return null;
    }

    @Override
    public boolean isPairedEnd() {
        return true;
    }
}
