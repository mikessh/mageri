package com.milaboratory.oncomigec.core.variant;

import com.milaboratory.oncomigec.misc.ParameterSet;
import org.jdom.Element;

public final class VariantCallerParameters implements ParameterSet {
    private final double order, modelCycles, modelEfficiency;
    private final int qualityThreshold, singletonFrequencyThreshold, coverageThreshold;

    public static VariantCallerParameters DEFAULT = new VariantCallerParameters(1.0, 20.0, 1.95,
            20, 10000, 100);

    public VariantCallerParameters(double order, double modelCycles, double modelEfficiency,
                                   int qualityThreshold, int singletonFrequencyThreshold, int coverageThreshold) {
        if (modelCycles < 10 || modelCycles > 40)
            throw new IllegalArgumentException("(model parameters) Number of PCR cycles should be in [10,40]");

        if (modelEfficiency < 1.8d || modelEfficiency >= 2.0d)
            throw new IllegalArgumentException("(model parameters) PCR efficiency should be set in [1.8, 2.0)");

        if (qualityThreshold < 0)
            throw new IllegalArgumentException("(filter parameters) Quality threshold should be >= 0");
        if (singletonFrequencyThreshold <= 1)
            throw new IllegalArgumentException("(filter parameters) Singleton filter ratio should > 1");
        if (coverageThreshold < 0)
            throw new IllegalArgumentException("(filter parameters) Coverage threshold should be >= 0");

        this.order = order;
        this.modelCycles = modelCycles;
        this.modelEfficiency = modelEfficiency;
        this.singletonFrequencyThreshold = singletonFrequencyThreshold;
        this.qualityThreshold = qualityThreshold;
        this.coverageThreshold = coverageThreshold;
    }

    public double getOrder() {
        return order;
    }

    public double getModelCycles() {
        return modelCycles;
    }

    public double getModelEfficiency() {
        return modelEfficiency;
    }

    public int getQualityThreshold() {
        return qualityThreshold;
    }

    public int getSingletonFrequencyThreshold() {
        return singletonFrequencyThreshold;
    }

    public int getCoverageThreshold() {
        return coverageThreshold;
    }

    @Override
    public Element toXml() {
        Element e = new Element("VariantCallerParameters");
        e.addContent(new Element("order").setText(Double.toString(order)));
        e.addContent(new Element("modelCycles").setText(Double.toString(modelCycles)));
        e.addContent(new Element("modelEfficiency").setText(Double.toString(modelEfficiency)));
        e.addContent(new Element("qualityThreshold").setText(Integer.toString(qualityThreshold)));
        e.addContent(new Element("singletonFrequencyThreshold").setText(Integer.toString(singletonFrequencyThreshold)));
        e.addContent(new Element("coverageThreshold").setText(Integer.toString(coverageThreshold)));
        return e;
    }

    public static VariantCallerParameters fromXml(Element parent) {
        Element e = parent.getChild("VariantCallerParameters");
        return new VariantCallerParameters(
                Double.parseDouble(e.getChildTextTrim("order")),
                Double.parseDouble(e.getChildTextTrim("modelCycles")),
                Double.parseDouble(e.getChildTextTrim("modelEfficiency")),
                Integer.parseInt(e.getChildTextTrim("qualityThreshold")),
                Integer.parseInt(e.getChildTextTrim("singletonFrequencyThreshold")),
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
        if (Double.compare(that.order, order) != 0) return false;
        if (qualityThreshold != that.qualityThreshold) return false;
        if (singletonFrequencyThreshold != that.singletonFrequencyThreshold) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(order);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(modelCycles);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(modelEfficiency);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + qualityThreshold;
        result = 31 * result + singletonFrequencyThreshold;
        result = 31 * result + coverageThreshold;
        return result;
    }
}
