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

import com.milaboratory.oncomigec.core.genomic.Contig;
import com.milaboratory.oncomigec.core.variant.Variant;
import com.milaboratory.oncomigec.core.variant.VariantCaller;
import com.milaboratory.oncomigec.core.variant.filter.VariantFilter;
import com.milaboratory.oncomigec.misc.RecordWriter;
import com.milaboratory.oncomigec.pipeline.Oncomigec;
import com.milaboratory.oncomigec.pipeline.Platform;
import com.milaboratory.oncomigec.pipeline.analysis.Sample;

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
                append("##source=").append(Oncomigec.MY_NAME).append(Oncomigec.MY_VERSION).append("\n").
                append("##reference=").append(referenceLibrary.getPath()).append("\n");
        for (Contig contig : referenceLibrary.getGenomicInfoProvider().getContigs()) {
            stringBuilder.append("##contig=<ID=").append(contig.getID()).
                    append(",assembly=").append(contig.getAssembly()).append(">\n");
        }
        stringBuilder.append("##phasing=none\n");

        // INFO fields
        stringBuilder.append(VcfUtil.INFO_HEADER).append("\n");

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
        VcfRecord vcfRecord = VcfUtil.create(variant);
        write(vcfRecord);
    }
}
