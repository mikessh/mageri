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

public class SamRecord {
    private final String qname, rname,
            cigar, rnext, seq, qual;
    private final int flag, pos, mapq, pnext, tlen;

    public SamRecord(String qname,
                     Integer flag,
                     String rname, Integer pos,
                     Integer mapq, String cigar,
                     String rnext, Integer pnext,
                     Integer tlen,
                     String seq, String qual) {
        this.qname = valueOrDummy(qname);
        this.flag = valueOrDummy(flag);
        this.rname = valueOrDummy(rname);
        this.pos = valueOrDummy(pos);
        this.mapq = valueOrDummy(mapq);
        this.cigar = valueOrDummy(cigar);
        this.rnext = valueOrDummy(rnext);
        this.pnext = valueOrDummy(pnext);
        this.tlen = valueOrDummy(tlen);
        this.seq = valueOrDummy(seq);
        this.qual = valueOrDummy(qual);
    }

    private static int valueOrDummy(Integer field) {
        return field == null ? 0 : field;
    }

    private static String valueOrDummy(String field) {
        return field == null ? "*" : field;
    }

    @Override
    public String toString() {
        return qname + "\t" +
                flag + "\t" +
                rname + "\t" + pos + "\t" +
                mapq + "\t" + cigar + "\t" +
                rnext + "\t" + pnext + "\t" +
                tlen + "\t" +
                seq + "\t" + qual;
    }
}
