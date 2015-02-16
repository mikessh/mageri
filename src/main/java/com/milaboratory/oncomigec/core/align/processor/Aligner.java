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
package com.milaboratory.oncomigec.core.align.processor;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.align.entity.PAlignmentResult;
import com.milaboratory.oncomigec.core.align.entity.SAlignmentResult;
import com.milaboratory.oncomigec.core.align.reference.ReferenceLibrary;

public interface Aligner {
    /**
     * This thread-safe method should perform an alignment and return a set of LocalAlignment blocks.
     * This could be either blast-like (KAligner), hybrid blast-like (VJAligner) or BWA-based.
     * Blocked structure is useful when aligning to gene fusions or immune receptors, e.g. two blocks for V-
     * and J- segments for B-cell receptor.
     * In case of an unknown random insert (e.g. ribosomal data or CDR3 region in immune receptors) aligner should
     * update the reference pool with newly discovered CDR3 as it is in the consensus. Only errors from reads within MIG
     * (and not consensus ones) will be therefore recorded for this de-novo obtained reference.
     * The de-novo reference list will then be scanned to discard erroneous references.
     *
     * @param sequence nucleotide sequence to align
     * @return alignment result (blocks of local alignments)
     */
    public SAlignmentResult align(NucleotideSequence sequence);

    public PAlignmentResult align(NucleotideSequence sequence1, NucleotideSequence sequence2);

    /**
     * Gets reference library
     *
     * @return returns a map of references built in the aligner with corresponding IDs
     */
    public ReferenceLibrary getReferenceLibrary();
}
