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
package com.milaboratory.migec2.core.align.reference;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.migec2.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ReferenceLibrary {
    // todo get by name, type, etc
    // todo clone for aligner with hypervariable regions
    private final List<Reference> references = new ArrayList<>();
    private final List<Reference> deNovoReferences = new ArrayList<>();

    public ReferenceLibrary(File referenceFile) throws IOException {
        this(Util.readFasta(referenceFile));
    }

    public ReferenceLibrary(Collection<SSequencingRead> fastaRecords) {
        Set<NucleotideSequence> referenceSequences = new HashSet<>();
        int globalId = 0;
        for (SSequencingRead record : fastaRecords) {
            NucleotideSequence sequence = record.getData().getSequence();
            String[] descriptionFields = record.getDescription().split("[ \t]");
            addReference(globalId, descriptionFields[0],
                    descriptionFields.length > 1 ? descriptionFields[1] : "generic",
                    sequence);
            globalId += 2;
            if (referenceSequences.contains(sequence))
                throw new IllegalArgumentException("Duplicate sequences not allowed in reference library.");
            referenceSequences.add(sequence);
        }
    }

    private void addReference(int globalId, String id, String type, NucleotideSequence sequence) {
        references.add(new Reference(globalId, id, type,
                sequence, false));
        references.add(new Reference(++globalId, id, type,
                sequence.getReverseComplement(), true));
    }

    public Reference getByGlobalId(int globalId) {
        return references.get(globalId);
    }

    public List<Reference> getReferences() {
        return Collections.unmodifiableList(references);
    }

    public List<Reference> getDeNovoReferences() {
        return deNovoReferences;
    }
}
