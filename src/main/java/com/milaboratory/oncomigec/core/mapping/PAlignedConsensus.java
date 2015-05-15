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

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.mapping.alignment.AlignmentResult;
import com.milaboratory.oncomigec.core.mutations.MutationArray;
import com.milaboratory.oncomigec.pipeline.analysis.Sample;

import java.util.Objects;

public final class PAlignedConsensus extends AlignedConsensus {
    private final MutationArray mutations1, mutations2;
    private final AlignmentResult alignmentResult1, alignmentResult2;
    private final NucleotideSQPair consensusSQPair1, consensusSQPair2;

    public PAlignedConsensus(Sample sample, NucleotideSequence umi,
                             NucleotideSQPair consensusSQPair1, NucleotideSQPair consensusSQPair2,
                             AlignmentResult alignmentResult1, AlignmentResult alignmentResult2,
                             MutationArray mutations1, MutationArray mutations2) {
        super(sample, umi);
        this.consensusSQPair1 = consensusSQPair1;
        this.consensusSQPair2 = consensusSQPair2;
        this.alignmentResult1 = alignmentResult1;
        this.alignmentResult2 = alignmentResult2;
        this.mutations1 = mutations1;
        this.mutations2 = mutations2;
    }

    public MutationArray getMutations1() {
        return mutations1;
    }

    public MutationArray getMutations2() {
        return mutations2;
    }

    public AlignmentResult getAlignmentResult1() {
        return alignmentResult1;
    }

    public AlignmentResult getAlignmentResult2() {
        return alignmentResult2;
    }

    public NucleotideSQPair getConsensusSQPair1() {
        return consensusSQPair1;
    }

    public NucleotideSQPair getConsensusSQPair2() {
        return consensusSQPair2;
    }

    public boolean isMapped1() {
        return alignmentResult1 != null;
    }

    public boolean isMapped2() {
        return alignmentResult2 != null;
    }

    public boolean isAligned1() {
        return isMapped1() && alignmentResult1.isGood();
    }

    public boolean isAligned2() {
        return isMapped2() && alignmentResult2.isGood();
    }

    @Override
    public boolean isMapped() {
        return isMapped1() && isMapped2();
    }

    @Override
    public boolean isAligned() {
        return isAligned1() && isAligned2();
    }

    @Override
    public boolean isChimeric() {
        return isMapped() &&
                !(Objects.equals(alignmentResult1.getReference(), alignmentResult2.getReference()));
    }

    @Override
    public boolean isPairedEnd() {
        return true;
    }
}
