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
package com.milaboratory.mageri.preprocessing;

import com.milaboratory.core.sequencing.read.PSequencingRead;
import com.milaboratory.mageri.preprocessing.barcode.BarcodeSearcher;

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

        for (String sampleName : sampleNames) {
            slaveFoundTotal += getSlaveCounter(sampleName);
        }

        return slaveFoundTotal / total;
    }

    @Override
    public boolean isPairedEnd() {
        return true;
    }
}
