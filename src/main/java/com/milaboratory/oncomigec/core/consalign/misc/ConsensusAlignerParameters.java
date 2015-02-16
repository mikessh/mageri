package com.milaboratory.oncomigec.core.consalign.misc;

import com.milaboratory.oncomigec.util.ParameterSet;
import org.jdom.Element;

public final class ConsensusAlignerParameters implements ParameterSet {
    private final byte consensusQualityThreshold;
    private final byte readQualityThreshold;
    private final boolean backAlignDroppedReads;

    public static ConsensusAlignerParameters DEFAULT = new ConsensusAlignerParameters((byte) 25, (byte) 20, true),
            NO_FILTER = new ConsensusAlignerParameters((byte) 0, (byte) 0, true);

    public ConsensusAlignerParameters(byte consensusQualityThreshold, byte readQualityThreshold,
                                      boolean backAlignDroppedReads) {
        this.consensusQualityThreshold = consensusQualityThreshold;
        this.readQualityThreshold = readQualityThreshold;
        this.backAlignDroppedReads = backAlignDroppedReads;
    }

    public byte getConsensusQualityThreshold() {
        return consensusQualityThreshold;
    }

    public byte getReadQualityThreshold() {
        return readQualityThreshold;
    }

    public boolean backAlignDroppedReads() {
        return backAlignDroppedReads;
    }


    @Override
    public Element toXml() {
        Element e = new Element("ConsensusAlignerParameters");
        e.addContent(new Element("consensusQualityThreshold").setText(Byte.toString(consensusQualityThreshold)));
        e.addContent(new Element("readQualityThreshold").setText(Byte.toString(readQualityThreshold)));
        e.addContent(new Element("backAlignDroppedReads").setText(Boolean.toString(backAlignDroppedReads)));
        return e;
    }

    public static ConsensusAlignerParameters fromXml(Element parent) {
        Element e = parent.getChild("ConsensusAlignerParameters");
        return new ConsensusAlignerParameters(
                Byte.parseByte(e.getChildTextTrim("consensusQualityThreshold")),
                Byte.parseByte(e.getChildTextTrim("readQualityThreshold")),
                Boolean.parseBoolean(e.getChildTextTrim("backAlignDroppedReads"))
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConsensusAlignerParameters that = (ConsensusAlignerParameters) o;

        if (backAlignDroppedReads != that.backAlignDroppedReads) return false;
        if (consensusQualityThreshold != that.consensusQualityThreshold) return false;
        if (readQualityThreshold != that.readQualityThreshold) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) consensusQualityThreshold;
        result = 31 * result + (int) readQualityThreshold;
        result = 31 * result + (backAlignDroppedReads ? 1 : 0);
        return result;
    }
}
