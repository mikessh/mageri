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

import com.milaboratory.core.sequence.Range;
import com.milaboratory.core.sequence.alignment.*;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.align.entity.PAlignmentResult;
import com.milaboratory.oncomigec.core.align.entity.SAlignmentResult;
import com.milaboratory.oncomigec.core.align.processor.Aligner;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;

import java.util.ArrayList;
import java.util.List;

public class SimpleExomeAligner implements Aligner {
    public final static KAlignerParameters DEFAULT_PARAMS = KAlignerParameters.getByName("strict");

    private final KAligner aligner;
    private final ReferenceLibrary referenceLibrary;
    private final LocalAlignmentEvaluator localAlignmentEvaluator;

    public SimpleExomeAligner(ReferenceLibrary referenceLibrary) {
        this(referenceLibrary, DEFAULT_PARAMS, LocalAlignmentEvaluator.STRICT);
    }

    public SimpleExomeAligner(ReferenceLibrary referenceLibrary,
                              KAlignerParameters parameters,
                              LocalAlignmentEvaluator localAlignmentEvaluator) {
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

        if (hit == null || !localAlignmentEvaluator.isGood(hit.getAlignment(), sequence))
            return null;

        List<LocalAlignment> alignmentBlocks = new ArrayList<>();
        alignmentBlocks.add(hit.getAlignment());

        List<Reference> referenceIds = new ArrayList<>();
        Reference reference = referenceLibrary.getReferences().get(hit.getId());
        referenceIds.add(reference);

        List<Range> rangeList = new ArrayList<>();
        rangeList.add(new Range(0, reference.getSequence().size())); // unused here

        return new SAlignmentResult(alignmentBlocks, referenceIds, rangeList);
    }

    @Override
    public PAlignmentResult align(NucleotideSequence sequence1, NucleotideSequence sequence2) {
        SAlignmentResult result1 = align(sequence1), result2 = align(sequence2);

        if (result1 == null || result2 == null ||
                !result1.getReferences().get(0).equals(result2.getReferences().get(0))) // chimeras not allowed here
            return null;

        if (result1.getAlignments().get(0).getSequence1Range().intersectsWith(
                result2.getAlignments().get(0).getSequence1Range()
        ))
            return null; // by convention reads cannot overlap, this also fixes issues from indels at read junction

        return new PAlignmentResult(result1, result2);
    }

    @Override
    public ReferenceLibrary getReferenceLibrary() {
        return referenceLibrary;
    }
}
