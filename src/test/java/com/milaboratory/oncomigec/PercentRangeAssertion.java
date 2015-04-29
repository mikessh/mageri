package com.milaboratory.oncomigec;

import org.junit.Assert;

public class PercentRangeAssertion {
    private final String paramName, condition;
    private final int from, to;
    private final boolean lowerBound, upperBound;

    public static PercentRangeAssertion createLowerBound(String paramName, String condition, int from) {
        return new PercentRangeAssertion(paramName, condition, from, 100, true, false);
    }

    public static PercentRangeAssertion createUpperBound(String paramName, String condition, int to) {
        return new PercentRangeAssertion(paramName, condition, 0, to, false, true);
    }

    public static PercentRangeAssertion createRange(String paramName, String condition, int from, int to) {
        return new PercentRangeAssertion(paramName, condition, from, to, false, false);
    }

    public static PercentRangeAssertion createDummy(String paramName, String condition) {
        return new PercentRangeAssertion(paramName, condition, 0, 100, true, true);
    }

    private PercentRangeAssertion(String paramName, String condition, int from, int to, boolean lowerBound, boolean upperBound) {
        if (from > to || from < 0 || to > 100)
            throw new IllegalArgumentException("From < to or not between 0 and 100%");
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

    private void assertInRange(int percent) {
        System.out.println(condition + " : " + paramName + " = " + percent + "%");
        Assert.assertTrue(paramName + " is in range " + toString() + " for " + condition, contains(percent));
    }

    public void assertInRange(int n, int N) {
        assertInRange(percent(n, N));
    }

    public void assertInRange(long n, long N) {
        assertInRange(percent(n, N));
    }

    public void assertInRange(double p) {
        assertInRange(percent(p));
    }

    public static int percent(int n, int N) {
        return (int) (100.0 * (n / (double) N));
    }

    public static int percent(long n, long N) {
        return (int) (100.0 * (n / (double) N));
    }

    public static int percent(double p) {
        return (int) (100.0 * p);
    }

    @Override
    public String toString() {
        if (lowerBound)
            return "[" + from + ", 100]%";
        else if (upperBound)
            return "[0, " + to + "]%";
        else
            return "[" + from + ", " + to + "]%";
    }
}
