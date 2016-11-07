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

import static org.apache.commons.math3.special.Beta.regularizedBeta;
import static org.apache.commons.math3.util.CombinatoricsUtils.binomialCoefficientLog;

public class AuxiliaryStats {
    public static double negativeBinomialCdf(int k, int r, double p) {
        return 1.0 - regularizedBeta(p, k + 1, r);
    }

    public static double negativeBinomialPdf(int k, int r, double p) {
        return Math.exp(binomialCoefficientLog(k + r - 1, k) +
                r * Math.log(1.0 - p) + k * Math.log(p));
    }
}
