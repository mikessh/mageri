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

import java.io.Serializable;
import java.util.regex.Pattern;

public class Contig implements Comparable<Contig>, Serializable {
    private final String ID, assembly;
    private final boolean skipInSamAndVcf;
    private final int length;

    // Contig comparison
    private final boolean canonicalId;
    private final String[] idTokens;

    private static final Pattern A = Pattern.compile("^\\D+$"),
            N = Pattern.compile("^\\d+$");

    private static String[] getTokens(String str) {
        return str.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
    }

    public Contig(String ID, String assembly, int length, boolean skipInSamAndVcf) {
        this.ID = ID;
        this.assembly = assembly;
        this.length = length;
        this.skipInSamAndVcf = skipInSamAndVcf;

        // for comparison
        this.idTokens = getTokens(ID);
        this.canonicalId = idTokens.length > 1 &&
                A.matcher(idTokens[0]).matches() &&
                N.matcher(idTokens[1]).matches();
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

    @Override
    public int compareTo(Contig o) {
        if (canonicalId && o.canonicalId) {
            int result = idTokens[0].compareTo(o.idTokens[0]);
            if (result == 0) {
                return Integer.compare(Integer.parseInt(idTokens[1]),
                        Integer.parseInt(o.idTokens[1]));
            }
            return result;
        } else {
            return ID.compareTo(o.ID);
        }
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

    public static final String HEADER = "assembly\tid";

    @Override
    public String toString() {
        return assembly + "\t" + ID;
    }
}
