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
package com.milaboratory.oncomigec.core.correct;

import com.milaboratory.core.sequence.Range;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.mutations.MigecMutationsCollection;
import com.milaboratory.oncomigec.core.mutations.wrappers.MutationWrapperCollection;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public final class CorrectedConsensus implements Serializable {
    private final Reference reference;
    private final int[] mutations;
    private final Set<Integer> coverageMask;
    private final int migSize;
    private final double maxPValue;
    private final List<Range> ranges;

    public CorrectedConsensus(Reference reference,
                              int[] mutations,
                              Set<Integer> coverageMask,
                              double maxPValue,
                              int migSize,
                              List<Range> ranges) {
        this.reference = reference;
        this.coverageMask = coverageMask;
        this.mutations = mutations;
        this.migSize = migSize;
        this.maxPValue = maxPValue;
        this.ranges = ranges;
    }

    public double getWorstPValue() {
        return maxPValue;
    }

    public Reference getReference() {
        return reference;
    }

    public List<Range> getRanges() {
        return ranges;
    }

    public int[] getMutations() {
        return mutations;
    }

    public Set<Integer> getCoverageMask() {
        return coverageMask;
    }

    public double getMaxPValue() {
        return maxPValue;
    }

    public int getMigSize() {
        return migSize;
    }
}
