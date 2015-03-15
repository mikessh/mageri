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
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.quality.SequenceQualityPhred;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.oncomigec.preproc.demultiplex.barcode.BarcodeSearcher;
import com.milaboratory.oncomigec.preproc.demultiplex.barcode.BarcodeSearcherResult;
import com.milaboratory.oncomigec.preproc.demultiplex.barcode.BarcodeUtil;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.SCheckoutResult;

public class SPositionalExtractor extends CheckoutProcessor<SSequencingRead, SCheckoutResult> {
    private final String sampleName;
    private final int maxOffset;
    private final boolean[] umiPositions;
    private final String mask;
    private final int matchPositionsCount;
    private final double maxMismatchRatio = 0.1;

    public SPositionalExtractor(String sampleName, int maxOffset, String mask) {
        super(new String[]{sampleName}, new BarcodeSearcher[1]);
        this.sampleName = sampleName;
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
        this.matchPositionsCount = matchPositionsCount;
        this.mask = mask.toUpperCase();
    }

    @Override
    public SCheckoutResult checkoutImpl(SSequencingRead sequencingRead) {
        int goodOffset = -1;

        for (int i = 0; i <= maxOffset; i++) {
            int mismatches = 0;
            boolean match = true;
            for (int j = 0; j < mask.length(); j++) {
                if (!BarcodeUtil.compareRedundant(mask.charAt(j),
                        sequencingRead.getData().getSequence().charFromCodeAt(i + j)) &&
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
                    seq[j] = sequencingRead.getData().getSequence().charFromCodeAt(goodOffset + j);
                    qual[j] = sequencingRead.getData().getQuality().value(goodOffset + j);
                }
            }
            umiSQPair = new NucleotideSQPair(new NucleotideSequence(seq),
                    new SequenceQualityPhred(qual));
        }

        if (umiSQPair == null) {
            return null;
        } else
            return new SCheckoutResult(0, sampleName, new BarcodeSearcherResult(umiSQPair, goodOffset));
    }

    @Override
    public boolean isPairedEnd() {
        return false;
    }
}
