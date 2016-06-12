/*
 * Copyright 2014-2016 Mikhail Shugay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.antigenomics.mageri.core.mapping.alignment;

import com.antigenomics.mageri.core.genomic.Reference;
import com.antigenomics.mageri.core.genomic.ReferenceLibrary;
import com.antigenomics.mageri.core.mapping.ConsensusAlignerParameters;
import com.antigenomics.mageri.core.mapping.kmer.KMerFinder;
import com.antigenomics.mageri.core.mapping.kmer.KMerFinderResult;
import com.milaboratory.core.sequence.alignment.LocalAligner;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;

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
