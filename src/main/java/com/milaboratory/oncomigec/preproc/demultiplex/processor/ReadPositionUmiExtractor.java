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

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.milaboratory.oncomigec.preproc.demultiplex.barcode.BarcodeSearcher;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.CheckoutResult;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.SimpleCheckoutResult;
import com.milaboratory.oncomigec.util.Util;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ReadPositionUmiExtractor extends CheckoutProcessor<CheckoutResult, SequencingRead> {
    // todo: implement
    private final String sampleName;

    public ReadPositionUmiExtractor(String sampleName, int start, int end) {
        super(new String[]{sampleName}, new BarcodeSearcher[1]);
        this.sampleName = sampleName;
    }

    @Override
    public CheckoutResult checkout(SequencingRead sequencingRead) {
        totalCounter.incrementAndGet();

        NucleotideSQPair umiSQPair = Util.extractUmiWithQual(sequencingRead.getDescription(0));

        if (umiSQPair == null) {
            masterNotFoundCounter.incrementAndGet();
            return null;
        } else
            return new SimpleCheckoutResult(sampleName, umiSQPair);
    }

    @Override
    public boolean[] getMasterFirst() {
        return new boolean[]{true};
    }


    @Override
    public boolean isPairedEnd() {
        throw new NotImplementedException();
    }
}
