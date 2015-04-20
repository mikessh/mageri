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
 * Last modified on 20.4.2015 by mikesh
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
                genomicInfo.getFrom() + mutation.getStart() + 1,
                BLANK_FIELD,
                variant.hasReference() ? mutation.getRef().toString() : BLANK_FIELD, mutation.getAlt().toString(),
                (int) variant.getQual(), variant.getFilterSummary().toString(),
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
