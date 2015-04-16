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
package com.milaboratory.oncomigec.preprocessing;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.preprocessing.barcode.BarcodeSearcherResult;

public final class PCheckoutResult extends CheckoutResult {
    private final boolean orientation, masterFirst;
    private final BarcodeSearcherResult slaveResult;

    public PCheckoutResult(int sampleId, String sampleName, boolean orientation, boolean masterFirst,
                           BarcodeSearcherResult masterResult, BarcodeSearcherResult slaveResult) {
        super(sampleId, sampleName, masterResult);
        this.slaveResult = slaveResult;
        this.orientation = orientation;
        this.masterFirst = masterFirst;
    }

    @Override
    public boolean getOrientation() {
        return orientation;
    }

    @Override
    public boolean getMasterFirst() {
        return masterFirst;
    }

    public boolean slaveFound() {
        return slaveResult != null;
    }

    @Override
    public NucleotideSequence getUmi() {
        if (slaveResult != null)
            return masterResult.getUmi().concatenate(slaveResult.getUmi());
        else
            return masterResult.getUmi();
    }

    @Override
    public boolean isGood(byte umiQualThreshold) {
        return slaveFound() &&
                (byte) Math.min(masterResult.getUmiWorstQual(), slaveResult.getUmiWorstQual()) >= umiQualThreshold;
    }

    public BarcodeSearcherResult getSlaveResult() {
        return slaveResult;
    }
}
