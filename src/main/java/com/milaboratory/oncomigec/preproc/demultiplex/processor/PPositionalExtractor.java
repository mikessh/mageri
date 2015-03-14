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
import com.milaboratory.oncomigec.preproc.demultiplex.barcode.BarcodeUtil;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.PCheckoutResult;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.SCheckoutResult;

import java.util.concurrent.atomic.AtomicLong;

public class PPositionalExtractor extends CheckoutProcessor<PSequencingRead, PCheckoutResult> {
    private final String sampleName;
    private final AtomicLong slaveNotFoundCounter;
    private final SPositionalExtractor extractor1, extractor2;

    public PPositionalExtractor(String sampleName, int maxOffset1, String mask1,
                                int maxOffset2, String mask2) {
        super(new String[]{sampleName}, new BarcodeSearcher[1]);
        this.sampleName = sampleName;
        this.extractor1 = new SPositionalExtractor(sampleName, maxOffset1, mask1);

        // To write the code concise (not repeat the extraction procedure from SPositionalProcessor)
        // we'll search in the slave mate as is (it is the RC in respect to master mate)
        // we are going to search for RC pattern and then transform the coordinates & RC UMI sequence
        char[] mask2RC = new char[mask2.length()];
        for (int i = 0; i < mask2.length(); i++) {
            mask2RC[mask2.length() - i - 1] = BarcodeUtil.complement(mask2.charAt(i));
        }

        this.extractor2 = new SPositionalExtractor(sampleName, maxOffset2, new String(mask2RC));
        this.slaveNotFoundCounter = new AtomicLong();
    }

    @Override
    public PCheckoutResult checkoutImpl(PSequencingRead sequencingRead) {
        SCheckoutResult extractionResult1 = extractor1.checkoutImpl(sequencingRead.getSingleRead(0)), extractionResult2;

        if (extractionResult1 == null)
            return null;

        extractionResult2 = extractor2.checkoutImpl(sequencingRead.getSingleRead(1));


        if (extractionResult2 == null) {
            slaveNotFoundCounter.incrementAndGet();
            return new PCheckoutResult(0, sampleName, true, true,
                    extractionResult1.getMasterResult(), BarcodeSearcherResult.BLANK_RESULT);
        } else {
            int len = sequencingRead.getData(1).size();

            BarcodeSearcherResult slaveResult = new BarcodeSearcherResult(
                    extractionResult2.getMasterResult().getUmi().getReverseComplement(), // we have searched in RC
                    extractionResult2.getMasterResult().getUmiWorstQual(),
                    0, 0, 0, // unused
                    len - extractionResult2.getMasterResult().getTo() + 1, // transform the coordinates, respect inclusive/exclusive
                    len - extractionResult2.getMasterResult().getFrom() - 1);

            return new PCheckoutResult(0, sampleName, true, true,
                    extractionResult1.getMasterResult(), slaveResult);
        }
    }

    public long getSlaveCounter(String sampleName) throws Exception {
        if (sampleName == null)
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
