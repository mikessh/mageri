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

package com.milaboratory.mageri.core.output;

import java.io.Serializable;

import static com.milaboratory.mageri.core.output.SamUtil.*;

public final class SamSegmentRecord implements Comparable<SamSegmentRecord>, Serializable {
    private final String queryName, referenceName,
            cigarString, sequence, quality;
    private String nextReferenceName;
    private final int position, mapqScore;
    private int flag, nextPosition, templateLength;

    public SamSegmentRecord(String queryName,
                            String sequence, String quality) {
        this(queryName,
                UNMAPPED_FLAG,
                DUMMY_STRING, DUMMY_INT,
                DUMMY_INT, DUMMY_STRING,
                sequence, quality);
    }

    public SamSegmentRecord(String queryName,
                            Integer flag,
                            String referenceName, Integer position,
                            Integer mapqScore, String cigarString,
                            String sequence, String quality) {
        this(queryName,
                flag,
                referenceName, position,
                mapqScore, cigarString,
                DUMMY_STRING, DUMMY_INT,
                DUMMY_INT,
                sequence, quality);
    }

    public SamSegmentRecord(String queryName,
                            Integer flag,
                            String referenceName, Integer position,
                            Integer mapqScore, String cigarString,
                            String nextReferenceName, int nextPosition,
                            int templateLength,
                            String sequence, String quality) {
        this.queryName = queryName;
        this.flag = flag;
        this.referenceName = referenceName;
        this.position = position;
        this.mapqScore = mapqScore;
        this.cigarString = cigarString;
        this.nextReferenceName = nextReferenceName;
        this.nextPosition = nextPosition;
        this.templateLength = templateLength;
        this.sequence = sequence;
        this.quality = quality;
    }

    public String getQueryName() {
        return queryName;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public String getCigarString() {
        return cigarString;
    }

    public String getSequence() {
        return sequence;
    }

    public String getQuality() {
        return quality;
    }

    public String getNextReferenceName() {
        return nextReferenceName;
    }

    public int getPosition() {
        return position;
    }

    public int getMapqScore() {
        return mapqScore;
    }

    public int getNextPosition() {
        return nextPosition;
    }

    public int getTemplateLength() {
        return templateLength;
    }

    protected void setNextReferenceName(String nextReferenceName) {
        this.nextReferenceName = nextReferenceName;
    }

    protected void setFlag(int flag) {
        this.flag |= flag;
    }

    protected void unsetFlag(int flag) {
        this.flag &= ~flag;
    }

    protected int getFlag() {
        return flag;
    }

    public boolean hasFlag(int flag) {
        return (this.flag & flag) == flag;
    }

    protected void setNextPosition(int nextPosition) {
        this.nextPosition = nextPosition;
    }

    protected void setTemplateLength(int templateLength) {
        this.templateLength = templateLength;
    }

    @Override
    public String toString() {
        return queryName + "\t" +
                flag + "\t" +
                referenceName + "\t" + position + "\t" +
                mapqScore + "\t" + cigarString + "\t" +
                nextReferenceName + "\t" + nextPosition + "\t" +
                templateLength + "\t" +
                sequence + "\t" + quality;
    }

    @Override
    public int compareTo(SamSegmentRecord o) {
        int primary = referenceName.compareTo(o.referenceName);

        return primary != 0 ? primary :
                Integer.compare(position, o.position);
    }
}
