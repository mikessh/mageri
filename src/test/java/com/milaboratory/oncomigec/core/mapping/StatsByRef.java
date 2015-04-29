/*
 * Copyright 2013-2015 Mikhail Shugay (mikhail.shugay@gmail.com)
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
 *
 * Last modified on 30.4.2015 by mikesh
 */

package com.milaboratory.oncomigec.core.mapping;

import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.generators.MigWithMutations;

import java.util.HashMap;
import java.util.Map;

import static com.milaboratory.core.sequence.mutations.Mutations.*;

public class StatsByRef {
    private final Map<Reference, Map<Integer, Integer>> majorCountsByRef = new HashMap<>(),
            minorCountsByRef = new HashMap<>();
    private final ReferenceLibrary referenceLibrary;

    public StatsByRef(ReferenceLibrary referenceLibrary) {
        this.referenceLibrary = referenceLibrary;
        for (Reference reference : referenceLibrary.getReferences()) {
            majorCountsByRef.put(reference, new HashMap<Integer, Integer>());
            minorCountsByRef.put(reference, new HashMap<Integer, Integer>());
        }
    }

    public void update(MigWithMutations MigWithMutations,
                       Reference reference, int offset) {
        final Map<Integer, Integer> majorCounts = majorCountsByRef.get(reference),
                minorCounts = minorCountsByRef.get(reference);

        Integer count;
        for (int major : MigWithMutations.getPcrMutations()) {
            major = move(major, offset);
            majorCounts.put(major,
                    ((count = majorCounts.get(major)) == null ? 0 : count) + 1);
        }

        for (int minor : MigWithMutations.getMinorMutationCounts().keySet()) {
            minor = move(minor, offset);
            minorCounts.put(minor,
                    ((count = minorCounts.get(minor)) == null ? 0 : count) + 1);
        }
    }

    public int[][] getMajorCounts(Reference reference) {
        int[][] majorMatrix = new int[reference.getSequence().size()][4];

        for (Map.Entry<Integer, Integer> majorEntry : majorCountsByRef.get(reference).entrySet()) {
            int code = majorEntry.getKey(), count = majorEntry.getValue(), pos = getPosition(code);
            if (isSubstitution(code) && pos >= 0 && pos < reference.getSequence().size()) {
                majorMatrix[pos][getTo(code)] += count;
            }
        }

        return majorMatrix;
    }

    public int[][] getMinorCounts(Reference reference) {
        int[][] minorMatrix = new int[reference.getSequence().size()][4];

        for (Map.Entry<Integer, Integer> minorEntry : minorCountsByRef.get(reference).entrySet()) {
            int code = minorEntry.getKey(), count = minorEntry.getValue(), pos = getPosition(code);
            if (isSubstitution(code) && pos >= 0 && pos < reference.getSequence().size()) {
                minorMatrix[pos][getTo(code)] += count;
            }
        }

        return minorMatrix;
    }

    public ReferenceLibrary getReferenceLibrary() {
        return referenceLibrary;
    }
}