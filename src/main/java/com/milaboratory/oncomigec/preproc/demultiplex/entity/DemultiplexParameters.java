package com.milaboratory.oncomigec.preproc.demultiplex.entity;

import com.milaboratory.oncomigec.util.ParameterSet;
import com.milaboratory.oncomigec.util.Util;
import org.jdom.Element;

public class DemultiplexParameters implements ParameterSet{
    private final boolean orientedReads;
    private final int maxTruncations;
    private final double maxGoodQualMMRatio, maxLowQualityMMRatio;
    private final byte lowQualityThreshold;

    public static DemultiplexParameters DEFAULT = new DemultiplexParameters(false,
            2, 0.05, 0.1,
            Util.PH33_LOW_QUAL);

    public DemultiplexParameters(boolean orientedReads,
                                 int maxTruncations, double maxGoodQualMMRatio, double maxLowQualityMMRatio,
                                 byte lowQualityThreshold) {
        this.orientedReads = orientedReads;
        this.maxTruncations = maxTruncations;
        this.maxGoodQualMMRatio = maxGoodQualMMRatio;
        this.maxLowQualityMMRatio = maxLowQualityMMRatio;
        this.lowQualityThreshold = lowQualityThreshold;
    }

    public boolean orientedReads() {
        return orientedReads;
    }

    public int getMaxTruncations() {
        return maxTruncations;
    }

    public double getMaxGoodQualMMRatio() {
        return maxGoodQualMMRatio;
    }

    public double getMaxLowQualityMMRatio() {
        return maxLowQualityMMRatio;
    }

    public byte getLowQualityThreshold() {
        return lowQualityThreshold;
    }

    @Override
    public Element toXml() {
        Element e = new Element("DemultiplexParameters");
        e.addContent(new Element("orientedReads").setText(Boolean.toString(orientedReads)));
        e.addContent(new Element("maxTruncations").setText(Integer.toString(maxTruncations)));
        e.addContent(new Element("maxGoodQualMMRatio").setText(Double.toString(maxGoodQualMMRatio)));
        e.addContent(new Element("maxLowQualityMMRatio").setText(Double.toString(maxLowQualityMMRatio)));
        e.addContent(new Element("lowQualityThreshold").setText(Byte.toString(lowQualityThreshold)));
        return e;
    }

    public static DemultiplexParameters fromXml(Element parent) {
        Element e = parent.getChild("DemultiplexParameters");
        return new DemultiplexParameters(
                Boolean.parseBoolean(e.getChildTextTrim("orientedReads")),
                Integer.parseInt(e.getChildTextTrim("maxTruncations")),
                Double.parseDouble(e.getChildTextTrim("maxGoodQualMMRatio")),
                Double.parseDouble(e.getChildTextTrim("maxLowQualityMMRatio")),
                Byte.parseByte(e.getChildTextTrim("lowQualityThreshold"))
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DemultiplexParameters that = (DemultiplexParameters) o;

        if (lowQualityThreshold != that.lowQualityThreshold) return false;
        if (Double.compare(that.maxGoodQualMMRatio, maxGoodQualMMRatio) != 0) return false;
        if (Double.compare(that.maxLowQualityMMRatio, maxLowQualityMMRatio) != 0) return false;
        if (maxTruncations != that.maxTruncations) return false;
        if (orientedReads != that.orientedReads) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (orientedReads ? 1 : 0);
        result = 31 * result + maxTruncations;
        temp = Double.doubleToLongBits(maxGoodQualMMRatio);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxLowQualityMMRatio);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) lowQualityThreshold;
        return result;
    }
}
