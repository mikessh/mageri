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

package com.antigenomics.mageri.misc;

import org.apache.commons.math3.special.Beta;
import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.util.FastMath;

import static org.apache.commons.math3.special.Beta.regularizedBeta;
import static org.apache.commons.math3.util.CombinatoricsUtils.binomialCoefficientLog;

public class AuxiliaryStats {
    private static final double SQRT2 = FastMath.sqrt(2.0);

    public static double negativeBinomialCdf(int k, double r, double p) {
        return 1.0 - regularizedBeta(p, k + 1, r);
    }

    public static double negativeBinomialPdf(int k, int r, double p) {
        return Math.exp(binomialCoefficientLog(k + r - 1, k) +
                r * Math.log(1.0 - p) + k * Math.log(p));
    }

    public static double betaBinomialPdf(int k, int n, double alpha, double beta) {
        return Math.exp(
                Beta.logBeta(k + alpha, n - k + beta) - Beta.logBeta(alpha, beta) +
                Gamma.logGamma(n + 1) - Gamma.logGamma(k + 1) - Gamma.logGamma(n - k + 1)
        );
    }

    public static double betaBinomialCdf(int k, int n, double alpha, double beta) {
        double sum = 0;

        for (int i = 0; i < k; i++) {
            sum += betaBinomialPdf(i, n, alpha, beta);
        }

        return Math.min(1.0, sum);
    }

    public static double normalCdf(double x, double mean, double sd) {
        final double dev = x - mean;
        if (FastMath.abs(dev) > 40 * sd) {
            return dev < 0 ? 0.0d : 1.0d;
        }
        return 0.5 * Erf.erfc(-dev / (sd * SQRT2));
    }
}
