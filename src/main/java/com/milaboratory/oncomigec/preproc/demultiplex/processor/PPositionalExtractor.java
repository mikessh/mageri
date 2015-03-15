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
import com.milaboratory.oncomigec.preproc.demultiplex.barcode.BarcodeSearcher;
import com.milaboratory.oncomigec.preproc.demultiplex.barcode.BarcodeSearcherResult;
import com.milaboratory.oncomigec.preproc.demultiplex.barcode.SlidingBarcodeSearcerR;
import com.milaboratory.oncomigec.preproc.demultiplex.barcode.SlidingBarcodeSearcher;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.PCheckoutResult;
import com.sun.istack.internal.NotNull;

import java.util.concurrent.atomic.AtomicLong;

public class PPositionalExtractor extends CheckoutProcessor<PSequencingRead, PCheckoutResult> {
    private final AtomicLong slaveNotFoundCounter = new AtomicLong();
    private final String sampleName;
    private final SlidingBarcodeSearcher masterBarcode, slaveBarcode;

    public PPositionalExtractor(@NotNull String sampleName,
                                @NotNull SlidingBarcodeSearcher masterBarcode) {
        super(new String[]{sampleName}, new BarcodeSearcher[]{masterBarcode});
        this.sampleName = sampleName;
        this.masterBarcode = masterBarcode;
        this.slaveBarcode = null;
    }

    public PPositionalExtractor(@NotNull String sampleName,
                                @NotNull SlidingBarcodeSearcher masterBarcode,
                                @NotNull SlidingBarcodeSearcher slaveBarcode) {
        super(new String[]{sampleName}, new BarcodeSearcher[]{masterBarcode});
        this.sampleName = sampleName;
        this.masterBarcode = masterBarcode;
        this.slaveBarcode = new SlidingBarcodeSearcerR(slaveBarcode);
    }

    @Override
    public PCheckoutResult checkoutImpl(PSequencingRead sequencingRead) {
        BarcodeSearcherResult masterResult = masterBarcode.search(sequencingRead.getData(0)),
                slaveResult;

        if (masterResult == null)
            return null;

        if (slaveBarcode == null)
            return new PCheckoutResult(0, sampleName, true, true,
                    masterResult, BarcodeSearcherResult.BLANK_RESULT);

        slaveResult = slaveBarcode.search(sequencingRead.getData(1));

        if (slaveResult == null) {
            slaveNotFoundCounter.incrementAndGet();
            return new PCheckoutResult(0, sampleName, true, true,
                    masterResult, BarcodeSearcherResult.BLANK_RESULT);
        } else {
            return new PCheckoutResult(0, sampleName, true, true,
                    masterResult, slaveResult);
        }
    }

    public long getSlaveCounter(String sampleName) throws Exception {
        if (!this.sampleName.equals(sampleName))
            throw new Exception("Sample " + sampleName + " doesn't exist");
        return totalCounter.get() - masterNotFoundCounter.get() - slaveNotFoundCounter.get();
    }

    @Override
    public double extractionRatio() {
        double total = totalCounter.get(),
                notFoundMaster = masterNotFoundCounter.get(),
                notFoundSlave = slaveNotFoundCounter.get();
        return 1 - (notFoundMaster + notFoundSlave) / total;
    }

    @Override
    public boolean isPairedEnd() {
        return true;
    }
}
