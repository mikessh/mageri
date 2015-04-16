package com.milaboratory.oncomigec.core.variant;

import com.milaboratory.oncomigec.misc.ParameterSet;
import org.jdom.Element;

public final class VariantCallerParameters implements ParameterSet {
    private final double modelCycles, modelEfficiency;
    private final int qualThreshold, singletonFilterRatio, coverageThreshold;

    public static VariantCallerParameters DEFAULT = new VariantCallerParameters(20.0, 1.95,
            20, 10000, 100);

    public VariantCallerParameters(double modelCycles, double modelEfficiency,
                                   int qualThreshold, int singletonFilterRatio, int coverageThreshold) {
        if (modelCycles < 10 || modelCycles > 40)
            throw new IllegalArgumentException("(model parameters) Number of PCR cycles should be in [10,40]");

        if (modelEfficiency < 1.8d || modelEfficiency >= 2.0d)
            throw new IllegalArgumentException("(model parameters) PCR efficiency should be set in [1.8, 2.0)");

        if (qualThreshold < 0)
            throw new IllegalArgumentException("(filter parameters) Quality threshold should be >= 0");
        if (singletonFilterRatio <= 1)
            throw new IllegalArgumentException("(filter parameters) Singleton filter ratio should > 1");
        if (coverageThreshold < 0)
            throw new IllegalArgumentException("(filter parameters) Coverage threshold should be >= 0");

        this.modelCycles = modelCycles;
        this.modelEfficiency = modelEfficiency;
        this.singletonFilterRatio = singletonFilterRatio;
        this.qualThreshold = qualThreshold;
        this.coverageThreshold = coverageThreshold;
    }


    public double getModelCycles() {
        return modelCycles;
    }

    public double getModelEfficiency() {
        return modelEfficiency;
    }

    public int getQualThreshold() {
        return qualThreshold;
    }

    public int getSingletonFilterRatio() {
        return singletonFilterRatio;
    }

    public int getCoverageThreshold() {
        return coverageThreshold;
    }

    @Override
    public Element toXml() {
        Element e = new Element("VariantCallerParameters");
        e.addContent(new Element("modelCycles").setText(Double.toString(modelCycles)));
        e.addContent(new Element("modelEfficiency").setText(Double.toString(modelEfficiency)));
        e.addContent(new Element("singletonFilterRatio").setText(Integer.toString(singletonFilterRatio)));
        e.addContent(new Element("qualThreshold").setText(Integer.toString(qualThreshold)));
        e.addContent(new Element("coverageThreshold").setText(Integer.toString(coverageThreshold)));
        return e;
    }

    public static VariantCallerParameters fromXml(Element parent) {
        Element e = parent.getChild("VariantCallerParameters");
        return new VariantCallerParameters(
                Double.parseDouble(e.getChildTextTrim("modelCycles")),
                Double.parseDouble(e.getChildTextTrim("modelEfficiency")),
                Integer.parseInt(e.getChildTextTrim("singletonFilterRatio")),
                Integer.parseInt(e.getChildTextTrim("qualThreshold")),
                Integer.parseInt(e.getChildTextTrim("coverageThreshold"))
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VariantCallerParameters that = (VariantCallerParameters) o;

        if (coverageThreshold != that.coverageThreshold) return false;
        if (Double.compare(that.modelCycles, modelCycles) != 0) return false;
        if (Double.compare(that.modelEfficiency, modelEfficiency) != 0) return false;
        if (qualThreshold != that.qualThreshold) return false;
        if (singletonFilterRatio != that.singletonFilterRatio) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(modelCycles);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(modelEfficiency);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + qualThreshold;
        result = 31 * result + singletonFilterRatio;
        result = 31 * result + coverageThreshold;
        return result;
    }
}
