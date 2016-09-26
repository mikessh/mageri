/*
 * Copyright 2014-2016 Mikhail Shugay
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

package com.antigenomics.mageri.core.output;

import com.antigenomics.mageri.misc.Record;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

import static com.antigenomics.mageri.core.output.SamUtil.*;

public class SamRecord implements Record {
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
            if (multiSegment) {
                currentRecord.setFlag(MULTIPLE_SEGMENTS_FLAG | (allAligned ? ALL_ALIGNED_FLAG : BLANK_FLAG));
            }

            SamSegmentRecord nextRecord;
            if (i < samSegmentRecords.length - 1) {
                nextRecord = samSegmentRecords[i + 1];

                if (!chimeric) {
                    int pos = currentRecord.getPosition();
                    minPos = Math.min(minPos, pos);
                    maxPos = Math.max(maxPos, pos);
                }
            } else if (samSegmentRecords.length > 1) {
                nextRecord = samSegmentRecords[0]; // circular, otherwise fail to verify
            } else {
                break;
            }

            assert Objects.equals(lastSegment.getQueryName(), nextRecord.getQueryName());

            // Transfer all 'next' flags to current record
            currentRecord.setFlag(getFlagsFromNext(nextRecord.getFlag()));

            currentRecord.setNextReferenceName(nextRecord.getReferenceName());
            currentRecord.setNextPosition(nextRecord.getPosition());
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
