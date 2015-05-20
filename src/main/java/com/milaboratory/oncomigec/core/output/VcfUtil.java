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

import com.milaboratory.oncomigec.core.genomic.GenomicInfo;
import com.milaboratory.oncomigec.core.mutations.Mutation;
import com.milaboratory.oncomigec.core.variant.Variant;

public final class VcfUtil {
    private VcfUtil() {
    }

    public static String
            BLANK_FIELD = ".",
            INFO_HEADER =
                    "##INFO=<ID=DP,Number=1,Type=Integer,Description=\"Total Depth\">\n" +
                            "##INFO=<ID=AF,Number=.,Type=Float,Description=\"Allele Frequency\">\n" +
                            "##INFO=<ID=AA,Number=1,Type=String,Description=\"Ancestral Allele\">",
            FORMAT_HEADER =
                    "##FORMAT=<ID=GT,Number=1,Type=String,Description=\"Genotype\">\n" +
                            "##INFO=<ID=DP,Number=1,Type=Integer,Description=\"MIG Depth\">",
            FORMAT_KEY = "GT:DP";

    public static VcfRecord create(Variant variant) {
        GenomicInfo genomicInfo = variant.getReference().getGenomicInfo();
        Mutation mutation = variant.getMutation();
        return new VcfRecord(genomicInfo.getChrom(),
                genomicInfo.getStart() + mutation.getStart() + 1,
                BLANK_FIELD,
                mutation.getRef().toString(), mutation.getAlt().toString(),
                (int) Math.min(variant.getQual(), 9999), variant.getFilterSummary().toString(),
                getInfo(variant),
                FORMAT_KEY, getSampleInfo(variant)
        );
    }

    public static String getInfo(Variant variant) {
        return "DP=" + variant.getDepth() +
                ";AF=" + (float) variant.getAlleleFrequency() +
                ";AA=" + variant.getAncestralAllele().toString();
    }

    public static String getSampleInfo(Variant variant) {
        return "0/1:" + variant.getDepth();
    }
}
