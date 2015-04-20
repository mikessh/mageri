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
 * Last modified on 17.4.2015 by mikesh
 */

package com.milaboratory.oncomigec.core.output;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

import static com.milaboratory.oncomigec.core.output.SamUtil.*;

public class SamRecord {
    private final SamSegmentRecord[] samSegmentRecords;

    private final static Comparator<SamSegmentRecord> comparator = new Comparator<SamSegmentRecord>() {
        @Override
        public int compare(SamSegmentRecord o1, SamSegmentRecord o2) {
            return o1.compareTo(o2);
        }
    };

    public SamRecord(SamSegmentRecord... samSegmentRecords) {
        this.samSegmentRecords = samSegmentRecords;

        Arrays.sort(this.samSegmentRecords, comparator);

        boolean multiSegment = samSegmentRecords.length > 1;

        SamSegmentRecord firstSegment = samSegmentRecords[0],
                lastSegment = samSegmentRecords[samSegmentRecords.length - 1];

        boolean allAligned = true, chimeric = false;
        for (SamSegmentRecord samSegmentRecord : samSegmentRecords) {
            if (samSegmentRecord.hasFlag(UNMAPPED_FLAG)) {
                allAligned = false;
            }
            if (samSegmentRecord.hasFlag(CHIMERIC_ALIGNMENT_FLAG)) {
                chimeric = true;
            }
        }

        int minPos = Integer.MAX_VALUE, maxPos = 0;

        for (int i = 0; i < samSegmentRecords.length; i++) {
            SamSegmentRecord currentRecord = samSegmentRecords[i];
            currentRecord.setFlag(
                    (multiSegment ? MULTIPLE_SEGMENTS_FLAG : BLANK_FLAG) |
                            (allAligned ? ALL_ALIGNED_FLAG : BLANK_FLAG)
            );

            if (i < samSegmentRecords.length - 1) {
                SamSegmentRecord nextRecord = samSegmentRecords[i + 1];

                assert Objects.equals(lastSegment.getQueryName(), nextRecord.getQueryName());

                // Transfer all 'next' flags to current record
                currentRecord.setFlag(getFlagsFromNext(nextRecord.getFlag()));

                currentRecord.setNextReferenceName(nextRecord.getReferenceName());
                currentRecord.setNextPosition(nextRecord.getPosition());

                if (!chimeric) {
                    int pos = currentRecord.getPosition();
                    minPos = Math.min(minPos, pos);
                    maxPos = Math.max(maxPos, pos);
                }
            }
        }

        if (multiSegment && !chimeric) {
            // mark first and last segments
            firstSegment.setFlag(FIRST_SEGMENT_FLAG);
            lastSegment.setFlag(LAST_SEGMENT_FLAG);
            int tLen = maxPos - minPos;
            firstSegment.setTemplateLength(tLen);
            lastSegment.setTemplateLength(-tLen);
        }
    }

    private static int getFlagsFromNext(int flag) {
        return (flag & (UNMAPPED_FLAG | RC_FLAG)) << 1;
    }

    public SamSegmentRecord[] getSamSegmentRecords() {
        return samSegmentRecords;
    }
}
