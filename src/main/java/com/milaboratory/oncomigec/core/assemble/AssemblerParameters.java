/*
 * Copyright (c) 2014-2015, Bolotin Dmitry, Chudakov Dmitry, Shugay Mikhail
 * (here and after addressed as Inventors)
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact the Inventors using one of the following
 * email addresses: chudakovdm@mail.ru, chudakovdm@gmail.com
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */

package com.milaboratory.oncomigec.core.assemble;

import com.milaboratory.oncomigec.misc.ParameterSet;
import org.jdom.Element;

public final class AssemblerParameters implements ParameterSet {
    private final boolean qualityWeightedMode;
    private final int offsetRange, anchorRegion, maxMMs, maxConsequentMMs;
    private final int minReadSize;
    private final double maxDroppedReadsRatio;
    private final boolean cqsRescue, qualityTrimming, greedyExtend;

    public static AssemblerParameters DEFAULT = new AssemblerParameters(
            4, 8, 4, 3,
            true,
            0.3,
            false, true, true);

    public static AssemblerParameters TORRENT454 = new AssemblerParameters(
            4, 8, 4, 3,
            true,
            0.6,
            true, false, false);

    public AssemblerParameters(int offsetRange, int anchorRegion, int maxMMs, int maxConsequentMMs,
                               boolean qualityWeightedMode, double maxDroppedReadsRatio,
                               boolean cqsRescue, boolean qualityTrimming, boolean greedyExtend) {
        this.offsetRange = offsetRange;
        this.anchorRegion = anchorRegion;
        this.maxMMs = maxMMs;
        this.maxConsequentMMs = maxConsequentMMs;
        this.qualityWeightedMode = qualityWeightedMode;
        this.minReadSize = 2 * offsetRange + 2 * anchorRegion;
        this.maxDroppedReadsRatio = maxDroppedReadsRatio;
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

    public boolean doCqsRescue() {
        return cqsRescue;
    }

    public boolean performQualityTrimming() {
        return qualityTrimming;
    }

    public boolean greedyExtend() {
        return greedyExtend;
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

        if (anchorRegion != that.anchorRegion) return false;
        if (cqsRescue != that.cqsRescue) return false;
        if (greedyExtend != that.greedyExtend) return false;
        if (maxConsequentMMs != that.maxConsequentMMs) return false;
        if (Double.compare(that.maxDroppedReadsRatio, maxDroppedReadsRatio) != 0) return false;
        if (maxMMs != that.maxMMs) return false;
        if (minReadSize != that.minReadSize) return false;
        if (offsetRange != that.offsetRange) return false;
        if (qualityTrimming != that.qualityTrimming) return false;
        if (qualityWeightedMode != that.qualityWeightedMode) return false;

        return true;
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
        result = 31 * result + (cqsRescue ? 1 : 0);
        result = 31 * result + (qualityTrimming ? 1 : 0);
        result = 31 * result + (greedyExtend ? 1 : 0);
        return result;
    }
}
