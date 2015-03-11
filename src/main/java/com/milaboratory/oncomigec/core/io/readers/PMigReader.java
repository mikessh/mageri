/*
 * Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)
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
package com.milaboratory.oncomigec.core.io.readers;

import cc.redberry.pipe.OutputPortCloseable;
import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.quality.QualityFormat;
import com.milaboratory.core.sequencing.io.fastq.PFastqReader;
import com.milaboratory.core.sequencing.read.PSequencingReadImpl;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.milaboratory.oncomigec.core.io.entity.PMig;
import com.milaboratory.oncomigec.core.io.entity.SMig;
import com.milaboratory.oncomigec.core.io.misc.MigReaderParameters;
import com.milaboratory.oncomigec.core.io.misc.ReadInfo;
import com.milaboratory.oncomigec.preproc.demultiplex.barcode.BarcodeSearcherResult;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.PCheckoutResult;
import com.milaboratory.oncomigec.preproc.demultiplex.processor.PCheckoutProcessor;
import com.milaboratory.oncomigec.preproc.misc.ReadOverlapper;
import com.milaboratory.oncomigec.util.Util;
import com.milaboratory.util.CompressionType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class PMigReader extends MigReader<PMig> {
    private final ReadOverlapper readOverlapper = new ReadOverlapper(true);

    public PMigReader(PFastqReader reader, PCheckoutProcessor checkoutProcessor, MigReaderParameters migReaderParameters)
            throws IOException, InterruptedException {
        super(migReaderParameters, checkoutProcessor);

        buildUmiIndex(new PairedReaderWrapper(reader));
    }

    public PMigReader(File file1, File file2,
                      PCheckoutProcessor checkoutProcessor) throws IOException, InterruptedException {
        this(file1, file2, checkoutProcessor, MigReaderParameters.DEFAULT);
    }

    public PMigReader(File file1, File file2,
                      PCheckoutProcessor checkoutProcessor, MigReaderParameters migReaderParameters)
            throws IOException, InterruptedException {
        this(new PFastqReader(file1, file2, QualityFormat.Phred33), checkoutProcessor, migReaderParameters);
    }

    PMigReader(InputStream inputStream1, InputStream inputStream2,
               PCheckoutProcessor checkoutProcessor, MigReaderParameters migReaderParameters)
            throws IOException, InterruptedException {
        this(new PFastqReader(inputStream1, inputStream2, QualityFormat.Phred33, CompressionType.None, null, false, false),
                checkoutProcessor, migReaderParameters);
    }

    PMigReader(InputStream inputStream1, InputStream inputStream2,
               PCheckoutProcessor checkoutProcessor)
            throws IOException, InterruptedException {
        this(inputStream1, inputStream2, checkoutProcessor, MigReaderParameters.DEFAULT);
    }

    public PMigReader(PFastqReader reader, String sampleName, MigReaderParameters migReaderParameters)
            throws IOException, InterruptedException {
        super(migReaderParameters, sampleName);

        buildUmiIndex(new PairedReaderWrapper(reader));
    }

    public PMigReader(File file1, File file2,
                      String sampleName) throws Exception {
        this(file1, file2, sampleName, MigReaderParameters.DEFAULT);
    }

    public PMigReader(File file1, File file2, String sampleName,
                      MigReaderParameters migReaderParameters) throws Exception {
        this(new PFastqReader(file1, file2, QualityFormat.Phred33), sampleName, migReaderParameters);
    }

    PMigReader(InputStream inputStream1, InputStream inputStream2,
               String sampleName, MigReaderParameters migReaderParameters)
            throws IOException, InterruptedException {
        this(new PFastqReader(inputStream1, inputStream2, QualityFormat.Phred33, CompressionType.None, null, false, false),
                sampleName, migReaderParameters);
    }

    PMigReader(InputStream inputStream1, InputStream inputStream2,
               String sampleName)
            throws IOException, InterruptedException {
        this(inputStream1, inputStream2, sampleName, MigReaderParameters.DEFAULT);
    }

    @Override
    protected synchronized PMig take(String sampleName, int sizeThreshold) {
        Iterator<Map.Entry<NucleotideSequence, List<ReadInfo>>> iterator = iteratorMap.get(sampleName);
        while (iterator.hasNext()) {
            Map.Entry<NucleotideSequence, List<ReadInfo>> entry = iterator.next();
            if (entry.getValue().size() >= sizeThreshold && checkUmiMismatch(sampleName, entry.getKey())) {
                List<NucleotideSQPair> readList1 = new ArrayList<>(),
                        readList2 = new ArrayList<>();

                for (ReadInfo readInfo : entry.getValue()) {
                    SequencingRead pRead = readInfo.getRead();
                    NucleotideSQPair read1 = pRead.getData(0),
                            read2 = pRead.getData(1);

                    // Barcode was found in RC version of entire read pair
                    // bring back to strand specified in checkout processor barcode
                    if (readInfo.rcMe()) {
                        read1 = read1.getRC();
                        read2 = read2.getRC();
                    }

                    if (readInfo.flipMe()) {
                        NucleotideSQPair tmp = read2;
                        read2 = read1;
                        read1 = tmp;
                    }

                    // For illumina systems
                    if (checkoutProcessor.performIlluminaRC())
                        read2 = Util.rc(read2);

                    // Trim reads if corresponding option is set
                    // and UMIs were de-novo extracted using adapter search
                    int barcodeOffset = 0;

                    if (migReaderParameters.trimAdapters() &&
                            readInfo.getCheckoutResult() instanceof PCheckoutResult) {
                        PCheckoutResult result = (PCheckoutResult) readInfo.getCheckoutResult();

                        // Trim adapters if required
                        // Convention is
                        // master first:
                        // -M-|            |-S-
                        // -R1|---> -R2----|-->
                        //
                        // slave first:
                        // -S-|            |-M-
                        // -R1|---> -R2----|-->

                        //if (result.masterFirst()) {
                        read1 = read1.getRange(result.getMasterResult().getTo(), read1.size());
                        if (result.getSlaveResult() != BarcodeSearcherResult.BLANK_RESULT)
                            read1 = read1.getRange(0, result.getSlaveResult().getFrom());

                        barcodeOffset = result.getMasterResult().getTo();
                    }

                    ReadOverlapper.OverlapResult overlapResult =
                            readOverlapper.overlap(new PSequencingReadImpl(0, null, null, read1, read2), barcodeOffset);
                    // orient reads, so that all have, depending on user specified options,
                    // either master or slave in RQ
                    //readInfo.flipMe() ?
                    //        readOverlapper.overlap(new PSequencingReadImpl(read2, read1)) :
                    //        readOverlapper.overlap(new PSequencingReadImpl(read1, read2));

                    // Note that we don't need to worry for Illumina RC of mates
                    // even if Overlapper has failed, it performs Illumina RC
                    readList1.add(overlapResult.getReadPair().getData(0));
                    readList2.add(overlapResult.getReadPair().getData(1));
                }

                return new PMig(new SMig(readList1, entry.getKey()),
                        new SMig(readList2, entry.getKey()));
            }

        }
        return null;
    }

    @Override
    public boolean isPairedEnd() {
        return true;
    }

    private class PairedReaderWrapper implements OutputPortCloseable<SequencingRead> {
        private final PFastqReader reader;

        public PairedReaderWrapper(PFastqReader reader) {
            this.reader = reader;
        }

        @Override
        public void close() {
            reader.close();
        }

        @Override
        public SequencingRead take() {
            // allows working with disabled buffering
            synchronized (reader) {
                return reader.take();
            }
        }
    }
}
