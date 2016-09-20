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

package com.antigenomics.mageri.core.assemble;

import com.antigenomics.mageri.misc.ParameterSet;
import org.jdom.Element;

public final class AssemblerParameters implements ParameterSet {
    private final boolean qualityWeightedMode;
    private final int offsetRange, anchorRegion, maxMMs, maxConsequentMMs;
    private final int minReadSize;
    private final double maxDroppedReadsRatio, maxDroppedReadsRatioAfterRescue,
            maxTrimmedConsensusBasesRatio, minMatchedBasesInRealignedReadRatio;
    private final boolean cqsRescue, qualityTrimming, greedyExtend;

    public static AssemblerParameters DEFAULT = new AssemblerParameters(
            4, 8, 4, 3,
            true,
            0.3, 0.0, 0.3, 0.0,
            false, true, true);

    public static AssemblerParameters TORRENT454 = new AssemblerParameters(
            4, 8, 4, 3,
            true,
            0.7, 0.3, 0.3, 0.5,
            true, false, true);

    public AssemblerParameters(int offsetRange, int anchorRegion, int maxMMs, int maxConsequentMMs,
                               boolean qualityWeightedMode, double maxDroppedReadsRatio,
                               double maxDroppedReadsRatioAfterRescue,
                               double maxTrimmedConsensusBasesRatio, double minMatchedBasesInRealignedReadRatio,
                               boolean cqsRescue, boolean qualityTrimming, boolean greedyExtend) {
        this.offsetRange = offsetRange;
        this.anchorRegion = anchorRegion;
        this.maxMMs = maxMMs;
        this.maxConsequentMMs = maxConsequentMMs;
        this.qualityWeightedMode = qualityWeightedMode;
        this.minReadSize = 2 * offsetRange + 2 * anchorRegion;
        this.maxDroppedReadsRatio = maxDroppedReadsRatio;
        this.maxDroppedReadsRatioAfterRescue = maxDroppedReadsRatioAfterRescue;
        this.maxTrimmedConsensusBasesRatio = maxTrimmedConsensusBasesRatio;
        this.minMatchedBasesInRealignedReadRatio = minMatchedBasesInRealignedReadRatio;
        this.cqsRescue = cqsRescue;
        this.qualityTrimming = qualityTrimming;
        this.greedyExtend = greedyExtend;
    }

    public boolean qualityWeightedMode() {
        return qualityWeightedMode;
    }

    public int getOffsetRange() {
        return offsetRange;
    }

    public int getAnchorRegion() {
        return anchorRegion;
    }

    public int getMaxMMs() {
        return maxMMs;
    }

    public int getMaxConsequentMMs() {
        return maxConsequentMMs;
    }

    public int getMinReadSize() {
        return minReadSize;
    }

    public double getMaxDroppedReadsRatio() {
        return maxDroppedReadsRatio;
    }

    public double getMaxDroppedReadsRatioAfterRescue() {
        return maxDroppedReadsRatioAfterRescue;
    }

    public boolean doCqsRescue() {
        return cqsRescue;
    }

    public boolean performQualityTrimming() {
        return qualityTrimming;
    }

    public boolean greedyExtend() {
        return greedyExtend;
    }

    public double getMaxTrimmedConsensusBasesRatio() {
        return maxTrimmedConsensusBasesRatio;
    }

    public double getMinMatchedBasesInRealignedReadRatio() {
        return minMatchedBasesInRealignedReadRatio;
    }

    public AssemblerParameters withQualityWeightedMode(boolean qualityWeightedMode) {
        return new AssemblerParameters(offsetRange, anchorRegion, maxMMs, maxConsequentMMs,
                qualityWeightedMode, maxDroppedReadsRatio, maxDroppedReadsRatioAfterRescue,
                maxTrimmedConsensusBasesRatio, minMatchedBasesInRealignedReadRatio,
                cqsRescue, qualityTrimming, greedyExtend);
    }

    public AssemblerParameters withOffsetRange(int offsetRange) {
        return new AssemblerParameters(offsetRange, anchorRegion, maxMMs, maxConsequentMMs,
                qualityWeightedMode, maxDroppedReadsRatio, maxDroppedReadsRatioAfterRescue,
                maxTrimmedConsensusBasesRatio, minMatchedBasesInRealignedReadRatio,
                cqsRescue, qualityTrimming, greedyExtend);
    }

    public AssemblerParameters withAnchorRegion(int anchorRegion) {
        return new AssemblerParameters(offsetRange, anchorRegion, maxMMs, maxConsequentMMs,
                qualityWeightedMode, maxDroppedReadsRatio, maxDroppedReadsRatioAfterRescue,
                maxTrimmedConsensusBasesRatio, minMatchedBasesInRealignedReadRatio,
                cqsRescue, qualityTrimming, greedyExtend);
    }

    public AssemblerParameters withMaxMMs(int maxMMs) {
        return new AssemblerParameters(offsetRange, anchorRegion, maxMMs, maxConsequentMMs,
                qualityWeightedMode, maxDroppedReadsRatio, maxDroppedReadsRatioAfterRescue,
                maxTrimmedConsensusBasesRatio, minMatchedBasesInRealignedReadRatio,
                cqsRescue, qualityTrimming, greedyExtend);
    }

    public AssemblerParameters withMaxConsequentMMs(int maxConsequentMMs) {
        return new AssemblerParameters(offsetRange, anchorRegion, maxMMs, maxConsequentMMs,
                qualityWeightedMode, maxDroppedReadsRatio, maxDroppedReadsRatioAfterRescue,
                maxTrimmedConsensusBasesRatio, minMatchedBasesInRealignedReadRatio,
                cqsRescue, qualityTrimming, greedyExtend);
    }

    public AssemblerParameters withMinReadSize(int minReadSize) {
        return new AssemblerParameters(offsetRange, anchorRegion, maxMMs, maxConsequentMMs,
                qualityWeightedMode, maxDroppedReadsRatio, maxDroppedReadsRatioAfterRescue,
                maxTrimmedConsensusBasesRatio, minMatchedBasesInRealignedReadRatio,
                cqsRescue, qualityTrimming, greedyExtend);
    }

    public AssemblerParameters withMaxDroppedReadsRatio(double maxDroppedReadsRatio) {
        return new AssemblerParameters(offsetRange, anchorRegion, maxMMs, maxConsequentMMs,
                qualityWeightedMode, maxDroppedReadsRatio, maxDroppedReadsRatioAfterRescue,
                maxTrimmedConsensusBasesRatio, minMatchedBasesInRealignedReadRatio,
                cqsRescue, qualityTrimming, greedyExtend);
    }

    public AssemblerParameters withMaxDroppedReadsAfterRescue(double maxDroppedReadsRatioAfterRescue) {
        return new AssemblerParameters(offsetRange, anchorRegion, maxMMs, maxConsequentMMs,
                qualityWeightedMode, maxDroppedReadsRatio, maxDroppedReadsRatioAfterRescue,
                maxTrimmedConsensusBasesRatio, minMatchedBasesInRealignedReadRatio,
                cqsRescue, qualityTrimming, greedyExtend);
    }

    public AssemblerParameters withCqsRescue(boolean cqsRescue) {
        return new AssemblerParameters(offsetRange, anchorRegion, maxMMs, maxConsequentMMs,
                qualityWeightedMode, maxDroppedReadsRatio, maxDroppedReadsRatioAfterRescue,
                maxTrimmedConsensusBasesRatio, minMatchedBasesInRealignedReadRatio,
                cqsRescue, qualityTrimming, greedyExtend);
    }

    public AssemblerParameters withQualityTrimming(boolean qualityTrimming) {
        return new AssemblerParameters(offsetRange, anchorRegion, maxMMs, maxConsequentMMs,
                qualityWeightedMode, maxDroppedReadsRatio, maxDroppedReadsRatioAfterRescue,
                maxTrimmedConsensusBasesRatio, minMatchedBasesInRealignedReadRatio,
                cqsRescue, qualityTrimming, greedyExtend);
    }

    public AssemblerParameters withGreedyExtend(boolean greedyExtend) {
        return new AssemblerParameters(offsetRange, anchorRegion, maxMMs, maxConsequentMMs,
                qualityWeightedMode, maxDroppedReadsRatio, maxDroppedReadsRatioAfterRescue,
                maxTrimmedConsensusBasesRatio, minMatchedBasesInRealignedReadRatio,
                cqsRescue, qualityTrimming, greedyExtend);
    }

    public AssemblerParameters withMaxTrimmedConsensusBasesRatio(double maxTrimmedConsensusBasesRatio) {
        return new AssemblerParameters(offsetRange, anchorRegion, maxMMs, maxConsequentMMs,
                qualityWeightedMode, maxDroppedReadsRatio, maxDroppedReadsRatioAfterRescue,
                maxTrimmedConsensusBasesRatio, minMatchedBasesInRealignedReadRatio,
                cqsRescue, qualityTrimming, greedyExtend);
    }

    public AssemblerParameters withMinMatchedBasesInRealignedReadRatio(double minMatchedBasesInRealignedReadRatio) {
        return new AssemblerParameters(offsetRange, anchorRegion, maxMMs, maxConsequentMMs,
                qualityWeightedMode, maxDroppedReadsRatio, maxDroppedReadsRatioAfterRescue,
                maxTrimmedConsensusBasesRatio, minMatchedBasesInRealignedReadRatio,
                cqsRescue, qualityTrimming, greedyExtend);
    }

    @Override
    public Element toXml() {
        Element e = new Element("AssemblerParameters");
        e.addContent(new Element("offsetRange").setText(Integer.toString(offsetRange)));
        e.addContent(new Element("anchorRegion").setText(Integer.toString(anchorRegion)));
        e.addContent(new Element("maxMMs").setText(Integer.toString(maxMMs)));
        e.addContent(new Element("maxConsequentMMs").setText(Integer.toString(maxConsequentMMs)));
        e.addContent(new Element("qualityWeightedMode").setText(Boolean.toString(qualityWeightedMode)));
        e.addContent(new Element("maxDroppedReadsRatio").setText(Double.toString(maxDroppedReadsRatio)));
        e.addContent(new Element("maxDroppedReadsRatioAfterRescue").setText(Double.toString(maxDroppedReadsRatioAfterRescue)));
        e.addContent(new Element("maxTrimmedConsensusBasesRatio").setText(Double.toString(maxTrimmedConsensusBasesRatio)));
        e.addContent(new Element("minMatchedBasesInRealignedReadRatio").setText(Double.toString(minMatchedBasesInRealignedReadRatio)));
        e.addContent(new Element("cqsRescue").setText(Boolean.toString(cqsRescue)));
        e.addContent(new Element("qualityTrimming").setText(Boolean.toString(qualityTrimming)));
        e.addContent(new Element("greedyExtend").setText(Boolean.toString(greedyExtend)));
        return e;
    }

    public static AssemblerParameters fromXml(Element parent) {
        Element e = parent.getChild("AssemblerParameters");
        return new AssemblerParameters(
                Integer.parseInt(e.getChildTextTrim("offsetRange")),
                Integer.parseInt(e.getChildTextTrim("anchorRegion")),
                Integer.parseInt(e.getChildTextTrim("maxMMs")),
                Integer.parseInt(e.getChildTextTrim("maxConsequentMMs")),
                Boolean.parseBoolean(e.getChildTextTrim("qualityWeightedMode")),
                Double.parseDouble(e.getChildTextTrim("maxDroppedReadsRatio")),
                Double.parseDouble(e.getChildTextTrim("maxDroppedReadsRatioAfterRescue")),
                Double.parseDouble(e.getChildTextTrim("maxTrimmedConsensusBasesRatio")),
                Double.parseDouble(e.getChildTextTrim("minMatchedBasesInRealignedReadRatio")),
                Boolean.parseBoolean(e.getChildTextTrim("cqsRescue")),
                Boolean.parseBoolean(e.getChildTextTrim("qualityTrimming")),
                Boolean.parseBoolean(e.getChildTextTrim("greedyExtend"))
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AssemblerParameters that = (AssemblerParameters) o;

        if (qualityWeightedMode != that.qualityWeightedMode) return false;
        if (offsetRange != that.offsetRange) return false;
        if (anchorRegion != that.anchorRegion) return false;
        if (maxMMs != that.maxMMs) return false;
        if (maxConsequentMMs != that.maxConsequentMMs) return false;
        if (minReadSize != that.minReadSize) return false;
        if (Double.compare(that.maxDroppedReadsRatio, maxDroppedReadsRatio) != 0) return false;
        if (Double.compare(that.maxDroppedReadsRatioAfterRescue, maxDroppedReadsRatioAfterRescue) != 0) return false;
        if (Double.compare(that.maxTrimmedConsensusBasesRatio, maxTrimmedConsensusBasesRatio) != 0) return false;
        if (Double.compare(that.minMatchedBasesInRealignedReadRatio, minMatchedBasesInRealignedReadRatio) != 0)
            return false;
        if (cqsRescue != that.cqsRescue) return false;
        if (qualityTrimming != that.qualityTrimming) return false;
        return greedyExtend == that.greedyExtend;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (qualityWeightedMode ? 1 : 0);
        result = 31 * result + offsetRange;
        result = 31 * result + anchorRegion;
        result = 31 * result + maxMMs;
        result = 31 * result + maxConsequentMMs;
        result = 31 * result + minReadSize;
        temp = Double.doubleToLongBits(maxDroppedReadsRatio);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxDroppedReadsRatioAfterRescue);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxTrimmedConsensusBasesRatio);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minMatchedBasesInRealignedReadRatio);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (cqsRescue ? 1 : 0);
        result = 31 * result + (qualityTrimming ? 1 : 0);
        result = 31 * result + (greedyExtend ? 1 : 0);
        return result;
    }
}
