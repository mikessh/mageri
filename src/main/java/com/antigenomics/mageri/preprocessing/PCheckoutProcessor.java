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

import com.antigenomics.mageri.preprocessing.barcode.BarcodeSearcher;
import com.milaboratory.core.sequencing.read.PSequencingRead;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

public abstract class PCheckoutProcessor extends CheckoutProcessor<PSequencingRead, PCheckoutResult> {
    protected final AtomicLongArray slaveCounters;
    protected final BarcodeSearcher[] slaveBarcodes;
    private final AtomicLong masterFirstCounter;

    protected PCheckoutProcessor(String sampleName,
                                 BarcodeSearcher masterBarcode) {
        this(sampleName, masterBarcode, null);
    }

    protected PCheckoutProcessor(String sampleName,
                                 BarcodeSearcher masterBarcode,
                                 BarcodeSearcher slaveBarcode) {
        this(new String[]{sampleName}, new BarcodeSearcher[]{masterBarcode}, new BarcodeSearcher[]{slaveBarcode});
    }

    protected PCheckoutProcessor(String[] sampleNames,
                                 BarcodeSearcher[] masterBarcodes) {
        this(sampleNames, masterBarcodes, new BarcodeSearcher[masterBarcodes.length]);
    }

    protected PCheckoutProcessor(String[] sampleNames,
                                 BarcodeSearcher[] masterBarcodes,
                                 BarcodeSearcher[] slaveBarcodes) {
        super(sampleNames, masterBarcodes);
        this.slaveBarcodes = slaveBarcodes;

        if (slaveBarcodes.length != masterBarcodes.length) {
            throw new IllegalArgumentException("Length of master and slave barcode arrays should be the same");
        }

        this.slaveCounters = new AtomicLongArray(slaveBarcodes.length);
        this.masterFirstCounter = new AtomicLong();
    }

    public long getSlaveCounter(String sampleName) {
        long count = 0;
        for (int id : getSampleIds(sampleName)) {
            count += slaveCounters.get(id);
        }
        return count;
    }

    @Override
    public abstract PCheckoutResult checkoutImpl(PSequencingRead read);

    @Override
    public PCheckoutResult checkout(PSequencingRead read) {
        totalCounter.incrementAndGet();
        PCheckoutResult result = checkoutImpl(read);

        if (result != null) {
            goodCounter.incrementAndGet();
            int sampleId = result.getSampleId();
            masterCounters.incrementAndGet(sampleId);

            if (result.getOrientation()) {
                masterFirstCounter.incrementAndGet();
            }

            if (result.slaveFound()) {
                slaveCounters.incrementAndGet(sampleId);
            } else {
                return null; // only master+slave match can go further
            }
        }

        return result;
    }

    public double getMasterFirstRatio() {
        return (double) masterFirstCounter.get() / goodCounter.get();
    }

    @Override
    public double extractionRatio() {
        double total = totalCounter.get(), slaveFoundTotal = 0;

        for (int i = 0; i < slaveCounters.length(); i++) {
            slaveFoundTotal += slaveCounters.get(i);
        }

        return slaveFoundTotal / total;
    }

    @Override
    public boolean isPairedEnd() {
        return true;
    }
}
