package com.milaboratory.migec2.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class ProbabilityHistogram {
    private final int nBinsLow, nBinsHigh;
    private final double lowHighBoundary;
    private final double logBase, logDenom;
    private final AtomicIntegerArray[] lowHistograms, highHistograms;
    private final AtomicIntegerArray totalCounters;
    private final String[] valueNames;
    private final Map<String, Integer> variableName2Id = new HashMap<>();

    public ProbabilityHistogram(String... valueNames) {
        this(10, 16, 0.25, 2, valueNames);
    }

    public ProbabilityHistogram(int nBinsLow, int nBinsHigh,
                                double lowHighBoundary, double logBase,
                                String... valueNames) {
        this.nBinsLow = nBinsLow;
        this.nBinsHigh = nBinsHigh;
        this.lowHighBoundary = lowHighBoundary;
        this.logBase = logBase;

        this.logDenom = Math.log(logBase);
        this.lowHistograms = new AtomicIntegerArray[valueNames.length];
        this.highHistograms = new AtomicIntegerArray[valueNames.length];
        for (int j = 0; j < valueNames.length; j++) {
            lowHistograms[j] = new AtomicIntegerArray(nBinsLow);
            highHistograms[j] = new AtomicIntegerArray(nBinsHigh);
        }
        this.totalCounters = new AtomicIntegerArray(valueNames.length);
        this.valueNames = valueNames;
        for (int i = 0; i < valueNames.length; i++)
            variableName2Id.put(valueNames[i], i);
    }

    private void appendLow(int variableId, double value, int weight) {
        int bin = value == 0 ? 0 : nBinsLow - 1 + (int) (Math.log(value / lowHighBoundary) / logDenom);
        if (bin < 0)
            bin = 0;
        lowHistograms[variableId].addAndGet(bin, weight);
    }

    private void appendHigh(int variableId, double value, int weight) {
        int bin = (int) Math.round((nBinsHigh - 1) * (value - lowHighBoundary) / (1.0 - lowHighBoundary));
        if (bin > nBinsHigh - 1)
            bin = nBinsHigh - 1;
        highHistograms[variableId].addAndGet(bin, weight);
    }

    public void append(String variableName, double value) {
        append(variableName, value, 1);
    }

    public void append(String variableName, double value, int weight) {
        append(variableName2Id.get(variableName), value, weight);
    }

    public void append(int variableId, double value) {
        append(variableId, value, 1);
    }

    public void append(int variableId, double value, int weight) {
        if (value < 0 || value > 1)
            throw new IllegalArgumentException("Value should be in range [0, 1]");
        if (value < lowHighBoundary)
            appendLow(variableId, value, weight);
        else
            appendHigh(variableId, value, weight);
        totalCounters.addAndGet(variableId, weight);
    }

    public void append(int variableId) {
        totalCounters.addAndGet(variableId, 1);
    }

    public void append(String variableName) {
        append(variableName, 1);
    }

    public void append(int variableId, int weight) {
        totalCounters.addAndGet(variableId, weight);
    }

    public void append(String variableName, int weight) {
        totalCounters.addAndGet(variableName2Id.get(variableName), weight);
    }

    public void reset() {
        for (int i = 0; i < valueNames.length; i++) {
            totalCounters.set(i, 0);
            for (int j = 0; j < nBinsLow; j++)
                lowHistograms[i].set(j, 0);
            for (int j = 0; j < nBinsHigh; j++)
                highHistograms[i].set(j, 0);
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Variables");
        for (int i = 0; i < nBinsLow - 1; i++) {
            double value = lowHighBoundary * Math.pow(logBase, i - nBinsLow + 1);
            stringBuilder.append('\t').append(value);
        }
        for (int i = 0; i < nBinsHigh; i++) {
            double value = lowHighBoundary + (1.0 - lowHighBoundary) * i / (double) (nBinsHigh - 1);
            stringBuilder.append('\t').append(value);
        }
        stringBuilder.append("\tTotal");
        for (int j = 0; j < valueNames.length; j++) {
            stringBuilder.append('\n').append(valueNames[j]);
            for (int i = 0; i < nBinsLow - 1; i++) {
                stringBuilder.append('\t').append(lowHistograms[j].get(i));
            }
            for (int i = 0; i < nBinsHigh; i++) {
                stringBuilder.append('\t').append(highHistograms[j].get(i));
            }
            stringBuilder.append('\t').append(totalCounters.get(j));
        }
        return stringBuilder.toString();
    }
}
