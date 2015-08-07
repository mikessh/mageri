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

package com.milaboratory.mageri.core.output;

import com.milaboratory.mageri.core.genomic.Contig;
import com.milaboratory.mageri.core.mapping.AlignedConsensus;
import com.milaboratory.mageri.core.mapping.ConsensusAligner;
import com.milaboratory.mageri.core.mapping.PAlignedConsensus;
import com.milaboratory.mageri.core.mapping.SAlignedConsensus;
import com.milaboratory.mageri.misc.RecordWriter;
import com.milaboratory.mageri.pipeline.Mageri;
import com.milaboratory.mageri.pipeline.Platform;
import com.milaboratory.mageri.pipeline.analysis.Sample;

import java.io.IOException;
import java.io.OutputStream;

public class SamWriter extends RecordWriter<SamRecord, ConsensusAligner> {
    public SamWriter(Sample sample, OutputStream outputStream,
                     ConsensusAligner consensusAligner, Platform platform) throws IOException {
        super(sample, outputStream, consensusAligner.getReferenceLibrary(), consensusAligner, platform);
    }

    @Override
    protected String getHeader() {
        StringBuilder stringBuilder = new StringBuilder("@HD\tVN:1.0\tSO:unsorted\tGO:query");

        for (Contig contig : referenceLibrary.getGenomicInfoProvider().getContigs()) {
            if (!contig.skipInSamAndVcf()) {
                stringBuilder.append("\n@SQ").
                        append("\tSN:").append(contig.getID()).
                        append("\tLN:").append(contig.getLength()).
                        append("\tAS:").append(contig.getAssembly());
            }
        }

        stringBuilder.append("\n@RG").
                append("\tID:").append(sample.getId()).
                append("\tSM:").append(groomString(sample.getName())).
                append("\tPU:").append(groomString(sample.getGroupName())).
                append("\tLB:").append(groomString(sample.getProjectName())).
                append("\tPL:").append(platform.toString());

        stringBuilder.append("\n@PG").
                append("\tID:").append(Mageri.MY_NAME).
                append("\tVN:").append(Mageri.MY_VERSION).
                append("\tCL:").append(Mageri.MY_COMMAND);

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

        if (samRecord != null) {
            write(samRecord);
        }
    }
}