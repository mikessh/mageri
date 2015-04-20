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
 * Last modified on 13.4.2015 by mikesh
 */

package com.milaboratory.oncomigec.core.output;

import com.milaboratory.oncomigec.core.genomic.Contig;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.core.mapping.AlignedConsensus;
import com.milaboratory.oncomigec.core.mapping.PAlignedConsensus;
import com.milaboratory.oncomigec.core.mapping.SAlignedConsensus;
import com.milaboratory.oncomigec.pipeline.Oncomigec;
import com.milaboratory.oncomigec.pipeline.analysis.Sample;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class SamWriter implements AutoCloseable {
    private final ReferenceLibrary referenceLibrary;
    private final List<Sample> samples;
    private final PrintWriter writer;

    public SamWriter(ReferenceLibrary referenceLibrary,
                     List<Sample> samples,
                     File outputFile) throws IOException {
        this.referenceLibrary = referenceLibrary;
        this.samples = samples;
        this.writer = new PrintWriter(outputFile);

        writer.println(getHeader());
    }

    private String getHeader() {
        StringBuilder stringBuilder = new StringBuilder("@HD\tVN:1.0\tSO:unsorted\tGO:query");

        for (Contig contig : referenceLibrary.getGenomicInfoProvider().getContigs()) {
            stringBuilder.append("\n@SQ").
                    append("\tSN:").append(contig.getID()).
                    append("\tLN:").append(contig.getLength()).
                    append("\tAS:").append(contig.getAssembly());
        }

        // TODO: instrument (platform)
        for (Sample sample : samples) {
            stringBuilder.append("\n@RG").
                    append("\tID:").append(sample.getId()).
                    append("\tSM:").append(sample.getName().replaceAll("[ \t]", "_")).
                    append("\tPU:").append(sample.getParent().getName().replaceAll("[ \t]", "_")).
                    append("\tLB:").append(sample.getParent().getParent().getName().replaceAll("[ \t]", "_")).
                    append("\tPL:").append("ILLUMINA");
        }

        stringBuilder.append("\n@PG").
                append("\tID:").append("oncomigec").
                append("\tVN:").append(Oncomigec.MY_VERSION);

        return stringBuilder.toString();
    }

    public synchronized void write(SamRecord samRecord, int readGroupId) throws IOException {
        for (SamSegmentRecord segmentRecord : samRecord.getSamSegmentRecords()) {
            writer.println(segmentRecord.toString() + "\tRG:Z:" + readGroupId);
        }
    }

    public void write(AlignedConsensus alignedConsensus) throws IOException {
        SamRecord samRecord = alignedConsensus.isPairedEnd() ?
                SamUtil.create((PAlignedConsensus) alignedConsensus) :
                SamUtil.create((SAlignedConsensus) alignedConsensus);
        write(samRecord, alignedConsensus.getSample().getId());
    }

    public void close() {
        writer.close();
    }
}
