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

package com.antigenomics.mageri;

import org.junit.Assert;

public class IntRangeAssertion {
    private final String paramName, condition;
    private final int from, to;
    private final boolean lowerBound, upperBound;

    public static IntRangeAssertion createLowerBound(String paramName, String condition, int from) {
        return new IntRangeAssertion(paramName, condition, from, Integer.MAX_VALUE, true, false);
    }

    public static IntRangeAssertion createUpperBound(String paramName, String condition, int to) {
        return new IntRangeAssertion(paramName, condition, Integer.MIN_VALUE, to, false, true);
    }

    public static IntRangeAssertion createRange(String paramName, String condition, int from, int to) {
        return new IntRangeAssertion(paramName, condition, from, to, false, false);
    }

    private IntRangeAssertion(String paramName, String condition, int from, int to, boolean lowerBound, boolean upperBound) {
        if (from > to)
            throw new IllegalArgumentException("From > to in range");
        this.paramName = paramName;
        this.condition = condition;
        this.from = from;
        this.to = to;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    private boolean contains(int value) {
        return (upperBound || value >= from) && (lowerBound || value <= to);
    }

    public void assertInRange(int value) {
        System.out.println(condition + " : " + paramName + " = " + value);
        Assert.assertTrue(paramName + " is in range " + toString() + " for " + condition, contains(value));
    }

    @Override
    public String toString() {
        if (lowerBound)
            return "[" + from + ", +inf)";
        else if (upperBound)
            return "(-inf, " + to + "]";
        else
            return "[" + from + ", " + to + "]";
    }
}
