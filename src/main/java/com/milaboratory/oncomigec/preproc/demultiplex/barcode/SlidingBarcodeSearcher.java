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
    protected final int maxOffset;
    protected final boolean[] umiPositions;
    protected final String mask;
    protected final int matchPositionsCount;
    protected final double maxMismatchRatio;

    public SlidingBarcodeSearcher(int maxOffset, String mask) {
        this(maxOffset, mask, 0.1);
    }

    public SlidingBarcodeSearcher(int maxOffset, String mask, double maxMismatchRatio) {
        this.maxOffset = maxOffset;
        this.umiPositions = new boolean[mask.length()];
        int matchPositionsCount = 0;
        for (int i = 0; i < mask.length(); i++) {
            boolean hasN = mask.charAt(i) == 'N';
            if (hasN) {
                umiPositions[i] = true;
            } else {
                matchPositionsCount++;
            }
        }
        this.maxMismatchRatio = maxMismatchRatio;
        this.matchPositionsCount = matchPositionsCount;
        this.mask = mask.toUpperCase();
    }

    @Override
    public BarcodeSearcherResult search(NucleotideSQPair read) {
        int goodOffset = -1;

        for (int i = 0; i <= maxOffset; i++) {
            int mismatches = 0;
            boolean match = true;
            for (int j = 0; j < mask.length(); j++) {
                if (!BarcodeUtil.compareRedundant(mask.charAt(j),
                        read.getSequence().charFromCodeAt(i + j)) &&
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
        if (goodOffset >= 0) {
            char[] seq = new char[mask.length()];
            byte[] qual = new byte[mask.length()];
            for (int j = 0; j < mask.length(); j++) {
                if (umiPositions[j]) {
                    seq[j] = read.getSequence().charFromCodeAt(goodOffset + j);
                    qual[j] = read.getQuality().value(goodOffset + j);
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
}
