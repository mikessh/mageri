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

import com.antigenomics.mageri.pipeline.Mageri;
import com.antigenomics.mageri.core.genomic.Contig;
import com.antigenomics.mageri.core.variant.Variant;
import com.antigenomics.mageri.core.variant.VariantCaller;
import com.antigenomics.mageri.core.variant.filter.VariantFilter;
import com.antigenomics.mageri.misc.RecordWriter;
import com.antigenomics.mageri.pipeline.Platform;
import com.antigenomics.mageri.pipeline.analysis.Sample;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

public class VcfWriter extends RecordWriter<VcfRecord, VariantCaller> {
    public VcfWriter(Sample sample, OutputStream outputStream,
                     VariantCaller variantCaller, Platform platform) throws IOException {
        super(sample, outputStream, variantCaller.getReferenceLibrary(), variantCaller, platform);
    }

    @Override
    public String getHeader() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.
                append("##fileformat=VCFv4.0").append("\n").
                append("##fileDate=").append(new Date().toString()).append("\n").
                append("##source=").append(Mageri.MY_NAME).append(Mageri.MY_VERSION).append("\n").
                append("##reference=").append(referenceLibrary.getPath()).append("\n");
        for (Contig contig : referenceLibrary.getGenomicInfoProvider().getContigs()) {
            if (!contig.skipInSamAndVcf()) {
                stringBuilder.append("##contig=<ID=").append(contig.getID()).
                        append(",assembly=").append(contig.getAssembly()).
                        append(",length=").append(contig.getLength()).
                        append(">\n");
            }
        }
        stringBuilder.append("##phasing=none\n");

        // INFO fields
        stringBuilder.append(VcfUtil.INFO_HEADER).append("\n");

        String[] errorModelStatisticIds = pipelineBlock.getErrorModelStatisticIds(),
                errorModelStatisticDescriptions = pipelineBlock.getErrorModelStatisticDescriptions();

        for (int i = 0; i < errorModelStatisticIds.length; i++) {
            stringBuilder.append("##INFO=<ID=")
                    .append(errorModelStatisticIds[i])
                    .append(",Number=1,Type=Float,Description=\"")
                    .append(errorModelStatisticDescriptions[i])
                    .append("\">\n");
        }

        // FILTER fields
        for (int i = 0; i < pipelineBlock.getFilterCount(); i++) {
            VariantFilter filter = pipelineBlock.getFilter(i);
            stringBuilder.append("##FILTER=<ID=").append(filter.getId()).
                    append(",Description=\"").append(filter.getDescription()).append("\">\n");
        }

        // FORMAT fields
        // todo: no genotyping so far
        stringBuilder.append(VcfUtil.FORMAT_HEADER).append("\n");

        stringBuilder.append("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\t").
                append(sample.getFullName());

        return stringBuilder.toString();
    }

    @Override
    public synchronized void write(VcfRecord record) throws IOException {
        writer.println(record.toString());
    }

    public void write(Variant variant) throws IOException {
        if (variant.getReference().getGenomicInfo().getContig().skipInSamAndVcf()) {
            return;
        }

        VcfRecord vcfRecord = VcfUtil.create(variant, pipelineBlock.getErrorModelStatisticIds());
        write(vcfRecord);
    }
}
