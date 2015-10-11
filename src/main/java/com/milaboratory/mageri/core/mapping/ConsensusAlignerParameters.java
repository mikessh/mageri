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

package com.milaboratory.mageri.core.mapping;

import com.milaboratory.mageri.misc.ParameterSet;
import com.milaboratory.mageri.misc.QualityDefaults;
import org.jdom.Element;

public final class ConsensusAlignerParameters implements ParameterSet {
    private final int k, matchReward, mismatchPenalty, gapOpenPenalty, gapExtendPenalty;
    private final double minIdentityRatio, minAlignedQueryRelativeSpan;
    private final byte muationCqsThreshold;

    public static ConsensusAlignerParameters DEFAULT = new ConsensusAlignerParameters(11,
            1, -3, -6, -1,
            0.9, 0.7, QualityDefaults.PH33_GOOD_QUAL);

    public ConsensusAlignerParameters(int k,
                                      int matchReward, int mismatchPenalty, int gapOpenPenalty, int gapExtendPenalty,
                                      double minIdentityRatio, double minAlignedQueryRelativeSpan,
                                      byte muationCqsThreshold) {
        this.k = k;
        this.matchReward = matchReward;
        this.mismatchPenalty = mismatchPenalty;
        this.gapOpenPenalty = gapOpenPenalty;
        this.gapExtendPenalty = gapExtendPenalty;
        this.minIdentityRatio = minIdentityRatio;
        this.minAlignedQueryRelativeSpan = minAlignedQueryRelativeSpan;
        this.muationCqsThreshold = muationCqsThreshold;
    }

    public int getK() {
        return k;
    }

    public int getMatchReward() {
        return matchReward;
    }

    public int getMismatchPenalty() {
        return mismatchPenalty;
    }

    public int getGapOpenPenalty() {
        return gapOpenPenalty;
    }

    public int getGapExtendPenalty() {
        return gapExtendPenalty;
    }

    public double getMinIdentityRatio() {
        return minIdentityRatio;
    }

    public double getMinAlignedQueryRelativeSpan() {
        return minAlignedQueryRelativeSpan;
    }

    public byte getMuationCqsThreshold() {
        return muationCqsThreshold;
    }

    public ConsensusAlignerParameters withK(int k) {
        return new ConsensusAlignerParameters(k, matchReward, mismatchPenalty, gapOpenPenalty, gapExtendPenalty,
                minIdentityRatio, minAlignedQueryRelativeSpan,
                muationCqsThreshold);
    }

    public ConsensusAlignerParameters withMatchRewards(int matchRewards) {
        return new ConsensusAlignerParameters(k, matchRewards, mismatchPenalty, gapOpenPenalty, gapExtendPenalty,
                minIdentityRatio, minAlignedQueryRelativeSpan,
                muationCqsThreshold);
    }

    public ConsensusAlignerParameters withMismatchPenalty(int mismatchPenalty) {
        return new ConsensusAlignerParameters(k, matchReward, mismatchPenalty, gapOpenPenalty, gapExtendPenalty,
                minIdentityRatio, minAlignedQueryRelativeSpan,
                muationCqsThreshold);
    }

    public ConsensusAlignerParameters withGapOpenPenalty(int gapOpenPenalty) {
        return new ConsensusAlignerParameters(k, matchReward, mismatchPenalty, gapOpenPenalty, gapExtendPenalty,
                minIdentityRatio, minAlignedQueryRelativeSpan,
                muationCqsThreshold);
    }

    public ConsensusAlignerParameters withGapExtendPenalty(int gapExtendPenalty) {
        return new ConsensusAlignerParameters(k, matchReward, mismatchPenalty, gapOpenPenalty, gapExtendPenalty,
                minIdentityRatio, minAlignedQueryRelativeSpan,
                muationCqsThreshold);
    }

    public ConsensusAlignerParameters withMinIdentityRatio(double minIdentityRatio) {
        return new ConsensusAlignerParameters(k, matchReward, mismatchPenalty, gapOpenPenalty, gapExtendPenalty,
                minIdentityRatio, minAlignedQueryRelativeSpan,
                muationCqsThreshold);
    }

    public ConsensusAlignerParameters withMinAlignedQueryRelativeSpan(double minAlignedQueryRelativeSpan) {
        return new ConsensusAlignerParameters(k, matchReward, mismatchPenalty, gapOpenPenalty, gapExtendPenalty,
                minIdentityRatio, minAlignedQueryRelativeSpan,
                muationCqsThreshold);
    }

    public ConsensusAlignerParameters withMutationCqsThreshold(byte muationCqsThreshold) {
        return new ConsensusAlignerParameters(k, matchReward, mismatchPenalty, gapOpenPenalty, gapExtendPenalty,
                minIdentityRatio, minAlignedQueryRelativeSpan,
                muationCqsThreshold);
    }

    @Override
    public Element toXml() {
        Element e = new Element("ConsensusAlignerParameters");
        e.addContent(new Element("k").setText(Integer.toString(k)));
        e.addContent(new Element("matchReward").setText(Integer.toString(matchReward)));
        e.addContent(new Element("mismatchPenalty").setText(Integer.toString(mismatchPenalty)));
        e.addContent(new Element("gapOpenPenalty").setText(Integer.toString(gapOpenPenalty)));
        e.addContent(new Element("gapExtendPenalty").setText(Integer.toString(gapExtendPenalty)));
        e.addContent(new Element("minIdentityRatio").setText(Double.toString(minIdentityRatio)));
        e.addContent(new Element("minAlignedQueryRelativeSpan").setText(Double.toString(minAlignedQueryRelativeSpan)));
        e.addContent(new Element("muationCqsThreshold").setText(Byte.toString(muationCqsThreshold)));
        return e;
    }

    public static ConsensusAlignerParameters fromXml(Element parent) {
        Element e = parent.getChild("ConsensusAlignerParameters");
        return new ConsensusAlignerParameters(
                Byte.parseByte(e.getChildTextTrim("k")),
                Integer.parseInt(e.getChildTextTrim("matchReward")),
                Integer.parseInt(e.getChildTextTrim("mismatchPenalty")),
                Integer.parseInt(e.getChildTextTrim("gapOpenPenalty")),
                Integer.parseInt(e.getChildTextTrim("gapExtendPenalty")),
                Double.parseDouble(e.getChildTextTrim("minIdentityRatio")),
                Double.parseDouble(e.getChildTextTrim("minAlignedQueryRelativeSpan")),
                Byte.parseByte(e.getChildTextTrim("muationCqsThreshold"))
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConsensusAlignerParameters that = (ConsensusAlignerParameters) o;

        if (gapExtendPenalty != that.gapExtendPenalty) return false;
        if (gapOpenPenalty != that.gapOpenPenalty) return false;
        if (k != that.k) return false;
        if (matchReward != that.matchReward) return false;
        if (Double.compare(that.minAlignedQueryRelativeSpan, minAlignedQueryRelativeSpan) != 0) return false;
        if (Double.compare(that.minIdentityRatio, minIdentityRatio) != 0) return false;
        if (mismatchPenalty != that.mismatchPenalty) return false;
        if (muationCqsThreshold != that.muationCqsThreshold) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = k;
        result = 31 * result + matchReward;
        result = 31 * result + mismatchPenalty;
        result = 31 * result + gapOpenPenalty;
        result = 31 * result + gapExtendPenalty;
        temp = Double.doubleToLongBits(minIdentityRatio);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minAlignedQueryRelativeSpan);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) muationCqsThreshold;
        return result;
    }
}
