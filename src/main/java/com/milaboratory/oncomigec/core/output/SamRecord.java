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

package com.milaboratory.oncomigec.core.output;

import com.milaboratory.oncomigec.misc.Record;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

import static com.milaboratory.oncomigec.core.output.SamUtil.*;

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
