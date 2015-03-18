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
import com.milaboratory.oncomigec.core.io.misc.PreprocessorParameters;
import com.milaboratory.oncomigec.core.io.misc.ReadInfo;
import com.milaboratory.oncomigec.pipeline.RuntimeParameters;
import com.milaboratory.oncomigec.preproc.demultiplex.barcode.BarcodeSearcherResult;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.PCheckoutResult;
import com.milaboratory.oncomigec.preproc.demultiplex.processor.CheckoutProcessor;
import com.milaboratory.oncomigec.preproc.misc.ReadOverlapper;
import com.milaboratory.util.CompressionType;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class PMigReader extends MigReader<PMig> {
    private final ReadOverlapper readOverlapper = new ReadOverlapper();

    public PMigReader(PFastqReader reader,
                      CheckoutProcessor checkoutProcessor,
                      PreprocessorParameters preprocessorParameters,
                      RuntimeParameters runtimeParameters)
            throws IOException, InterruptedException {
        super(preprocessorParameters,
                checkoutProcessor,
                runtimeParameters);

        buildUmiIndex(new PairedReaderWrapper(reader));
    }

    public PMigReader(InputStream inputStream1, InputStream inputStream2,
                      CheckoutProcessor checkoutProcessor,
                      PreprocessorParameters preprocessorParameters,
                      RuntimeParameters runtimeParameters)
            throws IOException, InterruptedException {
        this(new PFastqReader(inputStream1, inputStream2, QualityFormat.Phred33, CompressionType.None, null, false, false),
                checkoutProcessor,
                preprocessorParameters, runtimeParameters);
    }

    public PMigReader(InputStream inputStream1, InputStream inputStream2,
                      CheckoutProcessor checkoutProcessor,
                      PreprocessorParameters preprocessorParameters)
            throws IOException, InterruptedException {
        this(inputStream1, inputStream2,
                checkoutProcessor,
                preprocessorParameters, RuntimeParameters.DEFAULT);
    }

    public PMigReader(InputStream inputStream1, InputStream inputStream2,
                      CheckoutProcessor checkoutProcessor)
            throws IOException, InterruptedException {
        this(inputStream1, inputStream2,
                checkoutProcessor,
                PreprocessorParameters.DEFAULT);
    }

    @Override
    protected synchronized PMig take(String sampleName, int sizeThreshold) {
        Iterator<Map.Entry<NucleotideSequence, List<ReadInfo>>> iterator = iteratorMap.get(sampleName);
        while (iterator.hasNext()) {
            Map.Entry<NucleotideSequence, List<ReadInfo>> entry = iterator.next();
            if (entry.getValue().size() >= sizeThreshold && !checkUmiMismatch(sampleName, entry.getKey())) {
                List<NucleotideSQPair> readList1 = new ArrayList<>(),
                        readList2 = new ArrayList<>();

                for (ReadInfo readInfo : entry.getValue()) {
                    SequencingRead pRead = readInfo.getRead();
                    NucleotideSQPair read1 = pRead.getData(0), read2 = pRead.getData(1);

                    if (readInfo.getCheckoutResult() instanceof PCheckoutResult) {
                        PCheckoutResult result = (PCheckoutResult) readInfo.getCheckoutResult();
                        // Orient read so master is first and slave is on the masters strand
                        // Master   Slave
                        // -R1---> -R2------>
                        if (result.getOrientation()) {
                            read1 = pRead.getData(0);
                            read2 = pRead.getData(1).getRC();
                        } else {
                            read1 = pRead.getData(1);
                            read2 = pRead.getData(0).getRC();
                        }

                        // Trim reads if corresponding option is set
                        // and UMIs were de-novo extracted using adapter search
                        int barcodeOffset = 0;

                        if (preprocessorParameters.trimAdapters() &&
                                readInfo.getCheckoutResult() instanceof PCheckoutResult) {

                            // Trim adapters if required
                            // -M-|            |-S-
                            // -R1|---> -R2----|-->

                            read1 = read1.getRange(result.getMasterResult().getTo(), read1.size()); // getTo() is exclusive to
                            if (result.getSlaveResult() != BarcodeSearcherResult.BLANK_RESULT)
                                read2 = read2.getRange(0, result.getSlaveResult().getFrom());
                            barcodeOffset = result.getMasterResult().getTo();
                        }

                        // Try to overlap reads
                        ReadOverlapper.OverlapResult overlapResult =
                                readOverlapper.overlap(new PSequencingReadImpl(0, null, null, read1, read2),
                                        barcodeOffset);

                        // Account for 'master first' attribute
                        if (result.getMasterFirst()) {
                            read1 = overlapResult.getReadPair().getData(0);
                            read2 = overlapResult.getReadPair().getData(1);
                        } else {
                            read1 = overlapResult.getReadPair().getData(1).getRC();
                            read2 = overlapResult.getReadPair().getData(0).getRC();
                        }
                    }

                    // Note that we don't need to worry for Illumina RC of mates
                    // even if Overlapper has failed, it performs Illumina RC
                    readList1.add(read1);
                    readList2.add(read2);
                }

                return new PMig(new SMig(readList1, entry.getKey()),
                        new SMig(readList2, entry.getKey()));
            }

        }
        return null;
    }

    public ReadOverlapper getReadOverlapper() {
        return readOverlapper;
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
