package com.milaboratory.migec2.core.haplotype;

import com.milaboratory.migec2.util.ParameterSet;
import org.jdom.Element;

public final class HaplotypeTreeParameters implements ParameterSet {
    private final boolean filterSingleMigs;
    private final double childMajorRatioForPValue, pValueThreshold;
    private final int depth;

    public static HaplotypeTreeParameters DEFAULT = new HaplotypeTreeParameters(true, 0.5, 0.05, 2),
            NO_PVALUE_THRESHOLD = new HaplotypeTreeParameters(true, 0.5, 1.0, 2);

    public HaplotypeTreeParameters(boolean filterSingleMigs, double childMajorRatioForPValue,
                                   double pValueThreshold, int depth) {
        this.filterSingleMigs = filterSingleMigs;
        this.childMajorRatioForPValue = childMajorRatioForPValue;
        this.pValueThreshold = pValueThreshold;
        this.depth = depth;
    }

    public boolean filterSingleMigs() {
        return filterSingleMigs;
    }

    public double getChildMajorRatioForPValue() {
        return childMajorRatioForPValue;
    }

    public double getPValueThreshold() {
        return pValueThreshold;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public Element toXml() {
        Element e = new Element("HaplotypeTreeParameters");
        e.addContent(new Element("filterSingleMigs").setText(Boolean.toString(filterSingleMigs)));
        e.addContent(new Element("childMajorRatioForPValue").setText(Double.toString(childMajorRatioForPValue)));
        e.addContent(new Element("pValueThreshold").setText(Double.toString(pValueThreshold)));
        e.addContent(new Element("depth").setText(Integer.toString(depth)));
        return e;
    }

    public static HaplotypeTreeParameters fromXml(Element parent) {
        Element e = parent.getChild("HaplotypeTreeParameters");
        return new HaplotypeTreeParameters(
                Boolean.parseBoolean(e.getChildTextTrim("filterSingleMigs")),
                Double.parseDouble(e.getChildTextTrim("childMajorRatioForPValue")),
                Double.parseDouble(e.getChildTextTrim("pValueThreshold")),
                Integer.parseInt(e.getChildTextTrim("depth"))
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HaplotypeTreeParameters that = (HaplotypeTreeParameters) o;

        if (Double.compare(that.childMajorRatioForPValue, childMajorRatioForPValue) != 0) return false;
        if (depth != that.depth) return false;
        if (filterSingleMigs != that.filterSingleMigs) return false;
        if (Double.compare(that.pValueThreshold, pValueThreshold) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (filterSingleMigs ? 1 : 0);
        temp = Double.doubleToLongBits(childMajorRatioForPValue);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(pValueThreshold);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + depth;
        return result;
    }
}
