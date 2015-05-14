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
 * Last modified on 17.3.2015 by mikesh
 */

package com.milaboratory.oncomigec.core.genomic;

import java.io.Serializable;

public class GenomicInfo implements Serializable {
    private final Contig contig;
    private final int from, to;
    private final boolean strand;

    public GenomicInfo(Contig contig, int from, int to, boolean strand) {
        this.contig = contig;
        this.from = from;
        this.to = to;
        this.strand = strand;
    }

    public String getChrom() {
        return contig.getID();
    }

    public Contig getContig() {
        return contig;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    protected boolean positiveStrand() {
        return strand;
    }
}
