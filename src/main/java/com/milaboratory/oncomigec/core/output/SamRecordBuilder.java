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
 * Last modified on 16.4.2015 by mikesh
 */

package com.milaboratory.oncomigec.core.output;

import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.oncomigec.core.align.sequence.AlignmentResult;
import com.milaboratory.oncomigec.core.mutations.Indel;
import com.milaboratory.oncomigec.core.mutations.Insertion;
import com.milaboratory.oncomigec.core.mutations.Mutation;
import com.milaboratory.oncomigec.core.mutations.MutationArray;

public class SamRecordBuilder {
    protected String getCigarString(AlignmentResult alignmentResult) {
        int queryLen = alignmentResult.getQuery().size();
        LocalAlignment localAlignment = alignmentResult.getAlignment();
        MutationArray mutations
        StringBuilder cigar = new StringBuilder();

        int s5 = localAlignment.getSequence2Range().getFrom(),
                s3 = queryLen - localAlignment.getSequence2Range().getTo();

        // 5' soft-clipping of consensus bases
        if (s5 > 0)
            cigar.append(s5).append("S");

        // Mutations are in absolute reference coordinates
        int prevPos = localAlignment.getSequence1Range().getFrom();

        for (Mutation mutation : mutations.getMutations()) {
            if (mutation instanceof Indel) {
                int delta = mutation.getStart() - prevPos;
                if (delta > 0) {
                    cigar.append(delta).append("M");
                }
                cigar.append(mutation.getLength()).append(mutation instanceof Insertion ? "I" : "D");
                prevPos = mutation.getEnd(); // same as start for I, start+length for D
            }
        }

        // Remaining matches
        int delta = localAlignment.getSequence1Range().getTo() - prevPos;
        if (delta > 0) {
            cigar.append(delta).append("M");
        }

        // 3' soft-clipping
        if (s3 > 0)
            cigar.append(s3).append("S");

        return cigar.toString();
    }
}
