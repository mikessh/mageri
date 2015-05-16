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

package com.milaboratory.oncomigec.core.mapping;

import com.milaboratory.oncomigec.misc.ParameterSet;
import com.milaboratory.oncomigec.misc.QualityDefaults;
import org.jdom.Element;

public final class ConsensusAlignerParameters implements ParameterSet {
    private final byte consensusQualityThreshold;

    public static ConsensusAlignerParameters
            DEFAULT = new ConsensusAlignerParameters(QualityDefaults.PH33_GOOD_QUAL),
            NO_FILTER = new ConsensusAlignerParameters((byte) 0);

    public ConsensusAlignerParameters(byte consensusQualityThreshold) {
        this.consensusQualityThreshold = consensusQualityThreshold;
    }

    public byte getConsensusQualityThreshold() {
        return consensusQualityThreshold;
    }

    public ConsensusAlignerParameters withConsensusQualityThreshold(byte consensusQualityThreshold) {
        return new ConsensusAlignerParameters(consensusQualityThreshold);
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
