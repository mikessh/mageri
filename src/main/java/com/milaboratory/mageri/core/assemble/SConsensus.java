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
package com.milaboratory.mageri.core.assemble;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.core.sequencing.read.SSequencingReadImpl;
import com.milaboratory.mageri.pipeline.analysis.Sample;

import java.util.Collections;
import java.util.Set;

public final class SConsensus extends Consensus<SSequencingRead> {
    private final NucleotideSQPair consensusSQPair;
    private final int assembledSize, trueSize;
    private final Set<Integer> minors;

    public SConsensus(Sample sample,
                      NucleotideSequence umi,
                      NucleotideSQPair consensusSQPair,
                      Set<Integer> minors,
                      int assembledSize, int trueSize) {
        super(sample, umi);
        this.consensusSQPair = consensusSQPair;
        this.minors = minors;
        this.assembledSize = assembledSize;
        this.trueSize = trueSize;
    }

    public Set<Integer> getMinors() {
        return Collections.unmodifiableSet(minors);
    }

    public NucleotideSQPair getConsensusSQPair() {
        return consensusSQPair;
    }

    @Override
    public SSequencingRead asRead() {
        return new SSequencingReadImpl(
                "C:" + sample.getFullName() + " " + umi + ":" + assembledSize + ":" + trueSize,
                consensusSQPair,
                -1);
    }

    public int getAssembledSize() {
        return assembledSize;
    }

    public int getTrueSize() {
        return trueSize;
    }

    @Override
    public boolean isPairedEnd() {
        return false;
    }
    
    @Override
    public String toString() {
        return umi + "\t" + consensusSQPair.getSequence() + "\t" + consensusSQPair.getQuality();
    }
}
