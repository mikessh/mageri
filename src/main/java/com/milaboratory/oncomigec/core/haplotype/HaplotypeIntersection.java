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
 * Last modified on 18.3.2015 by mikesh
 */

package com.milaboratory.oncomigec.core.haplotype;

import com.milaboratory.core.sequence.Range;
import com.milaboratory.oncomigec.core.mutations.MutationDifference;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class HaplotypeIntersection {
    private final Haplotype haplotype1, haplotype2;
    private boolean embeddedOrTouching, intersects, matches = true;
    private boolean fast;
    private List<Range> intersections = new LinkedList<>();
    private List<MutationDifference> mutationDifferences;

    public HaplotypeIntersection(Haplotype haplotype1, Haplotype haplotype2, boolean fast) {
        this.haplotype1 = haplotype1;
        this.haplotype2 = haplotype2;
        this.fast = fast;
        this.intersects = haplotype1.getSpan().intersectsWithOrTouches(haplotype2.getSpan());

        if (intersects) {
            if (!fast) {
                mutationDifferences = new LinkedList<>();
            }

            outerloop:
            for (Range range : haplotype1.ranges) {
                for (Range otherRange : haplotype2.ranges) {
                    if (range.intersectsWith(otherRange)) {
                        // check if overlapping sequences match
                        // if yes, than they can be pooled with append
                        Range intersection = range.intersection(otherRange);

                        MutationDifference mutationDifference = haplotype1.getMutationDifference(haplotype2, intersection);

                        if (!mutationDifference.isEmpty()) {
                            matches = false;
                            if (fast)
                                break outerloop; // if we are on append run, just leave
                        }

                        intersections.add(intersection);

                        if (!fast) {
                            mutationDifferences.add(mutationDifference);
                        }
                    }
                    // else - embedded or touches
                }
            }
        }

        embeddedOrTouching = intersects && intersections.isEmpty();
    }

    public Haplotype getHaplotype1() {
        return haplotype1;
    }

    public Haplotype getHaplotype2() {
        return haplotype2;
    }

    public boolean embeddedOrTouching() {
        return embeddedOrTouching;
    }

    public boolean intersects() {
        return intersects;
    }

    public boolean matches() {
        return matches;
    }

    public boolean good() {
        return intersects && matches;
    }

    public List<Range> getIntersections() {
        return Collections.unmodifiableList(intersections);
    }

    public List<MutationDifference> getMutationDifferences() {
        if (fast)
            throw new RuntimeException("Not available for unmatched haplotypes.");
        return Collections.unmodifiableList(mutationDifferences);
    }
}
