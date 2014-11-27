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
import com.milaboratory.migec2.core.align.reference.Reference;

public class Variant {
    private final int pos;
    private final byte to;
    private final double[] fromWeights;

    private final Reference reference;

    private double bgMinorReadFreq, bgMinorMigFreq;

    private final int minorMigCount, majorMigCount, sumAtPosMig;
    private final long sumAtPosRead, minorReadCount, majorReadCount;

    public Variant(Reference reference, int pos, byte to,
                   double[] fromWeights,
                   double bgMinorMigFreq, double bgMinorReadFreq,
                   int sumAtPosMig, long sumAtPosRead,
                   int minorMigCount, int majorMigCount,
                   long minorReadCount, long majorReadCount) {
        this.reference = reference;
        this.pos = pos;
        this.to = to;
        this.fromWeights = fromWeights;
        this.bgMinorMigFreq = bgMinorMigFreq;
        this.bgMinorReadFreq = bgMinorReadFreq;
        this.sumAtPosMig = sumAtPosMig;
        this.sumAtPosRead = sumAtPosRead;
        this.minorMigCount = minorMigCount;
        this.majorMigCount = majorMigCount;
        this.minorReadCount = minorReadCount;
        this.majorReadCount = majorReadCount;
    }

    public static final String HEADER = "referenceName\treferenceType\treferenceRC\treferenceLen\t" +
            "pos\tto\t" +
            "from" + NucleotideAlphabet.INSTANCE.symbolFromCode((byte) 0) + "\t" +
            "from" + NucleotideAlphabet.INSTANCE.symbolFromCode((byte) 1) + "\t" +
            "from" + NucleotideAlphabet.INSTANCE.symbolFromCode((byte) 2) + "\t" +
            "from" + NucleotideAlphabet.INSTANCE.symbolFromCode((byte) 3) + "\t" +
            "bgMinorMigFreq\tbgMinorReadFreq\t" +
            "sumAtPosMig\tsumAtPosRead\t" +
            "minorMigCount\tmajorMigCount\t" +
            "minorReadCount\tmajorReadCount";

    public Reference getReference() {
        return reference;
    }

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
        return majorMigCount / (double) sumAtPosMig;
    }

    public void setBgMinorReadFreq(double bgMinorReadFreq) {
        this.bgMinorReadFreq = bgMinorReadFreq;
    }

    public void setBgMinorMigFreq(double bgMinorMigFreq) {
        this.bgMinorMigFreq = bgMinorMigFreq;
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
        return reference.getName() + "\t" + reference.getType() + "\t" +
                reference.isReverseComplement() + "\t" + reference.getSequence().size() + "\t" +
                pos + "\t" + NucleotideAlphabet.INSTANCE.symbolFromCode(to) + "\t" +
                fromWeights[0] + "\t" + fromWeights[1] + "\t" + fromWeights[2] + "\t" + fromWeights[3] + "\t" +
                bgMinorMigFreq + "\t" + bgMinorReadFreq + "\t" +
                sumAtPosMig + "\t" + sumAtPosRead + "\t" +
                minorMigCount + "\t" + majorMigCount + "\t" +
                minorReadCount + "\t" + majorReadCount;
    }
}
