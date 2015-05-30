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

package com.milaboratory.oncomigec.core.variant;

import com.milaboratory.oncomigec.core.mapping.MutationsTable;

public class MinorMatrix {
    private final double[][] innerMatrix;

    public static final MinorMatrix DEFAULT = new MinorMatrix(new double[][]{
            //  A        G        C        T
            {0.00000, 0.08551, 0.00427, 0.00005},
            {0.05339, 0.00000, 0.00005, 0.01960},
            {0.00635, 0.07048, 0.00000, 0.06102},
            {0.00005, 0.00005, 0.07926, 0.00000}
    });

    public static MinorMatrix fromMutationsTable(MutationsTable mutationsTable) {
        double[][] innerMatrix = new double[4][4];
        double[] fromCounters = new double[4];

        for (int pos = 0; pos < mutationsTable.length(); pos++) {
            int total = mutationsTable.getMigCoverage(pos);

            for (int from = 0; from < 4; from++) {
                int count = mutationsTable.getMajorMigCount(pos, from);
                if (count > 0) {
                    double factor = count / (double) total;
                    fromCounters[from] += count;
                    for (int to = 0; to < 4; to++) {
                        if (from != to) {
                            innerMatrix[from][to] += mutationsTable.getMinorMigCount(pos, to) * factor;
                        }
                    }
                }
            }
        }

        for (int from = 0; from < 4; from++) {
            for (int to = 0; to < 4; to++) {
                innerMatrix[from][to] /= fromCounters[from];
            }
        }

        return new MinorMatrix(innerMatrix);
    }

    private MinorMatrix(double[][] innerMatrix) {
        this.innerMatrix = innerMatrix;
    }

    public double getRate(int from, int to) {
        return getRate(from, to, false);
    }

    public double getRate(int from, int to, boolean symmetric) {
        return symmetric ? 0.5 * (innerMatrix[from][to] + innerMatrix[to][from]) : innerMatrix[from][to];
    }
}
