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

package com.milaboratory.oncomigec.preproc.demultiplex.barcode;

import com.milaboratory.core.sequence.NucleotideSQPair;

public class SlidingBarcodeSearcerR extends SlidingBarcodeSearcher {
    public SlidingBarcodeSearcerR(SlidingBarcodeSearcher slidingBarcodeSearcher) {
        super(slidingBarcodeSearcher.maxOffset,
                convertMask(slidingBarcodeSearcher.mask),
                slidingBarcodeSearcher.maxMismatchRatio);
    }

    private static String convertMask(String mask) {
        // To write the code concise (not repeat the extraction procedure from SPositionalProcessor)
        // we'll search in the slave mate as is (it is the RC in respect to master mate)
        // we are going to search for RC pattern and then transform the coordinates & RC UMI sequence
        char[] maskRC = new char[mask.length()];
        for (int i = 0; i < mask.length(); i++) {
            maskRC[mask.length() - i - 1] = BarcodeUtil.complement(mask.charAt(i));
        }

        return new String(maskRC);
    }

    @Override
    public BarcodeSearcherResult search(NucleotideSQPair read) {
        BarcodeSearcherResult result = super.search(read);

        if (result == null)
            return null;

        return new BarcodeSearcherResult(
                result.getUmi().getReverseComplement(), // we have searched in RC
                result.getUmiWorstQual(),
                0, 0, 0, // unused
                read.size() - result.getTo() + 1, // transform the coordinates, respect inclusive/exclusive
                read.size() - result.getFrom() - 1);
    }
}
