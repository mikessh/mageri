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

import com.milaboratory.core.sequencing.read.PSequencingRead;
import com.milaboratory.core.sequencing.read.PSequencingReadImpl;

public final class PConsensus extends Consensus<PSequencingRead> {
    private final SConsensus consensus1, consensus2;

    public PConsensus(SConsensus consensus1, SConsensus consensus2) {
        super(consensus1.getSample(), consensus1.getUmi());
        this.consensus1 = consensus1;
        this.consensus2 = consensus2;
    }

    public SConsensus getConsensus1() {
        return consensus1;
    }

    public SConsensus getConsensus2() {
        return consensus2;
    }

    @Override
    public int getTrueSize() {
        return consensus1.getTrueSize();
    }

    @Override
    public int getAssembledSize() {
        return Math.min(consensus1.getAssembledSize(),
                consensus2.getAssembledSize());
    }

    @Override
    public PSequencingRead asRead() {
        return new PSequencingReadImpl(consensus1.asRead(),
                consensus2.asRead());
    }

    @Override
    public boolean isPairedEnd() {
        return true;
    }

    @Override
    public String toString() {
        return consensus1.toString() + "\t" +
                consensus2.getConsensusSQPair().getSequence() + "\t" + consensus2.getConsensusSQPair().getQuality();
    }
}
