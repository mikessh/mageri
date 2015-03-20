/*
 * Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)
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
package com.milaboratory.oncomigec.core.align.processor.aligners;

import com.milaboratory.core.sequence.alignment.KAligner;
import com.milaboratory.core.sequence.alignment.KAlignerParameters;
import com.milaboratory.core.sequence.alignment.KAlignmentHit;
import com.milaboratory.core.sequence.alignment.KAlignmentResult;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.align.entity.SAlignmentResult;
import com.milaboratory.oncomigec.core.align.processor.Aligner;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;

public class SimpleExomeAligner extends Aligner {
    public final static KAlignerParameters DEFAULT_PARAMS = KAlignerParameters.getByName("strict");

    private final KAligner aligner;
    private final ReferenceLibrary referenceLibrary;
    private final LocalAlignmentEvaluator localAlignmentEvaluator;

    public SimpleExomeAligner(ReferenceLibrary referenceLibrary) {
        this(referenceLibrary, DEFAULT_PARAMS, new LocalAlignmentEvaluator());
    }

    public SimpleExomeAligner(ReferenceLibrary referenceLibrary,
                              KAlignerParameters parameters,
                              LocalAlignmentEvaluator localAlignmentEvaluator) {
        super(referenceLibrary);
        
        this.aligner = new KAligner(parameters);

        this.referenceLibrary = referenceLibrary;
        for (Reference reference : referenceLibrary.getReferences())
            aligner.addReference(reference.getSequence());

        this.localAlignmentEvaluator = localAlignmentEvaluator;
    }

    @Override
    public SAlignmentResult align(NucleotideSequence sequence) {
        KAlignmentResult result = aligner.align(sequence);
        result.calculateAllAlignments();

        KAlignmentHit hit = result.getBestHit();

        if (hit == null || !localAlignmentEvaluator.isGood(hit.getAlignment(), hit.getHitSequence(), sequence))
            return null;

        Reference reference = referenceLibrary.getReferences().get(hit.getId());

        return new SAlignmentResult(hit.getAlignment(), reference);
    }
}