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

package com.antigenomics.mageri.core.genomic;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;

import java.io.Serializable;

public class GenomicInfo implements Serializable, Comparable<GenomicInfo> {
    private final Contig contig;
    private final int start, end;
    private final boolean strand;

    public GenomicInfo(Contig contig, int start, int end, boolean strand) {
        this.contig = contig;
        this.start = start;
        this.end = end;
        this.strand = strand;
    }

    public String getChrom() {
        return contig.getID();
    }

    public Contig getContig() {
        return contig;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public GenomicInfo shift(int offset, NucleotideSequence sequence) {
        int newStart = start + offset,
                newEnd = start + offset + sequence.size(); // BED is 0-based

        if (newStart < start || newEnd > end) {
            throw new IllegalArgumentException("Shift out of bounds of current region.");
        }
        return new GenomicInfo(contig, newStart, newEnd, strand);
    }

    protected boolean positiveStrand() {
        return strand;
    }

    @Override
    public int compareTo(GenomicInfo o) {
        int result = getContig().compareTo(o.getContig());

        if (result == 0) {
            return Integer.compare(getStart(), o.getStart());
        }

        return result;
    }

    public static final String HEADER = Contig.HEADER + "\tstart\tend\tstrand";

    @Override
    public String toString() {
        return contig.toString() + "\t" + start + "\t" + end + "\t" + (strand ? "+" : "-");
    }
}
