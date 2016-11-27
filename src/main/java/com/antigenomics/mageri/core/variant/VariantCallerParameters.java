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
import com.antigenomics.mageri.core.variant.model.PresetErrorModel;
import com.antigenomics.mageri.core.variant.model.SubstitutionErrorMatrix;
import com.antigenomics.mageri.misc.ParameterSet;
import org.jdom.Element;

public final class VariantCallerParameters implements ParameterSet {
    private final double modelCycles, modelEfficiency, compoundQScoreSD, compoundQScoreMu;
    private final ErrorModelType errorModelType;
    private final int qualityThreshold, singletonFrequencyThreshold, coverageThreshold,
            modelCoverageThreshold, modelMinorCountThreshold;
    private final String modelPresetString;
    private final boolean noIndels, showAbsentVariants, shouldPropagate;

    public static VariantCallerParameters DEFAULT = new VariantCallerParameters(false,
            20, 10000, 100,
            ErrorModelType.Preset,
            false, 20.0, 1.8, 100, 10,
            PresetErrorModel.DEFAULT_VALUES,
            -0.46, 1.44,
            false);

    public VariantCallerParameters(boolean noIndels, int qualityThreshold, int singletonFrequencyThreshold, int coverageThreshold,
                                   ErrorModelType errorModelType,
                                   boolean shouldPropagate, double modelCycles, double modelEfficiency,
                                   int modelCoverageThreshold, int modelMinorCountThreshold,
                                   String modelPresetString,
                                   double compoundQScoreMu, double compoundQScoreSD,
                                   boolean showAbsentVariants) {
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

        this.noIndels = noIndels;
        this.singletonFrequencyThreshold = singletonFrequencyThreshold;
        this.qualityThreshold = qualityThreshold;
        this.coverageThreshold = coverageThreshold;

        this.modelCoverageThreshold = modelCoverageThreshold;
        this.modelMinorCountThreshold = modelMinorCountThreshold;

        this.errorModelType = errorModelType;
        this.shouldPropagate = shouldPropagate;
        this.modelCycles = modelCycles;
        this.modelEfficiency = modelEfficiency;
        this.modelPresetString = modelPresetString;

        this.compoundQScoreMu = compoundQScoreMu;
        this.compoundQScoreSD = compoundQScoreSD;

        this.showAbsentVariants = showAbsentVariants;
    }

    public boolean isNoIndels() {
        return noIndels;
    }

    public boolean isShowAbsentVariants() {
        return showAbsentVariants;
    }

    public boolean shouldPropagate() {
        return shouldPropagate;
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

    public String getModelPresetString() {
        return modelPresetString;
    }

    public int getSingletonFrequencyThreshold() {
        return singletonFrequencyThreshold;
    }

    public int getCoverageThreshold() {
        return coverageThreshold;
    }

    public int getModelCoverageThreshold() {
        return modelCoverageThreshold;
    }

    public int getModelMinorCountThreshold() {
        return modelMinorCountThreshold;
    }

    public boolean showAbsentVariants() {
        return showAbsentVariants;
    }

    public double getCompoundQScoreSD() {
        return compoundQScoreSD;
    }

    public double getCompoundQScoreMu() {
        return compoundQScoreMu;
    }

    public VariantCallerParameters withNoIndels(boolean noIndels) {
        return new VariantCallerParameters(noIndels,
                qualityThreshold, singletonFrequencyThreshold, coverageThreshold,
                errorModelType,
                shouldPropagate, modelCycles, modelEfficiency,
                modelCoverageThreshold, modelMinorCountThreshold,
                modelPresetString, compoundQScoreMu, compoundQScoreSD,
                showAbsentVariants);
    }

    public VariantCallerParameters withShouldPropagate(boolean shouldPropagate) {
        return new VariantCallerParameters(noIndels,
                qualityThreshold, singletonFrequencyThreshold, coverageThreshold,
                errorModelType,
                shouldPropagate, modelCycles, modelEfficiency,
                modelCoverageThreshold, modelMinorCountThreshold,
                modelPresetString, compoundQScoreMu, compoundQScoreSD,
                showAbsentVariants);
    }

    public VariantCallerParameters withModelCycles(double modelCycles) {
        return new VariantCallerParameters(noIndels,
                qualityThreshold, singletonFrequencyThreshold, coverageThreshold,
                errorModelType,
                shouldPropagate, modelCycles, modelEfficiency,
                modelCoverageThreshold, modelMinorCountThreshold,
                modelPresetString, compoundQScoreMu, compoundQScoreSD,
                showAbsentVariants);
    }

    public VariantCallerParameters withModelEfficiency(double modelEfficiency) {
        return new VariantCallerParameters(noIndels,
                qualityThreshold, singletonFrequencyThreshold, coverageThreshold,
                errorModelType,
                shouldPropagate, modelCycles, modelEfficiency,
                modelCoverageThreshold, modelMinorCountThreshold,
                modelPresetString, compoundQScoreMu, compoundQScoreSD,
                showAbsentVariants);
    }

    public VariantCallerParameters withQualityThreshold(int qualityThreshold) {
        return new VariantCallerParameters(noIndels,
                qualityThreshold, singletonFrequencyThreshold, coverageThreshold,
                errorModelType,
                shouldPropagate, modelCycles, modelEfficiency,
                modelCoverageThreshold, modelMinorCountThreshold,
                modelPresetString, compoundQScoreMu, compoundQScoreSD,
                showAbsentVariants);
    }

    public VariantCallerParameters withSingletonFrequencyThreshold(int singletonFrequencyThreshold) {
        return new VariantCallerParameters(noIndels,
                qualityThreshold, singletonFrequencyThreshold, coverageThreshold,
                errorModelType,
                shouldPropagate, modelCycles, modelEfficiency,
                modelCoverageThreshold, modelMinorCountThreshold,
                modelPresetString, compoundQScoreMu, compoundQScoreSD,
                showAbsentVariants);
    }

    public VariantCallerParameters withErrorModelType(ErrorModelType errorModelType) {
        return new VariantCallerParameters(noIndels,
                qualityThreshold, singletonFrequencyThreshold, coverageThreshold,
                errorModelType,
                shouldPropagate, modelCycles, modelEfficiency,
                modelCoverageThreshold, modelMinorCountThreshold,
                modelPresetString, compoundQScoreMu, compoundQScoreSD,
                showAbsentVariants);
    }

    public VariantCallerParameters withModelPresetString(String modelPresetString) {
        return new VariantCallerParameters(noIndels,
                qualityThreshold, singletonFrequencyThreshold, coverageThreshold,
                errorModelType,
                shouldPropagate, modelCycles, modelEfficiency,
                modelCoverageThreshold, modelMinorCountThreshold,
                modelPresetString, compoundQScoreMu, compoundQScoreSD,
                showAbsentVariants);
    }

    public VariantCallerParameters withCoverageThreshold(int coverageThreshold) {
        return new VariantCallerParameters(noIndels,
                qualityThreshold, singletonFrequencyThreshold, coverageThreshold,
                errorModelType,
                shouldPropagate, modelCycles, modelEfficiency,
                modelCoverageThreshold, modelMinorCountThreshold,
                modelPresetString, compoundQScoreMu, compoundQScoreSD,
                showAbsentVariants);
    }

    public VariantCallerParameters withModelCoverageThreshold(int modelCoverageThreshold) {
        return new VariantCallerParameters(noIndels,
                qualityThreshold, singletonFrequencyThreshold, coverageThreshold,
                errorModelType,
                shouldPropagate, modelCycles, modelEfficiency,
                modelCoverageThreshold, modelMinorCountThreshold,
                modelPresetString, compoundQScoreMu, compoundQScoreSD,
                showAbsentVariants);
    }

    public VariantCallerParameters withModelMinorCountThreshold(int modelMinorCountThreshold) {
        return new VariantCallerParameters(noIndels,
                qualityThreshold, singletonFrequencyThreshold, coverageThreshold,
                errorModelType,
                shouldPropagate, modelCycles, modelEfficiency,
                modelCoverageThreshold, modelMinorCountThreshold,
                modelPresetString, compoundQScoreMu, compoundQScoreSD,
                showAbsentVariants);
    }

    public VariantCallerParameters withCompoundQScoreMu(double compoundQScoreMu) {
        return new VariantCallerParameters(noIndels,
                qualityThreshold, singletonFrequencyThreshold, coverageThreshold,
                errorModelType,
                shouldPropagate, modelCycles, modelEfficiency,
                modelCoverageThreshold, modelMinorCountThreshold,
                modelPresetString, compoundQScoreMu, compoundQScoreSD,
                showAbsentVariants);
    }

    public VariantCallerParameters withCompoundQScoreSD(double compoundQScoreSD) {
        return new VariantCallerParameters(noIndels,
                qualityThreshold, singletonFrequencyThreshold, coverageThreshold,
                errorModelType,
                shouldPropagate, modelCycles, modelEfficiency,
                modelCoverageThreshold, modelMinorCountThreshold,
                modelPresetString, compoundQScoreMu, compoundQScoreSD,
                showAbsentVariants);
    }

    public VariantCallerParameters withShowAbsentVariants(boolean showAbsentVariants) {
        return new VariantCallerParameters(noIndels,
                qualityThreshold, singletonFrequencyThreshold, coverageThreshold,
                errorModelType,
                shouldPropagate, modelCycles, modelEfficiency,
                modelCoverageThreshold, modelMinorCountThreshold,
                modelPresetString, compoundQScoreMu, compoundQScoreSD,
                showAbsentVariants);
    }

    @Override
    public Element toXml() {
        Element e = new Element("VariantCallerParameters");
        // filters
        e.addContent(new Element("noIndels").setText(Boolean.toString(noIndels)));
        e.addContent(new Element("qualityThreshold").setText(Integer.toString(qualityThreshold)));
        e.addContent(new Element("singletonFrequencyThreshold").setText(Integer.toString(singletonFrequencyThreshold)));
        e.addContent(new Element("coverageThreshold").setText(Integer.toString(coverageThreshold)));
        e.addContent(new Element("showAbsentVariants").setText(Boolean.toString(showAbsentVariants)));
        // error model - generic
        e.addContent(new Element("errorModelType").setText(errorModelType.toString()));
        e.addContent(new Element("shouldPropagate").setText(Boolean.toString(shouldPropagate)));
        e.addContent(new Element("modelCycles").setText(Double.toString(modelCycles)));
        // preset model
        e.addContent(new Element("modelPresetString").setText(modelPresetString));
        // mbem model
        e.addContent(new Element("modelEfficiency").setText(Double.toString(modelEfficiency)));
        e.addContent(new Element("modelCoverageThreshold").setText(Integer.toString(modelCoverageThreshold)));
        e.addContent(new Element("modelMinorCountThreshold").setText(Integer.toString(modelMinorCountThreshold)));
        e.addContent(new Element("compoundQScoreMu").setText(Double.toString(compoundQScoreMu)));
        e.addContent(new Element("compoundQScoreSD").setText(Double.toString(compoundQScoreSD)));
        return e;
    }

    public static VariantCallerParameters fromXml(Element parent) {
        Element e = parent.getChild("VariantCallerParameters");
        return new VariantCallerParameters(
                Boolean.parseBoolean(e.getChildTextTrim("noIndels")),
                Integer.parseInt(e.getChildTextTrim("qualityThreshold")),
                Integer.parseInt(e.getChildTextTrim("singletonFrequencyThreshold")),
                Integer.parseInt(e.getChildTextTrim("coverageThreshold")),
                ErrorModelType.valueOf(e.getChildTextTrim("errorModelType")),
                Boolean.parseBoolean(e.getChildTextTrim("shouldPropagate")),
                Double.parseDouble(e.getChildTextTrim("modelCycles")),
                Double.parseDouble(e.getChildTextTrim("modelEfficiency")),
                Integer.parseInt(e.getChildTextTrim("modelCoverageThreshold")),
                Integer.parseInt(e.getChildTextTrim("modelMinorCountThreshold")),
                e.getChildTextTrim("modelPresetString"),
                Double.parseDouble(e.getChildTextTrim("compoundQScoreMu")),
                Double.parseDouble(e.getChildTextTrim("compoundQScoreSD")),
                Boolean.parseBoolean(e.getChildTextTrim("showAbsentVariants"))
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VariantCallerParameters that = (VariantCallerParameters) o;

        if (Double.compare(that.modelCycles, modelCycles) != 0) return false;
        if (Double.compare(that.modelEfficiency, modelEfficiency) != 0) return false;
        if (Double.compare(that.compoundQScoreSD, compoundQScoreSD) != 0) return false;
        if (Double.compare(that.compoundQScoreMu, compoundQScoreMu) != 0) return false;
        if (qualityThreshold != that.qualityThreshold) return false;
        if (singletonFrequencyThreshold != that.singletonFrequencyThreshold) return false;
        if (coverageThreshold != that.coverageThreshold) return false;
        if (modelCoverageThreshold != that.modelCoverageThreshold) return false;
        if (modelMinorCountThreshold != that.modelMinorCountThreshold) return false;
        if (noIndels != that.noIndels) return false;
        if (showAbsentVariants != that.showAbsentVariants) return false;
        if (shouldPropagate != that.shouldPropagate) return false;
        if (errorModelType != that.errorModelType) return false;
        return modelPresetString.equals(that.modelPresetString);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(modelCycles);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(modelEfficiency);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(compoundQScoreSD);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(compoundQScoreMu);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + errorModelType.hashCode();
        result = 31 * result + qualityThreshold;
        result = 31 * result + singletonFrequencyThreshold;
        result = 31 * result + coverageThreshold;
        result = 31 * result + modelCoverageThreshold;
        result = 31 * result + modelMinorCountThreshold;
        result = 31 * result + modelPresetString.hashCode();
        result = 31 * result + (noIndels ? 1 : 0);
        result = 31 * result + (showAbsentVariants ? 1 : 0);
        result = 31 * result + (shouldPropagate ? 1 : 0);
        return result;
    }
}
