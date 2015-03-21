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
package com.milaboratory.oncomigec.preprocessing.entity;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.preprocessing.barcode.BarcodeSearcherResult;

public class CheckoutResult {
    protected final int sampleId;
    protected final String sampleName;
    protected final BarcodeSearcherResult masterResult;

    protected CheckoutResult(int sampleId, String sampleName, BarcodeSearcherResult masterResult) {
        this.sampleId = sampleId;
        this.sampleName = sampleName;
        this.masterResult = masterResult;
    }

    public boolean getOrientation() {
        return true;
    }

    public boolean getMasterFirst() {
        return true;
    }

    public int getSampleId() {
        return sampleId;
    }

    public String getSampleName() {
        return sampleName;
    }

    public boolean isGood(byte umiQualThreshold) {
        return masterResult.getUmiWorstQual() >= umiQualThreshold;
    }

    public NucleotideSequence getUmi() {
        return masterResult.getUmi();
    }

    public BarcodeSearcherResult getMasterResult() {
        return masterResult;
    }
}
