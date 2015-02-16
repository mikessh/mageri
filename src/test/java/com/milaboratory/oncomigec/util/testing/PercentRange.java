package com.milaboratory.oncomigec.util.testing;

import com.milaboratory.oncomigec.util.Basics;
import org.junit.Assert;

public class PercentRange {
    private final String paramName, condition;
    private final int from, to;
    private final boolean lowerBound, upperBound;

    public static PercentRange createLowerBound(String paramName, String condition, int from) {
        return new PercentRange(paramName, condition, from, 100, true, false);
    }

    public static PercentRange createUpperBound(String paramName, String condition, int to) {
        return new PercentRange(paramName, condition, 0, to, false, true);
    }

    public static PercentRange createRange(String paramName, String condition, int from, int to) {
        return new PercentRange(paramName, condition, from, to, false, false);
    }

    private PercentRange(String paramName, String condition, int from, int to, boolean lowerBound, boolean upperBound) {
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
        assertInRange(Basics.percent(n, N));
    }

    public void assertInRange(double p) {
        assertInRange(Basics.percent(p));
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
