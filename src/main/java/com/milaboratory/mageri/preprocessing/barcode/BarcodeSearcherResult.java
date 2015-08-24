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

public class BarcodeSearcherResult {
    public static BarcodeSearcherResult BLANK_RESULT = new BarcodeSearcherResult(
            new NucleotideSequence(""), (byte) 40,
            0, 0, 0, 0, 0);

    private final NucleotideSequence umi;
    private final byte umiWorstQual;
    private final int goodMMs, lowQualityMMs, truncations, from, to;

    public BarcodeSearcherResult(NucleotideSequence umi, byte umiWorstQual,
                                 int goodMMs, int lowQualityMMs, int truncations,
                                 int from, int to) {
        this.umi = umi;
        this.umiWorstQual = umiWorstQual;
        this.goodMMs = goodMMs;
        this.lowQualityMMs = lowQualityMMs;
        this.truncations = truncations;
        this.from = from;
        this.to = to;

        assert from <= to;
    }

    public BarcodeSearcherResult(NucleotideSQPair umiSQPair) {
        this(umiSQPair.getSequence(), umiSQPair.getQuality().minValue(),
                0, 0, 0, 0, 0);
    }

    public BarcodeSearcherResult(NucleotideSQPair umiSQPair, int from, int matchLen) {
        this(umiSQPair.getSequence(), umiSQPair.getQuality().minValue(),
                0, 0, 0, from, from + matchLen);
    }

    public NucleotideSequence getUmi() {
        return umi;
    }

    public byte getUmiWorstQual() {
        return umiWorstQual;
    }

    public int getGoodMMs() {
        return goodMMs;
    }

    public int getLowQualityMMs() {
        return lowQualityMMs;
    }

    public int getTruncations() {
        return truncations;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public int getMatchSize() {
        return to - from;
    }

    /**
     * Reports whether the adapter sequence was matched.
     * Used at adapter trimming stage. Note that there can be
     * a situation when barcode search is considered successful
     * ({@code this != null}) yet there is no adapter sequence,
     * e.g. for dummy barcodes and simple header matching.
     *
     * @return
     */
    public boolean hasAdapterMatch() {
        return to > from;
    }

    @Override
    public String toString() {
        return "BarcodeSearcherResult{" +
                "umi='" + umi + '\'' +
                ", umiWorstQual=" + umiWorstQual +
                ", goodMMs=" + goodMMs +
                ", lowQualityMMs=" + lowQualityMMs +
                ", truncations=" + truncations +
                ", from=" + from +
                ", to=" + to +
                '}';
    }
}
