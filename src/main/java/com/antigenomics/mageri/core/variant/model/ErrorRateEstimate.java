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

package com.antigenomics.mageri.core.variant.model;

import java.io.Serializable;
import java.util.Arrays;

public class ErrorRateEstimate implements Serializable {
    public static ErrorRateEstimate createDummy(int statisticCount) {
        double[] statistics = new double[statisticCount];

        Arrays.fill(statistics, Double.NaN);

        return new ErrorRateEstimate(0.0, statistics);
    }

    private final double[] statistics;
    private final double errorRate;

    public ErrorRateEstimate(double errorRate, double... statistics) {
        this.errorRate = errorRate;
        this.statistics = statistics;
    }

    public double[] getStatistics() {
        return statistics;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public String getErrorRateEstimateRowPart() {
        String res = "";

        if (statistics.length > 0) {
            for (double statistic : statistics) {
                res += "\t" + (float) statistic;
            }
        }

        return res;
    }
}
