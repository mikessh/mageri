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

import com.antigenomics.mageri.misc.AuxiliaryStats;
import org.junit.Test;

public class ErrorModelStatTest {
    @Test
    public void betaBinomTest() {
        //System.out.println(calc(1447, 1447, 2.1314828, 24344.82));
        System.out.println(calc(13, 2374, 2.1314828, 24344.82));
        System.out.println(calc(500, 2374, 2.1314828, 24344.82));
        //System.out.println(calc(1590, 2374, 2.1314828, 24344.82));
    }

    private static double calc(int n, int N, double a, double b) {
        //return -10 * Math.log10(1.0 - AuxiliaryStats.betaBinomialCdf(n, N, a, b));
        return -10 * Math.log10(AuxiliaryStats.betaBinomialPvalueFast(n, N, a, b));
    }
}
