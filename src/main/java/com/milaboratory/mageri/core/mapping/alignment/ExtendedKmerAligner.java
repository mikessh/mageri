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

import com.milaboratory.core.sequence.alignment.LocalAligner;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.mageri.core.genomic.Reference;
import com.milaboratory.mageri.core.genomic.ReferenceLibrary;
import com.milaboratory.mageri.core.mapping.ConsensusAlignerParameters;
import com.milaboratory.mageri.core.mapping.kmer.KMerFinder;
import com.milaboratory.mageri.core.mapping.kmer.KMerFinderResult;

public class ExtendedKmerAligner implements Aligner {
    private final AlignmentScoring alignmentScoring;
    private final KMerFinder kMerFinder;
    private final LocalAlignmentEvaluator localAlignmentEvaluator;

    public ExtendedKmerAligner(ReferenceLibrary referenceLibrary) {
        this(referenceLibrary, ConsensusAlignerParameters.DEFAULT);
    }

    public ExtendedKmerAligner(ReferenceLibrary referenceLibrary, ConsensusAlignerParameters alignerParameters) {
        this(new KMerFinder(referenceLibrary, alignerParameters), alignerParameters);
    }

    public ExtendedKmerAligner(KMerFinder kMerFinder, ConsensusAlignerParameters alignerParameters) {
        this.kMerFinder = kMerFinder;
        this.alignmentScoring = new AlignmentScoring(alignerParameters);
        this.localAlignmentEvaluator = new LocalAlignmentEvaluator(alignerParameters);
    }

    @Override
    public AlignmentResult align(NucleotideSequence sequence) {
        KMerFinderResult result = kMerFinder.find(sequence);

        if (result == null) {
            // No primary hit
            return null;
        }

        Reference reference = result.getHit();

        boolean rc = result.isReverseComplement();

        if (rc) {
            // account for RC hits
            sequence = sequence.getReverseComplement();
        }

        LocalAlignment alignment = LocalAligner.align(alignmentScoring.asInternalScoring(),
                reference.getSequence(), sequence);

        if (alignment == null) {
            // No local alignment
            return null;
        }

        boolean good = localAlignmentEvaluator.isGood(alignment, reference.getSequence(), sequence);

        return new AlignmentResult(sequence, reference, alignment, rc, result.getScore(), good);
    }

    @Override
    public ReferenceLibrary getReferenceLibrary() {
        return kMerFinder.getReferenceLibrary();
    }
}
