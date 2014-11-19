/**
 * Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.milaboratory.migec2.datasim;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.migec2.core.align.reference.Reference;
import com.milaboratory.migec2.core.align.reference.ReferenceLibrary;

import java.util.*;

public class MigGeneratorHistory {
    private final Map<NucleotideSequence, MigGeneratorHistoryEntry> historyBySequence = new HashMap<>();

    public MigGeneratorHistory(Collection<NucleotideSequence> references) {
        for (NucleotideSequence reference : references) {
            historyBySequence.put(reference, new MigGeneratorHistoryEntry(reference));
        }
    }

    public MigGeneratorHistory(ReferenceLibrary referenceLibrary) {
        for (Reference reference : referenceLibrary.getReferences()) {
            historyBySequence.put(reference.getSequence(), new MigGeneratorHistoryEntry(reference.getSequence()));
        }
    }

    public void append(NucleotideSequence reference, NucleotideSequence variant) {
        historyBySequence.get(reference).append(variant);
    }

    public MigGeneratorHistoryEntry getEntry(NucleotideSequence sequence) {
        return historyBySequence.get(sequence);
    }

    public List<MigGeneratorHistoryEntry> sortedEntries() {
        List<MigGeneratorHistoryEntry> entryList = new ArrayList<>();

        for (MigGeneratorHistoryEntry entry : historyBySequence.values()) {
            if (entry.getFrequency() > 0)
                entryList.add(entry);
        }

        Collections.sort(entryList, new Comparator<MigGeneratorHistoryEntry>() {
            @Override
            public int compare(MigGeneratorHistoryEntry o1, MigGeneratorHistoryEntry o2) {
                return -Integer.compare(o1.getFrequency(), o2.getFrequency());
            }
        });

        return entryList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(">MIG generator history");

        for (MigGeneratorHistoryEntry entry : sortedEntries()) {
            sb.append("\n").append(entry.toString());
        }

        return sb.toString();
    }
}
