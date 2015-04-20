package com.milaboratory.oncomigec.core.mapping;

import com.milaboratory.oncomigec.misc.ParameterSet;
import com.milaboratory.oncomigec.misc.Util;
import org.jdom.Element;

public final class ConsensusAlignerParameters implements ParameterSet {
    private final byte consensusQualityThreshold;

    public static ConsensusAlignerParameters DEFAULT = new ConsensusAlignerParameters(Util.PH33_GOOD_QUAL),
            NO_FILTER = new ConsensusAlignerParameters((byte) 0);

    public ConsensusAlignerParameters(byte consensusQualityThreshold) {
        this.consensusQualityThreshold = consensusQualityThreshold;
    }

    public byte getConsensusQualityThreshold() {
        return consensusQualityThreshold;
    }

    @Override
    public Element toXml() {
        Element e = new Element("ConsensusAlignerParameters");
        e.addContent(new Element("consensusQualityThreshold").setText(Byte.toString(consensusQualityThreshold)));
        return e;
    }

    public static ConsensusAlignerParameters fromXml(Element parent) {
        Element e = parent.getChild("ConsensusAlignerParameters");
        return new ConsensusAlignerParameters(
                Byte.parseByte(e.getChildTextTrim("consensusQualityThreshold"))
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConsensusAlignerParameters that = (ConsensusAlignerParameters) o;

        if (consensusQualityThreshold != that.consensusQualityThreshold) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) consensusQualityThreshold;
    }
}
