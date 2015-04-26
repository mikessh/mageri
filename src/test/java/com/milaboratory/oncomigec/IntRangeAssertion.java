package com.milaboratory.oncomigec;

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
