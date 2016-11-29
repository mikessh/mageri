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

package com.antigenomics.mageri.core.output;

import com.antigenomics.mageri.core.genomic.GenomicInfo;
import com.antigenomics.mageri.core.variant.Variant;
import com.antigenomics.mageri.core.mutations.Mutation;

public final class VcfUtil {
    public static final int MAX_QUAL = 999, UNDEF_QUAL = -1;

    private VcfUtil() {
    }

    public static String
            BLANK_FIELD = ".",
            INFO_HEADER =
                    "##INFO=<ID=DP,Number=1,Type=Integer,Description=\"Total Depth\">\n" +
                            "##INFO=<ID=AF,Number=1,Type=Float,Description=\"Allele Frequency\">\n" +
                            "##INFO=<ID=AA,Number=1,Type=String,Description=\"Ancestral Allele\">\n" +
                            "##INFO=<ID=CQ,Number=1,Type=Integer,Description=\"Assembly quality\">\n" +
                            "##INFO=<ID=RI,Number=1,Type=String,Description=\"Reference id\">",
            FORMAT_HEADER =
                    "##FORMAT=<ID=GT,Number=1,Type=String,Description=\"Genotype\">\n" +
                            "##INFO=<ID=DP,Number=1,Type=Integer,Description=\"MIG Depth\">",
            FORMAT_KEY = "GT:DP";

    public static VcfRecord create(Variant variant, String[] errorModelStatisticIds) {
        GenomicInfo genomicInfo = variant.getReference().getGenomicInfo();
        Mutation mutation = variant.getMutation();
        return new VcfRecord(genomicInfo.getChrom(),
                genomicInfo.getStart() + mutation.getStart() + 1, // BED is 0-based, while VCF is 1-based
                BLANK_FIELD,
                variant.getRef(), variant.getAlt(),
                (int) Math.min(variant.getQual(), MAX_QUAL),
                variant.getFilterSummary().toString(),
                getInfo(variant, errorModelStatisticIds),
                FORMAT_KEY,
                getSampleInfo(variant)
        );
    }

    public static String getInfo(Variant variant, String[] errorModelStatisticIds) {
        String baseInfo = "DP=" + variant.getDepth() +
                ";AF=" + (float) variant.getAlleleFrequency() +
                ";AA=" + variant.getAncestralAllele() +
                ";CQ=" + (float) variant.getCqs() +
                ";RI=" + variant.getReference().getName();

        double[] statisticValues = variant.getErrorRateEstimate().getStatistics();

        for (int i = 0; i < errorModelStatisticIds.length; i++) {
            baseInfo += ";" + errorModelStatisticIds[i] + "=" + (float) statisticValues[i];
        }

        return baseInfo;
    }

    public static String getSampleInfo(Variant variant) {
        return "0/1:" + variant.getDepth();
    }
}
