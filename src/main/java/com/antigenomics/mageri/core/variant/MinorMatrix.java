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

package com.antigenomics.mageri.core.variant;

import com.antigenomics.mageri.core.mapping.MutationsTable;

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
