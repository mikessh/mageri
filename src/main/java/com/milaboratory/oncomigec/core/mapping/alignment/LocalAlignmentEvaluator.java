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

package com.milaboratory.oncomigec.core.mapping.alignment;

import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;

public class LocalAlignmentEvaluator implements AlignmentEvaluator<LocalAlignment> {
    private int maxConsequentMismatches;
    private double minIdentityRatio, minAlignedQueryRelativeSpan;

    public LocalAlignmentEvaluator() {
        this(5, 0.7, 0.3);
    }

    public LocalAlignmentEvaluator(int maxConsequentMismatches,
                                   double minIdentityRatio,
                                   double minAlignedQueryRelativeSpan) {
        this.maxConsequentMismatches = maxConsequentMismatches;
        this.minIdentityRatio = minIdentityRatio;
        this.minAlignedQueryRelativeSpan = minAlignedQueryRelativeSpan;
    }

    @Override
    public boolean isGood(LocalAlignment alignment, NucleotideSequence reference, NucleotideSequence query) {
        int[] mutations = alignment.getMutations();

        int alignedRefLength = alignment.getSequence1Range().length(),
                alignedQueryLength = alignment.getSequence2Range().length();
        double identity = (alignedRefLength - mutations.length) / (double) alignedRefLength,
                alignedQueryRelativeSpan = alignedQueryLength / (double) query.size();

        if (identity < minIdentityRatio || alignedQueryRelativeSpan < minAlignedQueryRelativeSpan) {
            return false;
        }

        int consequentMutations = 1, previousSubstitutionPos = -2;
        for (int mutation : mutations) {
            int pos = Mutations.getPosition(mutation);
            if (Mutations.isSubstitution(mutation)) {
                if (pos - 1 == previousSubstitutionPos)
                    consequentMutations++;
                else
                    consequentMutations = 1;
                previousSubstitutionPos = pos;
            } else {
                consequentMutations = 1;
            }

            if (consequentMutations > maxConsequentMismatches) {
                return false;
            }
        }

        return true;
    }

    public int getMaxConsequentMismatches() {
        return maxConsequentMismatches;
    }

    public double getMinIdentityRatio() {
        return minIdentityRatio;
    }

    public double getMinAlignedQueryRelativeSpan() {
        return minAlignedQueryRelativeSpan;
    }

    public void setMaxConsequentMismatches(int maxConsequentMismatches) {
        this.maxConsequentMismatches = maxConsequentMismatches;
    }

    public void setMinIdentityRatio(double minIdentityRatio) {
        this.minIdentityRatio = minIdentityRatio;
    }

    public void setMinAlignedQueryRelativeSpan(double minAlignedQueryRelativeSpan) {
        this.minAlignedQueryRelativeSpan = minAlignedQueryRelativeSpan;
    }
}
