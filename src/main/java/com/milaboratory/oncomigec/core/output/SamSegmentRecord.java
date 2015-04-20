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
 * Last modified on 15.4.2015 by mikesh
 */

package com.milaboratory.oncomigec.core.output;

import java.io.Serializable;

import static com.milaboratory.oncomigec.core.output.SamUtil.*;

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
