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

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.oncomigec.core.consalign.mutations.MinorMutationData;
import com.milaboratory.oncomigec.core.consalign.mutations.MutationsAndCoverage;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.core.mutations.MigecMutationsCollection;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public final class AlignerReferenceLibrary implements Serializable {
    private final Map<Reference, MutationsAndCoverage> mutationsAndCoverageByReference =
            new HashMap<>();

    private final ReferenceLibrary referenceLibrary;

    public AlignerReferenceLibrary(ReferenceLibrary referenceLibrary) {
        this.referenceLibrary = referenceLibrary;
        for (Reference reference : referenceLibrary.getReferences())
            mutationsAndCoverageByReference.put(reference, new MutationsAndCoverage(reference));
    }

    public void append(Reference reference, LocalAlignment alignment, NucleotideSQPair consensusSQPair,
                       MigecMutationsCollection majorMutations, MinorMutationData minorMutations,
                       boolean appendReference) {
        MutationsAndCoverage mutationsAndCoverage = mutationsAndCoverageByReference.get(reference);

        mutationsAndCoverage.append(alignment, consensusSQPair.getQuality(),
                majorMutations, minorMutations,
                appendReference);
    }

    public ReferenceLibrary getReferenceLibrary() {
        return referenceLibrary;
    }

    public MutationsAndCoverage getMutationsAndCoverage(Reference reference) {
        return mutationsAndCoverageByReference.get(reference);
    }

    @Override
    public String toString() {
        String formattedString = "#" + super.toString();
        for (Map.Entry<Reference, MutationsAndCoverage> entry : mutationsAndCoverageByReference.entrySet()) {
            if (entry.getValue().wasUpdated()) {
                formattedString += "\n" + entry.getKey().toString();
                formattedString += "\n" + entry.getValue().toString();
            }
        }
        return formattedString + "\n";
    }
}