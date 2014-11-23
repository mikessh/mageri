/*
 * Copyright 2013-2014 Mikhail Shugay (mikhail.shugay@gmail.com)
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
 * Last modified on 20.11.2014 by mikesh
 */

package com.milaboratory.migec2.model.variant;

import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;

public class Variant {
    private final int pos;
    private final byte to;
    private final double[] fromWeights;

    private final VariantLibrary parent;

    private double bgMinorReadFreq = -1, bgMinorMigFreq = -1;

    private final int minorMigCount, majorMigCount, sumAtPosMig;
    private final long sumAtPosRead, minorReadCount, majorReadCount;

    Variant(VariantLibrary parent, int pos, byte to,
            double[] fromWeights,
            int sumAtPosMig, long sumAtPosRead,
            int minorMigCount, int majorMigCount,
            long minorReadCount, long majorReadCount) {
        this.parent = parent;
        this.pos = pos;
        this.to = to;
        this.fromWeights = fromWeights;
        this.sumAtPosMig = sumAtPosMig;
        this.sumAtPosRead = sumAtPosRead;
        this.minorMigCount = minorMigCount;
        this.majorMigCount = majorMigCount;
        this.minorReadCount = minorReadCount;
        this.majorReadCount = majorReadCount;
    }

    public static final String HEADER = "Pos\tNt\t" +
            NucleotideAlphabet.INSTANCE.symbolFromCode((byte) 0) + "\t" +
            NucleotideAlphabet.INSTANCE.symbolFromCode((byte) 1) + "\t" +
            NucleotideAlphabet.INSTANCE.symbolFromCode((byte) 2) + "\t" +
            NucleotideAlphabet.INSTANCE.symbolFromCode((byte) 3) + "\t" +
            "BgMinorMigFreq\tBgMinorReadFreq\t" +
            "SumAtPosMig\tSumAtPosRead\t" +
            "MajorMigCount\tMinorMigCount\t" +
            "MajorReadCount\tMinorReadCount";

    public double getFromWeight(byte from) {
        return fromWeights[from];
    }

    public int getPos() {
        return pos;
    }

    public byte getTo() {
        return to;
    }

    public double getFreq() {
        return majorMigCount / (double)sumAtPosMig;
    }

    public double getBgMinorReadFreq() {
        if (bgMinorReadFreq < 0) {
            bgMinorReadFreq = 0;
            for (byte from = 0; from < 4; from++) {
                bgMinorReadFreq += fromWeights[from] * parent.getBgFreqRead(from, to);
            }
        }
        return bgMinorReadFreq;
    }

    public double getBgMinorMigFreq() {
        if (bgMinorMigFreq < 0) {
            bgMinorMigFreq = 0;
            for (byte from = 0; from < 4; from++) {
                bgMinorMigFreq += fromWeights[from] * parent.getBgFreqMig(from, to);
            }
        }
        return bgMinorMigFreq;
    }

    public int getMinorMigCount() {
        return minorMigCount;
    }

    public int getMajorMigCount() {
        return majorMigCount;
    }

    public long getMinorReadCount() {
        return minorReadCount;
    }

    public long getMajorReadCount() {
        return majorReadCount;
    }

    public int getSumAtPosMig() {
        return sumAtPosMig;
    }

    public long getSumAtPosRead() {
        return sumAtPosRead;
    }

    @Override
    public String toString() {
        return pos + "\t" + NucleotideAlphabet.INSTANCE.symbolFromCode(to) + "\t" +
                fromWeights[0] + "\t" + fromWeights[1] + "\t" + fromWeights[2] + "\t" + fromWeights[3] + "\t" +
                sumAtPosMig + "\t" + sumAtPosRead + "\t" +
                bgMinorMigFreq + "\t" + bgMinorReadFreq + "\t" +
                majorMigCount + "\t" + minorMigCount + "\t" +
                majorReadCount + "\t" + minorReadCount;
    }
}
