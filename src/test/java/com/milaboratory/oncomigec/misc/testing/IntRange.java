package com.milaboratory.oncomigec.misc.testing;

import org.junit.Assert;

public class IntRange {
    private final String paramName, condition;
    private final int from, to;
    private final boolean lowerBound, upperBound;

    public static IntRange createLowerBound(String paramName, String condition, int from) {
        return new IntRange(paramName, condition, from, Integer.MAX_VALUE, true, false);
    }

    public static IntRange createUpperBound(String paramName, String condition, int to) {
        return new IntRange(paramName, condition, Integer.MIN_VALUE, to, false, true);
    }

    public static IntRange createRange(String paramName, String condition, int from, int to) {
        return new IntRange(paramName, condition, from, to, false, false);
    }

    private IntRange(String paramName, String condition, int from, int to, boolean lowerBound, boolean upperBound) {
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
