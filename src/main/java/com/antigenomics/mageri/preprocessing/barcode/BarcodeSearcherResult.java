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
package com.antigenomics.mageri.preprocessing.barcode;


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
