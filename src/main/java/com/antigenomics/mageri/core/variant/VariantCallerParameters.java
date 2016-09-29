/*
 * Copyright 2014-2016 Mikhail Shugay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.antigenomics.mageri.core.variant;

import com.antigenomics.mageri.core.variant.model.ErrorModelType;
import com.antigenomics.mageri.core.variant.model.SubstitutionErrorMatrix;
import com.antigenomics.mageri.misc.ParameterSet;
import org.jdom.Element;

public final class VariantCallerParameters implements ParameterSet {
    private final double modelOrder, modelCycles, modelEfficiency;
    private final ErrorModelType errorModelType;
    private final int qualityThreshold, singletonFrequencyThreshold, coverageThreshold;
    private final String substitutionErrorRateMatrix;
    private final SubstitutionErrorMatrix parsedSubstitutionErrorRateMatrix;

    public static VariantCallerParameters DEFAULT = new VariantCallerParameters(
            20, 10000, 100,
            ErrorModelType.MinorBased,
            1.0, 20.0, 1.95,
            SubstitutionErrorMatrix.DEFAULT.toString());

    public VariantCallerParameters(int qualityThreshold, int singletonFrequencyThreshold, int coverageThreshold,
                                   ErrorModelType errorModelType,
                                   double modelOrder, double modelCycles, double modelEfficiency,
                                   String substitutionErrorRateMatrix) {
        if (modelCycles < 10 || modelCycles > 40)
            throw new IllegalArgumentException("(model parameters) Number of PCR cycles should be in [10,40]");

        if (modelEfficiency <= 1.0d || modelEfficiency >= 2.0d)
            throw new IllegalArgumentException("(model parameters) PCR efficiency should be set in (1.0, 2.0)");

        if (qualityThreshold < 0)
            throw new IllegalArgumentException("(filter parameters) Quality threshold should be >= 0");
        if (singletonFrequencyThreshold <= 1)
            throw new IllegalArgumentException("(filter parameters) Singleton filter ratio should > 1");
        if (coverageThreshold < 0)
            throw new IllegalArgumentException("(filter parameters) Coverage threshold should be >= 0");

        this.singletonFrequencyThreshold = singletonFrequencyThreshold;
        this.qualityThreshold = qualityThreshold;
        this.coverageThreshold = coverageThreshold;

        this.errorModelType = errorModelType;
        this.modelOrder = modelOrder;
        this.modelCycles = modelCycles;
        this.modelEfficiency = modelEfficiency;
        this.substitutionErrorRateMatrix = substitutionErrorRateMatrix;
        this.parsedSubstitutionErrorRateMatrix = SubstitutionErrorMatrix.fromString(substitutionErrorRateMatrix);
    }

    public double getModelOrder() {
        return modelOrder;
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

    public ErrorModelType getErrorModelType() {
        return errorModelType;
    }

    public String getSubstitutionErrorRateMatrix() {
        return substitutionErrorRateMatrix;
    }

    public SubstitutionErrorMatrix getParsedSubstitutionErrorRateMatrix() {
        return parsedSubstitutionErrorRateMatrix;
    }

    public int getSingletonFrequencyThreshold() {
        return singletonFrequencyThreshold;
    }

    public int getCoverageThreshold() {
        return coverageThreshold;
    }

    public VariantCallerParameters withOrder(double order) {
        return new VariantCallerParameters(
                qualityThreshold, singletonFrequencyThreshold, coverageThreshold,
                errorModelType,
                order, modelCycles, modelEfficiency,
                substitutionErrorRateMatrix);
    }

    public VariantCallerParameters withModelCycles(double modelCycles) {
        return new VariantCallerParameters(
                qualityThreshold, singletonFrequencyThreshold, coverageThreshold,
                errorModelType,
                modelOrder, modelCycles, modelEfficiency,
                substitutionErrorRateMatrix);
    }

    public VariantCallerParameters withModelEfficiency(double modelEfficiency) {
        return new VariantCallerParameters(
                qualityThreshold, singletonFrequencyThreshold, coverageThreshold,
                errorModelType,
                modelOrder, modelCycles, modelEfficiency,
                substitutionErrorRateMatrix);
    }

    public VariantCallerParameters withQualityThreshold(int qualityThreshold) {
        return new VariantCallerParameters(
                qualityThreshold, singletonFrequencyThreshold, coverageThreshold,
                errorModelType,
                modelOrder, modelCycles, modelEfficiency,
                substitutionErrorRateMatrix);
    }

    public VariantCallerParameters withSingletonFrequencyThreshold(int singletonFrequencyThreshold) {
        return new VariantCallerParameters(
                qualityThreshold, singletonFrequencyThreshold, coverageThreshold,
                errorModelType,
                modelOrder, modelCycles, modelEfficiency,
                substitutionErrorRateMatrix);
    }

    public VariantCallerParameters withCoverageThreshold(int coverageThreshold) {
        return new VariantCallerParameters(
                qualityThreshold, singletonFrequencyThreshold, coverageThreshold,
                errorModelType,
                modelOrder, modelCycles, modelEfficiency,
                substitutionErrorRateMatrix);
    }

    @Override
    public Element toXml() {
        Element e = new Element("VariantCallerParameters");
        e.addContent(new Element("qualityThreshold").setText(Integer.toString(qualityThreshold)));
        e.addContent(new Element("singletonFrequencyThreshold").setText(Integer.toString(singletonFrequencyThreshold)));
        e.addContent(new Element("coverageThreshold").setText(Integer.toString(coverageThreshold)));
        e.addContent(new Element("errorModelType").setText(errorModelType.toString()));
        e.addContent(new Element("modelOrder").setText(Double.toString(modelOrder)));
        e.addContent(new Element("modelCycles").setText(Double.toString(modelCycles)));
        e.addContent(new Element("modelEfficiency").setText(Double.toString(modelEfficiency)));
        e.addContent(new Element("substitutionErrorRateMatrix").setText(substitutionErrorRateMatrix));
        return e;
    }

    public static VariantCallerParameters fromXml(Element parent) {
        Element e = parent.getChild("VariantCallerParameters");
        return new VariantCallerParameters(
                Integer.parseInt(e.getChildTextTrim("qualityThreshold")),
                Integer.parseInt(e.getChildTextTrim("singletonFrequencyThreshold")),
                Integer.parseInt(e.getChildTextTrim("coverageThreshold")),
                ErrorModelType.valueOf(e.getChildTextTrim("errorModelType")),
                Double.parseDouble(e.getChildTextTrim("modelOrder")),
                Double.parseDouble(e.getChildTextTrim("modelCycles")),
                Double.parseDouble(e.getChildTextTrim("modelEfficiency")),
                e.getChildTextTrim("substitutionErrorRateMatrix")
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VariantCallerParameters that = (VariantCallerParameters) o;

        if (Double.compare(that.modelOrder, modelOrder) != 0) return false;
        if (Double.compare(that.modelCycles, modelCycles) != 0) return false;
        if (Double.compare(that.modelEfficiency, modelEfficiency) != 0) return false;
        if (qualityThreshold != that.qualityThreshold) return false;
        if (singletonFrequencyThreshold != that.singletonFrequencyThreshold) return false;
        if (coverageThreshold != that.coverageThreshold) return false;
        if (errorModelType != that.errorModelType) return false;
        return parsedSubstitutionErrorRateMatrix.equals(that.parsedSubstitutionErrorRateMatrix);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(modelOrder);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(modelCycles);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(modelEfficiency);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + errorModelType.hashCode();
        result = 31 * result + qualityThreshold;
        result = 31 * result + singletonFrequencyThreshold;
        result = 31 * result + coverageThreshold;
        result = 31 * result + parsedSubstitutionErrorRateMatrix.hashCode();
        return result;
    }
}
