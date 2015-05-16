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

package com.milaboratory.oncomigec.preprocessing;

import com.milaboratory.oncomigec.misc.ParameterSet;
import org.jdom.Element;

public class DemultiplexParameters implements ParameterSet {
    private final boolean orientedReads;
    private final int maxTruncations;
    private final double maxGoodQualMMRatio, maxLowQualityMMRatio;
    private final byte lowQualityThreshold;

    public static DemultiplexParameters DEFAULT = new DemultiplexParameters(false,
            2, 0.05, 0.1,
            (byte) 20),
            ORIENTED = new DemultiplexParameters(true,
                    2, 0.05, 0.1,
                    (byte) 20);

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

    public DemultiplexParameters withOrientedReads(boolean orientedReads) {
        return new DemultiplexParameters(orientedReads,
                maxTruncations, maxGoodQualMMRatio, maxLowQualityMMRatio,
                lowQualityThreshold);
    }

    public DemultiplexParameters withMaxTruncations(int maxTruncations) {
        return new DemultiplexParameters(orientedReads,
                maxTruncations, maxGoodQualMMRatio, maxLowQualityMMRatio,
                lowQualityThreshold);
    }

    public DemultiplexParameters withMaxGoodQualMMRatio(double maxGoodQualMMRatio) {
        return new DemultiplexParameters(orientedReads,
                maxTruncations, maxGoodQualMMRatio, maxLowQualityMMRatio,
                lowQualityThreshold);
    }

    public DemultiplexParameters withMaxLowQualityMMRatio(double maxLowQualityMMRatio) {
        return new DemultiplexParameters(orientedReads,
                maxTruncations, maxGoodQualMMRatio, maxLowQualityMMRatio,
                lowQualityThreshold);
    }

    public DemultiplexParameters withLowQualityThreshold(byte lowQualityThreshold) {
        return new DemultiplexParameters(orientedReads,
                maxTruncations, maxGoodQualMMRatio, maxLowQualityMMRatio,
                lowQualityThreshold);
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
