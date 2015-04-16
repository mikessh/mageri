package com.milaboratory.oncomigec.misc.testing;

import org.junit.Assert;

public class DoubleRange {
    private final String paramName, condition;
    private final double from, to;
    private final boolean lowerBound, upperBound;

    public static DoubleRange createLowerBound(String paramName, String condition, double from) {
        return new DoubleRange(paramName, condition, from, Double.MAX_VALUE, true, false);
    }

    public static DoubleRange createUpperBound(String paramName, String condition, double to) {
        return new DoubleRange(paramName, condition, Double.MIN_VALUE, to, false, true);
    }

    public static DoubleRange createRange(String paramName, String condition, double from, double to) {
        return new DoubleRange(paramName, condition, from, to, false, false);
    }

    private DoubleRange(String paramName, String condition, double from, double to, boolean lowerBound, boolean upperBound) {
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
