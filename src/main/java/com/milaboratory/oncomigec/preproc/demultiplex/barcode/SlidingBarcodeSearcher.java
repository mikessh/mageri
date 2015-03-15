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
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.quality.SequenceQualityPhred;

public class SlidingBarcodeSearcher implements BarcodeSearcher {
    protected final int minOffset, umiPositionsCount;
    protected final boolean[] umiPositions;
    protected final String _mask, mask;
    protected final int matchPositionsCount;
    protected final double maxMismatchRatio;

    public SlidingBarcodeSearcher(String mask) {
        this(mask, 0.1);
    }

    public SlidingBarcodeSearcher(String mask, double maxMismatchRatio) {
        this.umiPositions = new boolean[mask.length()];
        int umiPositionsCount = 0, matchPositionsCount = 0, minOffset = 0;
        for (int i = 0; i < mask.length(); i++) {
            boolean hasN = mask.charAt(i) == BarcodeUtil.UMI_MARK;
            if (hasN) {
                umiPositions[i] = true;
                umiPositionsCount++;
            } else if (mask.charAt(i) == BarcodeUtil.PROTECTIVE_N) {
                // Protective n's
                // when added to the beginning of barcode, several offsets will be scanned
                // could also be added to regulate read shifting
                // E.g.  nnnNNNNtct
                //
                // tATGCtct
                // nnnNNNNtct - bad
                // nnNNNNtct  - bad
                // nNNNNtct   - good
                // NNNNtct    - bad
                minOffset++;
            } else {
                if (Character.isUpperCase(mask.charAt(i))) {
                    throw new RuntimeException("The only uppercase character allowed for " +
                            "sliding barcode searcher mask is " + BarcodeUtil.UMI_MARK);
                }
                matchPositionsCount++;
            }
        }
        this.minOffset = minOffset;
        this.umiPositionsCount = umiPositionsCount;
        this.maxMismatchRatio = maxMismatchRatio;
        this.matchPositionsCount = matchPositionsCount;
        this._mask = mask;
        this.mask = mask.toUpperCase();
    }

    @Override
    public BarcodeSearcherResult search(NucleotideSQPair read) {
        int goodOffset = -1;

        for (int i = 0; i <= minOffset; i++) {
            int mismatches = 0;
            boolean match = true;
            for (int j = 0; j < mask.length() - i; j++) {
                if (!BarcodeUtil.compareRedundant(mask.charAt(i + j),
                        read.getSequence().charFromCodeAt(j)) &&
                        (++mismatches / (double) matchPositionsCount) >= maxMismatchRatio) {
                    match = false;
                    break;
                }
            }
            if (match) {
                goodOffset = i;
                break;
            }
        }

        NucleotideSQPair umiSQPair = null;
        int i = 0;
        if (goodOffset >= 0) {
            char[] seq = new char[umiPositionsCount];
            byte[] qual = new byte[umiPositionsCount];
            for (int j = 0; j < mask.length() - goodOffset; j++) {
                if (umiPositions[goodOffset + j]) {
                    seq[i] = read.getSequence().charFromCodeAt(j);
                    qual[i] = read.getQuality().value(j);
                    i++;
                }
            }
            umiSQPair = new NucleotideSQPair(new NucleotideSequence(seq),
                    new SequenceQualityPhred(qual));
        }

        if (umiSQPair == null) {
            return null;
        } else
            return new BarcodeSearcherResult(umiSQPair, goodOffset);
    }

    public SlidingBarcodeSearcher getForSlave() {
        return new SlidingBarcodeSearcerR(this);
    }

    private static class SlidingBarcodeSearcerR extends SlidingBarcodeSearcher {
        public SlidingBarcodeSearcerR(SlidingBarcodeSearcher slidingBarcodeSearcher) {
            super(convertMask(slidingBarcodeSearcher._mask),
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
}
