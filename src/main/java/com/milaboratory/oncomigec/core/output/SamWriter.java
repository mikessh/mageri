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
import com.milaboratory.oncomigec.core.mapping.AlignedConsensus;
import com.milaboratory.oncomigec.core.mapping.ConsensusAligner;
import com.milaboratory.oncomigec.core.mapping.PAlignedConsensus;
import com.milaboratory.oncomigec.core.mapping.SAlignedConsensus;
import com.milaboratory.oncomigec.misc.RecordWriter;
import com.milaboratory.oncomigec.pipeline.Oncomigec;
import com.milaboratory.oncomigec.pipeline.analysis.Sample;

import java.io.IOException;
import java.io.OutputStream;

public class SamWriter extends RecordWriter<SamRecord, ConsensusAligner> {

    public SamWriter(Sample sample, OutputStream outputStream, ConsensusAligner consensusAligner) throws IOException {
        super(sample, outputStream, consensusAligner.getReferenceLibrary(), consensusAligner);
    }

    @Override
    protected String getHeader() {
        StringBuilder stringBuilder = new StringBuilder("@HD\tVN:1.0\tSO:unsorted\tGO:query");

        for (Contig contig : referenceLibrary.getGenomicInfoProvider().getContigs()) {
            stringBuilder.append("\n@SQ").
                    append("\tSN:").append(contig.getID()).
                    append("\tLN:").append(contig.getLength()).
                    append("\tAS:").append(contig.getAssembly());
        }

        // TODO: instrument (platform)
        stringBuilder.append("\n@RG").
                append("\tID:").append(sample.getId()).
                append("\tSM:").append(groomString(sample.getName())).
                append("\tPU:").append(groomString(sample.getGroupName())).
                append("\tLB:").append(groomString(sample.getProjectName())).
                append("\tPL:").append("ILLUMINA");

        stringBuilder.append("\n@PG").
                append("\tID:").append(Oncomigec.MY_NAME).
                append("\tVN:").append(Oncomigec.MY_VERSION).
                append("\tCL:").append(Oncomigec.MY_COMMAND);

        return stringBuilder.toString();
    }

    private static String groomString(String string) {
        return string.replaceAll("[ \t]", "_");
    }

    @Override
    public synchronized void write(SamRecord samRecord) throws IOException {
        for (SamSegmentRecord segmentRecord : samRecord.getSamSegmentRecords()) {
            writer.println(segmentRecord.toString() + "\tRG:Z:" + sample.getId());
        }
    }

    public void write(AlignedConsensus alignedConsensus) throws IOException {
        SamRecord samRecord = alignedConsensus.isPairedEnd() ?
                SamUtil.create((PAlignedConsensus) alignedConsensus) :
                SamUtil.create((SAlignedConsensus) alignedConsensus);
        write(samRecord);
    }
}