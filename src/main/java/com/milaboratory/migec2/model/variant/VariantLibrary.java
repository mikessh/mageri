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
 * Last modified on 23.11.2014 by mikesh
 */

package com.milaboratory.migec2.model.variant;

import com.milaboratory.migec2.core.align.reference.Reference;
import com.milaboratory.migec2.core.consalign.mutations.MutationsAndCoverage;

import java.util.Collection;
import java.util.LinkedList;

/**
 * A dual-purpose class that serves as an updatable transition frequency matrix and
 * a minor variant scanner. While minor variants are collected, they are annotated with
 * corresponding background error frequencies, an information that could be further used
 * in error filtering process.
 */
public class VariantLibrary {
    private final double[][] innerMatrixMig = new double[4][4], innerMatrixRead = new double[4][4];
    private final double[] rowSumsMig = new double[4], rowSumsRead = new double[4];

    /**
     * Updates background substitution statistics using a given mutations and coverage matrix
     *
     * @param mutationsAndCoverage mutation and coverage matrix to summarize
     */
    public void update(MutationsAndCoverage mutationsAndCoverage) {
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
                            // don't forget to protect from overflow here
                            innerMatrixMig[from][to] += minorMigCount * (double) majorMigCount / (double) sumAtPosMig;
                            innerMatrixRead[from][to] += minorReadCount * (double) majorReadCount / (double) sumAtPosRead;
                        }
                    }
                }
            }
        }
    }

    /**
     * Collects minor variants (&lt; 5%) from a given mutations and coverage matrix
     *
     * @param mutationsAndCoverage mutation and coverage matrix to scan for minor variants
     * @return a collection of minor variants that were detected
     */
    public Collection<Variant> collectVariants(MutationsAndCoverage mutationsAndCoverage) {
        return collectVariants(mutationsAndCoverage, 0.05);
    }

    /**
     * Collects minor variants from a given mutations and coverage matrix
     *
     * @param mutationsAndCoverage  mutation and coverage matrix to scan for minor variants
     * @param minorVariantThreshold a threshold for minor variant frequency
     * @return a collection of minor variants that were detected
     */
    public Collection<Variant> collectVariants(MutationsAndCoverage mutationsAndCoverage,
                                               double minorVariantThreshold) {
        final LinkedList<Variant> variants = new LinkedList<>();
        Reference reference = mutationsAndCoverage.getReference();

        final int[] masterCount = new int[4];
        for (int i = 0; i < mutationsAndCoverage.referenceLength(); i++) {
            int sumAtPosMig = 0, majorMigCount, minorMigCount, majorReadCount, minorReadCount;

            for (byte from = 0; from < 4; from++) {
                majorMigCount = mutationsAndCoverage.getMajorNucleotideMigCount(i, from);
                sumAtPosMig += majorMigCount;
                masterCount[from] = majorMigCount;
            }

            if (sumAtPosMig > 0) {
                for (byte from = 0; from < 4; from++) {
                    majorMigCount = masterCount[from];
                    majorReadCount = mutationsAndCoverage.getMajorNucleotideReadCount(i, from);

                    for (byte to = 0; to < 4; to++) {
                        minorMigCount = mutationsAndCoverage.getMinorNucleotideMigCount(i, to);
                        minorReadCount = mutationsAndCoverage.getMinorNucleotideReadCount(i, to);

                        // NOTE: we use solely MIG-based criteria to calculate variant freq here
                        if (majorMigCount > 0 &&
                                majorMigCount / (double) sumAtPosMig <= minorVariantThreshold) {
                            // report a variant

                            final double[] parentProb = new double[4];

                            // background error rates, to be properly normalized
                            double bgMinorMigFreq = 0, bgMinorReadFreq = 0;

                            // weighted probability of parent
                            for (byte parent = 0; parent < 4; parent++) {
                                if (parent != to) {
                                    // NOTE: we use MIG-based measure of probability solely here
                                    double w = masterCount[parent] / (double) (sumAtPosMig - masterCount[to]);
                                    parentProb[parent] = w;
                                    bgMinorMigFreq += w * getBgFreqMig(from, to);
                                    bgMinorReadFreq += w * getBgFreqRead(from, to);
                                }
                            }

                            variants.add(new Variant(reference, i, from,
                                    parentProb,
                                    bgMinorMigFreq,
                                    bgMinorReadFreq,
                                    minorMigCount, majorMigCount,
                                    minorReadCount, majorReadCount));
                        }
                    }
                }
            }
        }

        return variants;
    }

    /**
     * Gets background substitution frequency, computed in MIG units. Note: the probability matrix is non-symmetrical
     *
     * @param from reference nucleotide
     * @param to   variant nucleotide
     * @return probability of substitution
     */
    public double getBgFreqMig(byte from, byte to) {
        return from == to ? 0d : (innerMatrixMig[from][to] / rowSumsMig[from]);
    }

    /**
     * Gets background substitution frequency, computed in READ units. Note: the probability matrix is non-symmetrical
     *
     * @param from reference nucleotide code
     * @param to   variant nucleotide code
     * @return probability of substitution
     */
    public double getBgFreqRead(byte from, byte to) {
        return from == to ? 0d : (innerMatrixRead[from][to] / rowSumsRead[from]);
    }
}
