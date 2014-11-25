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

import com.milaboratory.migec2.core.consalign.mutations.MutationsAndCoverage;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A dual-purpose class that serves as an updatable transition frequency matrix and
 * a minor variant scanner. While minor variants are collected, they are annotated with
 * corresponding background error frequencies, an information that could be further used
 * in error filtering process.
 */
public class VariantLibrary {
    private final MutationsAndCoverage mutationsAndCoverage;
    private final List<Variant> variantList = new LinkedList<>();
    private final Variant[][] variants;
    private final double[][] innerMatrixMig = new double[4][4], innerMatrixRead = new double[4][4];
    private final double[] rowSumsMig = new double[4], rowSumsRead = new double[4];

    /**
     * Creates a new instance
     *
     * @param mutationsAndCoverage mutations and coverage matrix to process
     */
    public VariantLibrary(MutationsAndCoverage mutationsAndCoverage) {
        this.mutationsAndCoverage = mutationsAndCoverage;
        this.variants = new Variant[mutationsAndCoverage.referenceLength()][4];
        init();
    }

    /**
     * Computes background substitution statistics using a given mutations and coverage matrix.
     * Summarizes variants
     */
    private void init() {
        final int[] majorMigCountArr = new int[4];

        for (int i = 0; i < mutationsAndCoverage.referenceLength(); i++) {
            int sumAtPosMig = 0, majorMigCount, minorMigCount;
            long sumAtPosRead = 0, majorReadCount, minorReadCount;

            for (byte from = 0; from < 4; from++) {
                majorMigCount = mutationsAndCoverage.getMajorNucleotideMigCount(i, from);
                majorReadCount = mutationsAndCoverage.getMajorNucleotideReadCount(i, from);

                sumAtPosMig += majorMigCount;
                rowSumsMig[from] += majorMigCount;

                sumAtPosRead += majorReadCount;
                rowSumsRead[from] += majorReadCount;

                majorMigCountArr[from] = majorMigCount;
            }

            if (sumAtPosMig > 0) {
                for (byte from = 0; from < 4; from++) {
                    majorMigCount = majorMigCountArr[from];
                    majorReadCount = mutationsAndCoverage.getMajorNucleotideReadCount(i, from);

                    for (byte to = 0; to < 4; to++) {
                        minorMigCount = mutationsAndCoverage.getMinorNucleotideMigCount(i, to);
                        minorReadCount = mutationsAndCoverage.getMinorNucleotideReadCount(i, to);

                        if (from != to) {
                            // don't forget to protect from overflow here
                            innerMatrixMig[from][to] += minorMigCount * (double) majorMigCount / (double) sumAtPosMig;
                            innerMatrixRead[from][to] += minorReadCount * (double) majorReadCount / (double) sumAtPosRead;
                        } else if (majorMigCount > 0) {
                            // diagonal - store variants that are represented by at least 1 molecule
                            final double[] fromWeights = new double[4];

                            // first determine probability of where this variant originated from
                            for (byte from2 = 0; from2 < 4; from2++)
                                if (from2 != to)
                                    fromWeights[from2] = majorMigCountArr[from2] /
                                            (double) (sumAtPosMig - majorMigCountArr[to]);

                            // then store this variant. Bg frequencies are calculated in lazy fashion
                            Variant variant = new Variant(
                                    mutationsAndCoverage.getReference(),
                                    i, to, fromWeights,
                                    -1.0, -1.0,
                                    sumAtPosMig, sumAtPosRead, minorMigCount, majorMigCount,
                                    minorReadCount, majorReadCount);
                            variants[i][to] = variant;
                            variantList.add(variant);
                        }
                    }
                }
            }
        }

        for (Variant variant : variantList) {
            double bgMinorMigFreq = 0;
            for (byte from = 0; from < 4; from++) {
                bgMinorMigFreq += variant.getFromWeight(from) * getBgFreqMig(from, variant.getTo());
            }
            variant.setBgMinorMigFreq(bgMinorMigFreq);

            double bgMinorReadFreq = 0;
            for (byte from = 0; from < 4; from++) {
                bgMinorReadFreq += variant.getFromWeight(from) * getBgFreqRead(from, variant.getTo());
            }
            variant.setBgMinorReadFreq(bgMinorReadFreq);
        }
    }

    /**
     * Collects minor variants that have &lt; 5% frequency
     *
     * @return a collection of minor variants that were detected
     */
    public Collection<Variant> collectVariants() {
        return collectVariants(0.05);
    }

    /**
     * Collects minor variants from a given mutations and coverage matrix
     *
     * @return a collection of minor variants that were detected
     */
    public Collection<Variant> collectVariants(double minorVariantThreshold) {
        final LinkedList<Variant> variants = new LinkedList<>();

        for (int i = 0; i < mutationsAndCoverage.referenceLength(); i++) {
            for (byte to = 0; to < 4; to++) {
                Variant variant = this.variants[i][to];
                if (variant != null && variant.getFreq() <= minorVariantThreshold)
                    variants.add(variant);
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

    /**
     * Get mutations and coverage data used to build this variant library
     */
    public MutationsAndCoverage getMutationsAndCoverage() {
        return mutationsAndCoverage;
    }

    /**
     * Gets a variant data at specified position
     *
     * @param pos coordinate
     * @param nt  nucleotide
     * @return variant data or null if no variant exists
     */
    public Variant getAt(int pos, byte nt) {
        return variants[pos][nt];
    }
}
