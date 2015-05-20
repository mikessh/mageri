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

package com.milaboratory.oncomigec.preprocessing;

import com.milaboratory.core.sequencing.read.PSequencingRead;
import com.milaboratory.oncomigec.preprocessing.barcode.BarcodeSearcher;
import com.milaboratory.oncomigec.preprocessing.barcode.BarcodeSearcherResult;
import com.milaboratory.oncomigec.preprocessing.barcode.SlidingBarcodeSearcher;

import java.util.concurrent.atomic.AtomicLong;

public class PPositionalExtractor extends CheckoutProcessor<PSequencingRead, PCheckoutResult> {
    private final AtomicLong slaveCounter = new AtomicLong();
    private final String sampleName;
    private final SlidingBarcodeSearcher masterBarcode, slaveBarcode;

    public PPositionalExtractor(String sampleName,
                                SlidingBarcodeSearcher masterBarcode) {
        super(new String[]{sampleName}, new BarcodeSearcher[]{masterBarcode});
        this.sampleName = sampleName;
        this.masterBarcode = masterBarcode;
        this.slaveBarcode = null;
    }

    public PPositionalExtractor(String sampleName,
                                String mask1) {
        this(sampleName, new SlidingBarcodeSearcher(mask1));
    }

    public PPositionalExtractor(String sampleName,
                                SlidingBarcodeSearcher masterBarcode,
                                SlidingBarcodeSearcher slaveBarcode) {
        super(new String[]{sampleName}, new BarcodeSearcher[]{masterBarcode});
        this.sampleName = sampleName;
        this.masterBarcode = masterBarcode;
        this.slaveBarcode = slaveBarcode.getForSlave();
    }

    public PPositionalExtractor(String sampleName,
                                String mask1,
                                String mask2) {
        this(sampleName, new SlidingBarcodeSearcher(mask1), new SlidingBarcodeSearcher(mask2));
    }

    @Override
    public PCheckoutResult checkoutImpl(PSequencingRead sequencingRead) {
        BarcodeSearcherResult masterResult = masterBarcode.search(sequencingRead.getData(0)),
                slaveResult;

        if (masterResult == null) {
            return null;
        }

        if (slaveBarcode == null) {
            slaveCounter.incrementAndGet();
            return new PCheckoutResult(0, sampleName, true, true,
                    masterResult, BarcodeSearcherResult.BLANK_RESULT);
        }

        slaveResult = slaveBarcode.search(sequencingRead.getData(1));

        if (slaveResult != null) {
            slaveCounter.incrementAndGet();
        }

        return new PCheckoutResult(0, sampleName, true, true,
                masterResult, slaveResult);
    }

    public long getSlaveCounter(String sampleName) throws Exception {
        if (!this.sampleName.equals(sampleName))
            throw new Exception("Sample " + sampleName + " doesn't exist");
        return slaveCounter.get();
    }

    @Override
    public double extractionRatio() {
        return slaveCounter.get() / (double) totalCounter.get();
    }

    @Override
    public boolean isPairedEnd() {
        return true;
    }
}
