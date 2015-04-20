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

package com.milaboratory.oncomigec.core.output;

import com.milaboratory.oncomigec.core.genomic.Contig;
import com.milaboratory.oncomigec.core.variant.Variant;
import com.milaboratory.oncomigec.core.variant.VariantCaller;
import com.milaboratory.oncomigec.core.variant.filter.VariantFilter;
import com.milaboratory.oncomigec.misc.RecordWriter;
import com.milaboratory.oncomigec.pipeline.Oncomigec;
import com.milaboratory.oncomigec.pipeline.analysis.Sample;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class VcfWriter extends RecordWriter<VcfRecord> {
    private final VariantCaller variantCaller;

    public VcfWriter(Sample sample, File outputFile,
                     VariantCaller variantCaller) throws IOException {
        super(sample, outputFile, variantCaller.getReferenceLibrary());
        this.variantCaller = variantCaller;
    }

    public String getHeader() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.
                append("##fileformat=VCFv4.0").append("\n").
                append("##fileDate=").append(new Date().toString()).append("\n").
                append("##source=oncomigec").append(Oncomigec.MY_VERSION).append("\n").
                append("##reference=").append(referenceLibrary.getPath()).append("\n");
        for (Contig contig : referenceLibrary.getGenomicInfoProvider().getContigs()) {
            stringBuilder.append("##contig=<ID=").append(contig.getID()).
                    append(",assembly=").append(contig.getAssembly()).append(">\n");
        }
        stringBuilder.append("##phasing=none\n");

        // INFO fields
        stringBuilder.append(VcfUtil.INFO_HEADER).append("\n");

        // FILTER fields
        for (int i = 0; i < variantCaller.getFilterCount(); i++) {
            VariantFilter filter = variantCaller.getFilter(i);
            stringBuilder.append("##FILTER=<ID=").append(filter.getId()).
                    append(",Description=\"").append(filter.getDescription()).append("\">\n");
        }

        // FORMAT fields
        // todo: no genotyping so far
        stringBuilder.append(VcfUtil.FORMAT_HEADER).append("\n");

        stringBuilder.append("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\t").
                append(sample.getFullName()).append("\n");

        return stringBuilder.toString();
    }

    @Override
    public synchronized void write(VcfRecord record) throws IOException {
        writer.println(record.toString());
    }

    public void write(Variant variant) throws IOException {
        VcfRecord vcfRecord = VcfUtil.create(variant);
        write(vcfRecord);
    }
}
