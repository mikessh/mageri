package com.milaboratory.oncomigec.core.haplotype;

import com.milaboratory.oncomigec.util.ParameterSet;
import org.jdom.Element;

public final class HaplotypeTreeParameters implements ParameterSet {
    private final double errorMaskingTestMajorRatio, errorMaskingTestPvalueThreshold;
    private final int depth;

    public static HaplotypeTreeParameters DEFAULT = new HaplotypeTreeParameters(2.0, 0.05, 2),
            NO_MASKING_TEST = new HaplotypeTreeParameters(2.0, 1.0, 2);

    public HaplotypeTreeParameters(double errorMaskingTestMajorRatio,
                                   double errorMaskingTestPvalueThreshold, int depth) {
        this.errorMaskingTestMajorRatio = errorMaskingTestMajorRatio;
        this.errorMaskingTestPvalueThreshold = errorMaskingTestPvalueThreshold;
        this.depth = depth;

        if (errorMaskingTestPvalueThreshold < 0 ||
                errorMaskingTestPvalueThreshold > 1)
            throw new IllegalArgumentException("Error masking test P-value threshold should be set in [0, 1] range");
        if (errorMaskingTestMajorRatio < 1)
            throw new IllegalArgumentException("Error masking test major ratio should be greater than 1");
    }

    public double getErrorMaskingTestMajorRatio() {
        return errorMaskingTestMajorRatio;
    }

    public double getPValueThreshold() {
        return errorMaskingTestPvalueThreshold;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public Element toXml() {
        Element e = new Element("HaplotypeTreeParameters");
        e.addContent(new Element("errorMaskingTestMajorRatio").setText(Double.toString(errorMaskingTestMajorRatio)));
        e.addContent(new Element("errorMaskingTestPvalueThreshold").setText(Double.toString(errorMaskingTestPvalueThreshold)));
        e.addContent(new Element("depth").setText(Integer.toString(depth)));
        return e;
    }

    public static HaplotypeTreeParameters fromXml(Element parent) {
        Element e = parent.getChild("HaplotypeTreeParameters");
        return new HaplotypeTreeParameters(
                Double.parseDouble(e.getChildTextTrim("errorMaskingTestMajorRatio")),
                Double.parseDouble(e.getChildTextTrim("errorMaskingTestPvalueThreshold")),
                Integer.parseInt(e.getChildTextTrim("depth"))
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HaplotypeTreeParameters that = (HaplotypeTreeParameters) o;

        if (Double.compare(that.errorMaskingTestMajorRatio, errorMaskingTestMajorRatio) != 0) return false;
        if (depth != that.depth) return false;
        if (Double.compare(that.errorMaskingTestPvalueThreshold, errorMaskingTestPvalueThreshold) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(errorMaskingTestMajorRatio);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(errorMaskingTestPvalueThreshold);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + depth;
        return result;
    }
}
