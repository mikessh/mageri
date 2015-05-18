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

package com.milaboratory.oncomigec.core.genomic;

import java.io.Serializable;
import java.util.regex.Pattern;

public class Contig implements Comparable<Contig>, Serializable {
    private final String ID, assembly;
    private final boolean skipInSamAndVcf;
    private final int length;

    public Contig(String ID, String assembly, int length, boolean skipInSamAndVcf) {
        this.ID = ID;
        this.assembly = assembly;
        this.length = length;
        this.skipInSamAndVcf = skipInSamAndVcf;
    }

    public String getID() {
        return ID;
    }

    public String getAssembly() {
        return assembly;
    }

    public int getLength() {
        return length;
    }

    public boolean skipInSamAndVcf() {
        return skipInSamAndVcf;
    }

    private static final Pattern A = Pattern.compile("^\\D+$"),
            N = Pattern.compile("^\\d+$");

    private static String[] getTokens(String str) {
        return str.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
    }

    private static int compare(String o1, String o2) {
        // Check if contig name is in form of chr##
        String[] t1 = getTokens(o1), t2 = getTokens(o2);
        if (A.matcher(t1[0]).matches() && N.matcher(t1[1]).matches() &&
                A.matcher(t2[0]).matches() && N.matcher(t2[1]).matches()) {
            int result = t1[0].compareTo(t2[0]);
            if (result == 0) {
                return Integer.compare(Integer.parseInt(t1[1]), Integer.parseInt(t2[1]));
            }
            return result;
        } else {
            return o1.compareTo(o2);
        }
    }

    @Override
    public int compareTo(Contig o) {
        return compare(ID, o.ID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Contig contig = (Contig) o;

        if (!ID.equals(contig.ID)) return false;
        if (!assembly.equals(contig.assembly)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ID.hashCode();
        result = 31 * result + assembly.hashCode();
        return result;
    }
}
