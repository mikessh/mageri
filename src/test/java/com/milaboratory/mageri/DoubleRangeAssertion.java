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

package com.milaboratory.mageri;

import org.junit.Assert;

public class DoubleRangeAssertion {
    private final String paramName, condition;
    private final double from, to;
    private final boolean lowerBound, upperBound;

    public static DoubleRangeAssertion createLowerBound(String paramName, String condition, double from) {
        return new DoubleRangeAssertion(paramName, condition, from, Double.MAX_VALUE, true, false);
    }

    public static DoubleRangeAssertion createUpperBound(String paramName, String condition, double to) {
        return new DoubleRangeAssertion(paramName, condition, Double.MIN_VALUE, to, false, true);
    }

    public static DoubleRangeAssertion createRange(String paramName, String condition, double from, double to) {
        return new DoubleRangeAssertion(paramName, condition, from, to, false, false);
    }

    public static DoubleRangeAssertion createDummy(String paramName, String condition) {
        return new DoubleRangeAssertion(paramName, condition, Double.MIN_VALUE, Double.MAX_VALUE, true, true);
    }

    private DoubleRangeAssertion(String paramName, String condition, double from, double to, boolean lowerBound, boolean upperBound) {
        if (from > to)
            throw new IllegalArgumentException("From > to in range");
        this.paramName = paramName;
        this.condition = condition;
        this.from = from;
        this.to = to;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    private boolean contains(double value) {
        return (upperBound || value >= from) && (lowerBound || value <= to);
    }

    public void assertInRange(double value) {
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
