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

package com.milaboratory.mageri.core.mapping.alignment;

import com.milaboratory.core.sequence.alignment.AffineGapAlignmentScoring;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import com.milaboratory.mageri.core.mapping.ConsensusAlignerParameters;

import static com.milaboratory.core.sequence.alignment.ScoringUtils.getSymmetricMatrix;

public class AlignmentScoring {
    private final int matchReward, mismatchPenalty, gapOpenPenalty, gapExtendPenalty;
    private final AffineGapAlignmentScoring internalScoring;

    public AlignmentScoring() {
        this(ConsensusAlignerParameters.DEFAULT);
    }

    public AlignmentScoring(ConsensusAlignerParameters alignerParameters) {
        this(alignerParameters.getMatchReward(), alignerParameters.getMismatchPenalty(),
                alignerParameters.getGapOpenPenalty(), alignerParameters.getGapExtendPenalty());
    }

    public AlignmentScoring(int matchReward, int mismatchPenalty,
                            int gapOpenPenalty, int gapExtendPenalty) {
        this.matchReward = matchReward;
        this.mismatchPenalty = mismatchPenalty;
        this.gapOpenPenalty = gapOpenPenalty;
        this.gapExtendPenalty = gapExtendPenalty;
        this.internalScoring = new AffineGapAlignmentScoring<>(NucleotideAlphabet.INSTANCE,
                getSymmetricMatrix(matchReward, mismatchPenalty, 4), gapOpenPenalty, gapExtendPenalty);
    }

    public AffineGapAlignmentScoring asInternalScoring() {
        return internalScoring;
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

    public AffineGapAlignmentScoring getInternalScoring() {
        return internalScoring;
    }
}
