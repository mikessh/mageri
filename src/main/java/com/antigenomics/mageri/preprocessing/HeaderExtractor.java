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
import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequencing.read.SequencingRead;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public final class HeaderExtractor extends CheckoutProcessor<SequencingRead, CheckoutResult> {
    public HeaderExtractor(String sampleName) {
        super(new String[]{sampleName}, new BarcodeSearcher[]{null});
    }

    @Override
    public CheckoutResult checkoutImpl(SequencingRead sequencingRead) {
        NucleotideSQPair umiSQPair = extractUmiWithQual(sequencingRead.getDescription(0));

        if (umiSQPair == null) {
            return null;
        } else {
            return new SimpleCheckoutResult(sampleNames[0], umiSQPair);
        }
    }

    @Override
    public boolean isPairedEnd() {
        throw new NotImplementedException();
    }

    public static final String UMI_FIELD_ID = "UMI";
    public static final int UMI_QUAL_OFFSET = UMI_FIELD_ID.length() + 2;

    public static NucleotideSQPair extractUmiWithQual(String header) {
        for (String field : header.split("[ \t]")) {
            if (field.startsWith(UMI_FIELD_ID)) {
                String seq = field.split(":")[1];
                String qual = field.substring(UMI_QUAL_OFFSET + seq.length());
                return new NucleotideSQPair(seq, qual);
            }
        }

        return null;
    }
}
