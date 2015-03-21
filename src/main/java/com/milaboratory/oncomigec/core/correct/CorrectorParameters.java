package com.milaboratory.oncomigec.core.correct;

import com.milaboratory.oncomigec.util.ParameterSet;
import com.milaboratory.oncomigec.util.Util;
import org.jdom.Element;

public final class CorrectorParameters implements ParameterSet {
    private final double readGainThreshold, pValueThreshold;

    private final boolean filterSingletons;
    private final double singletonFilterRatio;

    private final int minMigCoverage;
    private final int minMigCount;
    private final byte minAvgQuality;

    private final double maxBasePairsMaskedRatio;

    public static CorrectorParameters DEFAULT = new CorrectorParameters(0.7, 0.05, false, 10000.0,
            1, 1, Util.PH33_BAD_QUAL, 0.7);

    public CorrectorParameters(double readGainThreshold, double pValueThreshold,
                               boolean filterSingletons, double singletonFilterRatio,
                               int minMigCoverage, int minMigCount,
                               byte minAvgQuality, double maxBasePairsMaskedRatio) {
        this.readGainThreshold = readGainThreshold;
        this.pValueThreshold = pValueThreshold;
        this.filterSingletons = filterSingletons;
        this.singletonFilterRatio = singletonFilterRatio;
        this.minMigCoverage = minMigCoverage;
        this.minMigCount = minMigCount;
        this.minAvgQuality = minAvgQuality;
        this.maxBasePairsMaskedRatio = maxBasePairsMaskedRatio;

        if (readGainThreshold < 0 ||
                readGainThreshold > 1)
            throw new IllegalArgumentException("Classifier probability threshold should be set in [0, 1] range");
        if (singletonFilterRatio < 1)
            throw new IllegalArgumentException("Singleton filter ratio should be greater than 1");
        if (maxBasePairsMaskedRatio < 0 ||
                maxBasePairsMaskedRatio > 1)
            throw new IllegalArgumentException("Max base pair masked ratio should be set in [0, 1] range");
    }

    public double getReadGainThreshold() {
        return readGainThreshold;
    }

    public double getpValueThreshold() {
        return pValueThreshold;
    }

    public boolean filterSingletons() {
        return filterSingletons;
    }

    public double getSingletonFilterRatio() {
        return singletonFilterRatio;
    }

    public int getMinMigCoverage() {
        return minMigCoverage;
    }

    public byte getMinAvgQuality() {
        return minAvgQuality;
    }

    public int getMinMigCount() {
        return minMigCount;
    }

    public double getMaxBasePairsMaskedRatio() {
        return maxBasePairsMaskedRatio;
    }

    @Override
    public Element toXml() {
        Element e = new Element("CorrectorParameters");
        e.addContent(new Element("readGainThreshold").setText(Double.toString(readGainThreshold)));
        e.addContent(new Element("pValueThreshold").setText(Double.toString(pValueThreshold)));
        e.addContent(new Element("filterSingletons").setText(Boolean.toString(filterSingletons)));
        e.addContent(new Element("singletonFilterRatio").setText(Double.toString(singletonFilterRatio)));
        e.addContent(new Element("minMigCoverage").setText(Integer.toString(minMigCoverage)));
        e.addContent(new Element("minMigCount").setText(Integer.toString(minMigCount)));
        e.addContent(new Element("minAvgQuality").setText(Byte.toString(minAvgQuality)));
        e.addContent(new Element("maxBasePairsMaskedRatio").setText(Double.toString(maxBasePairsMaskedRatio)));
        return e;
    }

    public static CorrectorParameters fromXml(Element parent) {
        Element e = parent.getChild("CorrectorParameters");
        return new CorrectorParameters(
                Double.parseDouble(e.getChildTextTrim("readGainThreshold")),
                Double.parseDouble(e.getChildTextTrim("pValueThreshold")),
                Boolean.parseBoolean(e.getChildTextTrim("filterSingletons")),
                Double.parseDouble(e.getChildTextTrim("singletonFilterRatio")),
                Integer.parseInt(e.getChildTextTrim("minMigCoverage")),
                Integer.parseInt(e.getChildTextTrim("minMigCount")),
                Byte.parseByte(e.getChildTextTrim("minAvgQuality")),
                Double.parseDouble(e.getChildTextTrim("maxBasePairsMaskedRatio"))
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CorrectorParameters that = (CorrectorParameters) o;

        if (filterSingletons != that.filterSingletons) return false;
        if (Double.compare(that.maxBasePairsMaskedRatio, maxBasePairsMaskedRatio) != 0) return false;
        if (minAvgQuality != that.minAvgQuality) return false;
        if (minMigCount != that.minMigCount) return false;
        if (minMigCoverage != that.minMigCoverage) return false;
        if (Double.compare(that.pValueThreshold, pValueThreshold) != 0) return false;
        if (Double.compare(that.readGainThreshold, readGainThreshold) != 0) return false;
        if (Double.compare(that.singletonFilterRatio, singletonFilterRatio) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(readGainThreshold);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(pValueThreshold);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (filterSingletons ? 1 : 0);
        temp = Double.doubleToLongBits(singletonFilterRatio);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + minMigCoverage;
        result = 31 * result + minMigCount;
        result = 31 * result + (int) minAvgQuality;
        temp = Double.doubleToLongBits(maxBasePairsMaskedRatio);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
