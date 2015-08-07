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

import com.milaboratory.mageri.core.genomic.Reference;
import com.milaboratory.mageri.core.genomic.ReferenceLibrary;
import com.milaboratory.mageri.generators.MigWithMutations;

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
        for (int major : MigWithMutations.getMajorMutations()) {
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