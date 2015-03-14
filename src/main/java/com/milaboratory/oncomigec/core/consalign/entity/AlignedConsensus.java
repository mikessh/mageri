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
package com.milaboratory.oncomigec.core.consalign.entity;

import com.milaboratory.core.sequence.Range;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.mutations.MigecMutationsCollection;

import java.util.List;

public final class AlignedConsensus {
    private final int migSize;
    private final List<MigecMutationsCollection> majorMutationsList;
    private final List<Reference> references;
    private final List<Range> ranges;
    private final int numberOfReferences;

    public AlignedConsensus(List<MigecMutationsCollection> majorMutationsList,
                            List<Reference> references, List<Range> ranges, int migSize) {
        this.references = references;
        this.ranges = ranges;
        this.majorMutationsList = majorMutationsList;
        this.numberOfReferences = references.size();
        this.migSize = migSize;
    }

    public int getNumberOfReferences() {
        return numberOfReferences;
    }

    /**
     * @return
     */
    public MigecMutationsCollection getMajorMutations(int referenceIndex) {
        return majorMutationsList.get(referenceIndex);
    }

    /**
     * References to reconstruct consensus sequence after error correction
     * All lists in this class have the same ordering
     *
     * @return List of references
     */
    public Reference getReference(int referenceIndex) {
        return references.get(referenceIndex);
    }

    /**
     * Range list to reconstruct consensus sequence after error correction.
     * For exome data, simply the span of each exon reference.
     * For TCR data this could vary according to CDR3 boundary of V/J genes,
     * same as for ribosomal RNA data.
     * All lists in this class have the same ordering
     *
     * @return List of ranges
     */
    public Range getRange(int referenceIndex) {
        return ranges.get(referenceIndex);
    }

    public int getMigSize() {
        return migSize;
    }
}
