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

import java.io.Serializable;
import java.util.Set;

public final class MutationFilter implements Serializable{
    private final boolean[][] substitutionMask;
    private final boolean[] referenceMask, qualityMask, coverageMask;
    private final boolean good;
    private final int mustHaveMutationsCount;
    private final Set<Integer> indels;

    public MutationFilter(boolean[][] substitutionMask, boolean[] referenceMask,
                          boolean[] qualityMask, boolean[] coverageMask, Set<Integer> indels,
                          boolean good, int mustHaveMutationsCount) {
        this.substitutionMask = substitutionMask;
        this.referenceMask = referenceMask;
        this.coverageMask = coverageMask;
        this.qualityMask = qualityMask;
        this.indels = indels;
        this.good = good;
        this.mustHaveMutationsCount = mustHaveMutationsCount;
    }

    public boolean hasSubstitution(int position, int ntCode) {
        return passedFilter(position) &&
                substitutionMask[position][ntCode];
    }

    public boolean hasIndel(int indel) {
        return indels.contains(indel);
    }

    public boolean hasReference(int position) {
        return referenceMask[position];
    }

    public boolean passedFilter(int position) {
        return coverageMask[position] && qualityMask[position];
    }

    public boolean good() {
        return good;
    }

    public int getMustHaveMutationsCount() {
        return mustHaveMutationsCount;
    }
}
