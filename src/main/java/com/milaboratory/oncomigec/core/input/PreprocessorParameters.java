package com.milaboratory.oncomigec.core.input;

import com.milaboratory.oncomigec.misc.ParameterSet;
import com.milaboratory.oncomigec.misc.QualityDefaults;
import org.jdom.Element;

public class PreprocessorParameters implements ParameterSet {
    private final byte umiQualThreshold, goodQualityThreshold;
    private final boolean trimAdapters, forceOverseq;
    private final int defaultOverseq;
    private final double minUmiMismatchRatio;

    public static final PreprocessorParameters DEFAULT = new PreprocessorParameters(
            QualityDefaults.PH33_BAD_QUAL, QualityDefaults.PH33_GOOD_QUAL,
            true,
            20.0, false, 5);

    public static final PreprocessorParameters IGNORE_QUAL = new PreprocessorParameters(
            (byte) 0, (byte) 0,
            true,
            20.0, false, 5);

    public PreprocessorParameters(byte umiQualThreshold, byte goodQualityThreshold,
                                  boolean trimAdapters,
                                  double minUmiMismatchRatio,
                                  boolean forceOverseq, int defaultOverseq) {
        this.umiQualThreshold = umiQualThreshold;
        this.goodQualityThreshold = goodQualityThreshold;
        this.trimAdapters = trimAdapters;
        this.minUmiMismatchRatio = minUmiMismatchRatio;
        this.forceOverseq = forceOverseq;
        this.defaultOverseq = defaultOverseq;
    }

    public byte getUmiQualThreshold() {
        return umiQualThreshold;
    }

    public byte getGoodQualityThreshold() {
        return goodQualityThreshold;
    }

    public double getMinUmiMismatchRatio() {
        return minUmiMismatchRatio;
    }

    public boolean trimAdapters() {
        return trimAdapters;
    }

    public boolean forceOverseq() {
        return forceOverseq;
    }

    public int getDefaultOverseq() {
        return defaultOverseq;
    }

    public PreprocessorParameters withUmiQualThreshold(byte umiQualThreshold) {
        return new PreprocessorParameters(umiQualThreshold, goodQualityThreshold,
                trimAdapters, minUmiMismatchRatio, forceOverseq, defaultOverseq);
    }

    public PreprocessorParameters withGoodQualityThreshold(byte goodQualityThreshold) {
        return new PreprocessorParameters(umiQualThreshold, goodQualityThreshold,
                trimAdapters, minUmiMismatchRatio, forceOverseq, defaultOverseq);
    }

    public PreprocessorParameters withTrimAdapters(boolean trimAdapters) {
        return new PreprocessorParameters(umiQualThreshold, goodQualityThreshold,
                trimAdapters, minUmiMismatchRatio, forceOverseq, defaultOverseq);
    }

    public PreprocessorParameters withForceOverseq(boolean forceOverseq) {
        return new PreprocessorParameters(umiQualThreshold, goodQualityThreshold,
                trimAdapters, minUmiMismatchRatio, forceOverseq, defaultOverseq);
    }

    public PreprocessorParameters withDefaultOverseq(int defaultOverseq) {
        return new PreprocessorParameters(umiQualThreshold, goodQualityThreshold,
                trimAdapters, minUmiMismatchRatio, forceOverseq, defaultOverseq);
    }

    public PreprocessorParameters withMinUmiMismatchRatio(double minUmiMismatchRatio) {
        return new PreprocessorParameters(umiQualThreshold, goodQualityThreshold,
                trimAdapters, minUmiMismatchRatio, forceOverseq, defaultOverseq);
    }

    @Override
    public Element toXml() {
        Element e = new Element("PreprocessorParameters");
        e.addContent(new Element("umiQualThreshold").setText(Byte.toString(umiQualThreshold)));
        e.addContent(new Element("goodQualityThreshold").setText(Byte.toString(goodQualityThreshold)));
        e.addContent(new Element("trimAdapters").setText(Boolean.toString(trimAdapters)));
        e.addContent(new Element("minUmiMismatchRatio").setText(Double.toString(minUmiMismatchRatio)));
        e.addContent(new Element("forceOverseq").setText(Boolean.toString(forceOverseq)));
        e.addContent(new Element("defaultOverseq").setText(Integer.toString(defaultOverseq)));
        return e;
    }

    public static PreprocessorParameters fromXml(Element parent) {
        Element e = parent.getChild("PreprocessorParameters");
        return new PreprocessorParameters(
                Byte.parseByte(e.getChildTextTrim("umiQualThreshold")),
                Byte.parseByte(e.getChildTextTrim("goodQualityThreshold")),
                Boolean.parseBoolean(e.getChildTextTrim("trimAdapters")),
                Double.parseDouble(e.getChildTextTrim("minUmiMismatchRatio")),
                Boolean.parseBoolean(e.getChildTextTrim("forceOverseq")),
                Integer.parseInt(e.getChildTextTrim("defaultOverseq"))
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PreprocessorParameters that = (PreprocessorParameters) o;

        if (defaultOverseq != that.defaultOverseq) return false;
        if (forceOverseq != that.forceOverseq) return false;
        if (goodQualityThreshold != that.goodQualityThreshold) return false;
        if (Double.compare(that.minUmiMismatchRatio, minUmiMismatchRatio) != 0) return false;
        if (trimAdapters != that.trimAdapters) return false;
        if (umiQualThreshold != that.umiQualThreshold) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) umiQualThreshold;
        result = 31 * result + (int) goodQualityThreshold;
        result = 31 * result + (trimAdapters ? 1 : 0);
        result = 31 * result + (forceOverseq ? 1 : 0);
        result = 31 * result + defaultOverseq;
        temp = Double.doubleToLongBits(minUmiMismatchRatio);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
