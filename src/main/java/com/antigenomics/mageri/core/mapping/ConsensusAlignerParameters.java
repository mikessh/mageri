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

package com.antigenomics.mageri.core.mapping;

import com.antigenomics.mageri.misc.ParameterSet;
import com.antigenomics.mageri.misc.QualityDefaults;
import org.jdom.Element;

public final class ConsensusAlignerParameters implements ParameterSet {
    private final int k, matchReward, mismatchPenalty, gapOpenPenalty, gapExtendPenalty;
    private final double minIdentityRatio, minAlignedQueryRelativeSpan;
    private final byte muationCqsThreshold;
    private final boolean useSpacedKmers;

    public static ConsensusAlignerParameters DEFAULT = new ConsensusAlignerParameters(11,
            1, -3, -6, -1,
            0.9, 0.7, QualityDefaults.PH33_GOOD_QUAL, true);

    public ConsensusAlignerParameters(int k,
                                      int matchReward, int mismatchPenalty, int gapOpenPenalty, int gapExtendPenalty,
                                      double minIdentityRatio, double minAlignedQueryRelativeSpan,
                                      byte muationCqsThreshold, boolean useSpacedKmers) {
        this.k = k;
        this.matchReward = matchReward;
        this.mismatchPenalty = mismatchPenalty;
        this.gapOpenPenalty = gapOpenPenalty;
        this.gapExtendPenalty = gapExtendPenalty;
        this.minIdentityRatio = minIdentityRatio;
        this.minAlignedQueryRelativeSpan = minAlignedQueryRelativeSpan;
        this.muationCqsThreshold = muationCqsThreshold;
        this.useSpacedKmers = useSpacedKmers;
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

    public boolean getUseSpacedKmers() {
        return useSpacedKmers;
    }

    public ConsensusAlignerParameters withK(int k) {
        return new ConsensusAlignerParameters(k, matchReward, mismatchPenalty, gapOpenPenalty, gapExtendPenalty,
                minIdentityRatio, minAlignedQueryRelativeSpan,
                muationCqsThreshold, useSpacedKmers);
    }

    public ConsensusAlignerParameters withMatchRewards(int matchRewards) {
        return new ConsensusAlignerParameters(k, matchRewards, mismatchPenalty, gapOpenPenalty, gapExtendPenalty,
                minIdentityRatio, minAlignedQueryRelativeSpan,
                muationCqsThreshold, useSpacedKmers);
    }

    public ConsensusAlignerParameters withMismatchPenalty(int mismatchPenalty) {
        return new ConsensusAlignerParameters(k, matchReward, mismatchPenalty, gapOpenPenalty, gapExtendPenalty,
                minIdentityRatio, minAlignedQueryRelativeSpan,
                muationCqsThreshold, useSpacedKmers);
    }

    public ConsensusAlignerParameters withGapOpenPenalty(int gapOpenPenalty) {
        return new ConsensusAlignerParameters(k, matchReward, mismatchPenalty, gapOpenPenalty, gapExtendPenalty,
                minIdentityRatio, minAlignedQueryRelativeSpan,
                muationCqsThreshold, useSpacedKmers);
    }

    public ConsensusAlignerParameters withGapExtendPenalty(int gapExtendPenalty) {
        return new ConsensusAlignerParameters(k, matchReward, mismatchPenalty, gapOpenPenalty, gapExtendPenalty,
                minIdentityRatio, minAlignedQueryRelativeSpan,
                muationCqsThreshold, useSpacedKmers);
    }

    public ConsensusAlignerParameters withMinIdentityRatio(double minIdentityRatio) {
        return new ConsensusAlignerParameters(k, matchReward, mismatchPenalty, gapOpenPenalty, gapExtendPenalty,
                minIdentityRatio, minAlignedQueryRelativeSpan,
                muationCqsThreshold, useSpacedKmers);
    }

    public ConsensusAlignerParameters withMinAlignedQueryRelativeSpan(double minAlignedQueryRelativeSpan) {
        return new ConsensusAlignerParameters(k, matchReward, mismatchPenalty, gapOpenPenalty, gapExtendPenalty,
                minIdentityRatio, minAlignedQueryRelativeSpan,
                muationCqsThreshold, useSpacedKmers);
    }

    public ConsensusAlignerParameters withMutationCqsThreshold(byte muationCqsThreshold) {
        return new ConsensusAlignerParameters(k, matchReward, mismatchPenalty, gapOpenPenalty, gapExtendPenalty,
                minIdentityRatio, minAlignedQueryRelativeSpan,
                muationCqsThreshold, useSpacedKmers);
    }

    public ConsensusAlignerParameters withUseSpacedKmers(boolean useSpacedKmers) {
        return new ConsensusAlignerParameters(k, matchReward, mismatchPenalty, gapOpenPenalty, gapExtendPenalty,
                minIdentityRatio, minAlignedQueryRelativeSpan,
                muationCqsThreshold, useSpacedKmers);
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
        e.addContent(new Element("useSpacedKmers").setText(Boolean.toString(useSpacedKmers)));
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
                Byte.parseByte(e.getChildTextTrim("muationCqsThreshold")),
                Boolean.parseBoolean(e.getChildTextTrim("useSpacedKmers"))
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConsensusAlignerParameters that = (ConsensusAlignerParameters) o;

        if (k != that.k) return false;
        if (matchReward != that.matchReward) return false;
        if (mismatchPenalty != that.mismatchPenalty) return false;
        if (gapOpenPenalty != that.gapOpenPenalty) return false;
        if (gapExtendPenalty != that.gapExtendPenalty) return false;
        if (Double.compare(that.minIdentityRatio, minIdentityRatio) != 0) return false;
        if (Double.compare(that.minAlignedQueryRelativeSpan, minAlignedQueryRelativeSpan) != 0) return false;
        if (muationCqsThreshold != that.muationCqsThreshold) return false;
        return useSpacedKmers == that.useSpacedKmers;

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
        result = 31 * result + (useSpacedKmers ? 1 : 0);
        return result;
    }
}
