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

package com.milaboratory.mageri.preprocessing.barcode;

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
        if (mask.length() == 0) {
            throw new IllegalArgumentException("Mask length should be > 0");
        }
        if (maxMismatchRatio < 0 || maxMismatchRatio > 1) {
            throw new IllegalArgumentException("Max mismatch ratio should be in [0,1]");
        }

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
                // e.g.  nnnNNNNtct
                //
                // "tATGCtct" case:
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
        } else {
            return new BarcodeSearcherResult(umiSQPair, goodOffset, mask.length() - goodOffset);
        }
    }

    public SlidingBarcodeSearcher getForSlave() {
        return new SlidingBarcodeSearcherR(this);
    }

    private static class SlidingBarcodeSearcherR extends SlidingBarcodeSearcher {
        public SlidingBarcodeSearcherR(SlidingBarcodeSearcher slidingBarcodeSearcher) {
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
                    read.size() - result.getTo(), // transform the coordinates, respect inclusive/exclusive
                    read.size() - result.getFrom());
        }
    }
}
