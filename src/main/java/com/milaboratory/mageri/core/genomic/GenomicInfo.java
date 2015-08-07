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

package com.milaboratory.mageri.core.genomic;

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
