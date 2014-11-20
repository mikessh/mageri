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

import com.milaboratory.migec2.core.consalign.mutations.MutationsAndCoverage;

import java.util.Collection;
import java.util.LinkedList;

public class VariantCollector {
    private final double[][] innerMatrixMig = new double[4][4], innerMatrixRead = new double[4][4];
    private final double[] rowSumsMig = new double[4], rowSumsRead = new double[4];
    private final double minorVariantThreshold;

    public VariantCollector(double minorVariantThreshold) {
        this.minorVariantThreshold = minorVariantThreshold;
    }

    public Collection<Variant> collect(MutationsAndCoverage mutationsAndCoverage) {
        final LinkedList<Variant> variants = new LinkedList<>();

        final int[] masterCount = new int[4];
        for (int i = 0; i < mutationsAndCoverage.referenceLength(); i++) {
            int sumAtPosMig = 0, sumAtPosRead = 0,
                    majorMigCount, minorMigCount, majorReadCount, minorReadCount;

            for (byte from = 0; from < 4; from++) {
                majorMigCount = mutationsAndCoverage.getMajorNucleotideMigCount(i, from);
                majorReadCount = mutationsAndCoverage.getMajorNucleotideReadCount(i, from);

                sumAtPosMig += majorMigCount;
                rowSumsMig[from] += majorMigCount;

                sumAtPosRead += majorReadCount;
                rowSumsRead[from] += majorReadCount;

                masterCount[from] = majorMigCount;
            }

            if (sumAtPosMig > 0) {
                for (byte from = 0; from < 4; from++) {
                    majorMigCount = masterCount[from];
                    majorReadCount = mutationsAndCoverage.getMajorNucleotideReadCount(i, from);

                    for (byte to = 0; to < 4; to++) {
                        minorMigCount = mutationsAndCoverage.getMinorNucleotideMigCount(i, to);
                        minorReadCount = mutationsAndCoverage.getMinorNucleotideReadCount(i, to);

                        if (from != to) {
                            innerMatrixMig[from][to] += minorMigCount * majorMigCount / (double) sumAtPosMig;
                            innerMatrixRead[from][to] += minorReadCount * majorReadCount / (double) sumAtPosRead;
                        } else if (majorMigCount > 0 &&
                                majorMigCount / (double) sumAtPosMig <= minorVariantThreshold) {
                            // report a variant
                            // NOTE: we use MIG-based criteria solely here

                            final double[] parentProb = new double[4];

                            // weighted probability of parent
                            for (byte parent = 0; parent < 4; parent++)
                                if (parent != from)
                                    parentProb[parent] = masterCount[parent] / (double) (sumAtPosMig - masterCount[from]);

                            variants.add(new Variant(i, from, parentProb,
                                    minorMigCount, majorMigCount,
                                    minorReadCount, majorReadCount));
                        }
                    }
                }
            }
        }

        for (Variant variant : variants) {
            for (byte from = 0; from < 4; from++) {
                // weight by parent probability
                // NOTE: we use MIG-based measure of probability solely here
                double weight = variant.getParentProb(from);
                variant.incrementBgMinorMigFreq(weight * getAtMig(from, variant.getTo()));
                variant.incrementBgMinorReadFreq(weight * getAtRead(from, variant.getTo()));
            }
        }

        return variants;
    }

    public double getAtMig(byte from, byte to) {
        return from == to ? 0d : (innerMatrixMig[from][to] / rowSumsMig[from]);
    }

    public double getAtRead(byte from, byte to) {
        return from == to ? 0d : (innerMatrixRead[from][to] / rowSumsRead[from]);
    }
}
