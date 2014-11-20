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

package com.milaboratory.migec2.model.substitution;

import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;

public class Variant {
    private final int pos;
    private final byte to;
    private final double[] parentProb;

    private double bgMinorReadFreq = 0, bgMinorMigFreq = 0;

    private final int minorMigCount, majorMigCount, minorReadCount, majorReadCount;

    Variant(int pos, byte to,
            double[] parentProb,
            int minorMigCount, int majorMigCount,
            int minorReadCount, int majorReadCount) {
        this.pos = pos;
        this.to = to;
        this.parentProb = parentProb;
        this.minorMigCount = minorMigCount;
        this.majorMigCount = majorMigCount;
        this.minorReadCount = minorReadCount;
        this.majorReadCount = majorReadCount;
    }

    public static final String HEADER = "Pos\tNt\t" +
            NucleotideAlphabet.INSTANCE.symbolFromCode((byte)0) + "\t" +
            NucleotideAlphabet.INSTANCE.symbolFromCode((byte)1) + "\t" +
            NucleotideAlphabet.INSTANCE.symbolFromCode((byte)2) + "\t" +
            NucleotideAlphabet.INSTANCE.symbolFromCode((byte)3) + "\t" +
            "BgMinorMigFreq\tBgMinorReadFreq\t" +
            "MajorMigCount\tMinorMigCount\t" +
            "MajorReadCount\tMinorReadCount";

    void incrementBgMinorReadFreq(double bgMinorReadFreq) {
        this.bgMinorReadFreq += bgMinorReadFreq;
    }

    void incrementBgMinorMigFreq(double bgMinorMigFreq) {
        this.bgMinorMigFreq += bgMinorMigFreq;
    }

    public double getParentProb(byte from) {
        return from != to ? parentProb[from] : 0.0;
    }

    public int getPos() {
        return pos;
    }

    public byte getTo() {
        return to;
    }

    public double getBgMinorReadFreq() {
        return bgMinorReadFreq;
    }

    public double getBgMinorMigFreq() {
        return bgMinorMigFreq;
    }

    public int getMinorMigCount() {
        return minorMigCount;
    }

    public int getMajorMigCount() {
        return majorMigCount;
    }

    public int getMinorReadCount() {
        return minorReadCount;
    }

    public int getMajorReadCount() {
        return majorReadCount;
    }

    @Override
    public String toString() {
        return pos + "\t" + NucleotideAlphabet.INSTANCE.symbolFromCode(to) + "\t" +
                parentProb[0] + "\t" + parentProb[1] + "\t" + parentProb[2] + "\t" + parentProb[3] + "\t" +
                bgMinorMigFreq + "\t" + bgMinorReadFreq + "\t" +
                majorMigCount + "\t" + minorMigCount + "\t" +
                majorReadCount + "\t" + minorReadCount;
    }
}
