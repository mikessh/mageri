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
package com.milaboratory.oncomigec.core.align.entity;

import com.milaboratory.core.sequence.Range;
import com.milaboratory.oncomigec.core.genomic.Reference;

import java.util.List;

public class PAlignmentResult {
    private final List<Reference> references;
    private final List<Range> ranges;
    private final SAlignmentResult result1, result2;

    public PAlignmentResult(SAlignmentResult result1, SAlignmentResult result2) {
        this.result1 = result1;
        this.result2 = result2;

        // Here we combine reference ranges from paired-end sequencing
        // We overlap all ranges for references present both in read1 and read2
        // Note that ranges are only meaningful for chimeric alignments
        references = result1.getReferences();
        ranges = result1.getRanges();

        for (int i = 0; i < result2.getReferences().size(); i++) {
            Reference reference2 = result2.getReferences().get(i);
            Range range2 = result2.getRanges().get(i);

            boolean exists = false;
            for (int j = 0; j < references.size(); j++) {
                if (references.get(j) == reference2) {
                    Range range1 = ranges.get(j);
                    ranges.set(j, new Range(Math.min(range1.getFrom(), range2.getFrom()),
                            Math.max(range1.getTo(), range2.getTo())));
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                references.add(reference2);
                ranges.add(range2);
            }
        }
    }

    public List<Reference> getReferences() {
        return references;
    }

    public List<Range> getRanges() {
        return ranges;
    }

    public SAlignmentResult getResult1() {
        return result1;
    }

    public SAlignmentResult getResult2() {
        return result2;
    }
}
