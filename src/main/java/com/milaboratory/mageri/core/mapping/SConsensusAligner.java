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

package com.milaboratory.mageri.core.mapping;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.mageri.core.assemble.SConsensus;
import com.milaboratory.mageri.core.genomic.ReferenceLibrary;
import com.milaboratory.mageri.core.mapping.alignment.Aligner;
import com.milaboratory.mageri.core.mapping.alignment.AlignmentResult;
import com.milaboratory.mageri.core.mapping.alignment.ExtendedKmerAligner;
import com.milaboratory.mageri.core.mutations.MutationArray;

public final class SConsensusAligner extends ConsensusAligner<SConsensus, SAlignedConsensus> {

    public SConsensusAligner(Aligner aligner, ConsensusAlignerParameters parameters) {
        super(aligner, parameters);
    }

    public SConsensusAligner(Aligner aligner) {
        super(aligner, ConsensusAlignerParameters.DEFAULT);
    }

    public SConsensusAligner(ReferenceLibrary referenceLibrary) {
        super(new ExtendedKmerAligner(referenceLibrary), ConsensusAlignerParameters.DEFAULT);
    }

    @Override
    public SAlignedConsensus align(SConsensus consensus) {
        NucleotideSQPair consensusSQPair = consensus.getConsensusSQPair();
        AlignmentResult alignmentResult = aligner.align(consensusSQPair.getSequence());

        MutationArray mutations = alignmentResult != null ? extractMutations(alignmentResult, consensus) : null;

        return new SAlignedConsensus(consensus.getSample(), consensus.getUmi(),
                consensusSQPair, alignmentResult, mutations);
    }

    @Override
    public boolean isPairedEnd() {
        return false;
    }
}
