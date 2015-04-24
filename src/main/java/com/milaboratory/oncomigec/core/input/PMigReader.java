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
package com.milaboratory.oncomigec.core.input;

import cc.redberry.pipe.OutputPortCloseable;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.quality.QualityFormat;
import com.milaboratory.core.sequencing.io.fastq.PFastqReader;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.milaboratory.oncomigec.core.input.index.Read;
import com.milaboratory.oncomigec.core.input.index.ReadContainer;
import com.milaboratory.oncomigec.core.input.index.ReadInfo;
import com.milaboratory.oncomigec.pipeline.RuntimeParameters;
import com.milaboratory.oncomigec.pipeline.analysis.Sample;
import com.milaboratory.oncomigec.preprocessing.CheckoutProcessor;
import com.milaboratory.oncomigec.preprocessing.PCheckoutResult;
import com.milaboratory.oncomigec.preprocessing.barcode.BarcodeSearcherResult;
import com.milaboratory.util.CompressionType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class PMigReader extends MigReader<PMig> {
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
    protected synchronized PMig take(Sample sample, String barcodeName, int sizeThreshold) {
        Iterator<Map.Entry<NucleotideSequence, List<ReadInfo>>> iterator = iteratorMap.get(barcodeName);
        while (iterator.hasNext()) {
            Map.Entry<NucleotideSequence, List<ReadInfo>> entry = iterator.next();
            if (entry.getValue().size() >= sizeThreshold && !checkUmiMismatch(barcodeName, entry.getKey())) {
                List<Read> readList1 = new LinkedList<>(),
                        readList2 = new LinkedList<>();

                for (ReadInfo readInfo : entry.getValue()) {
                    ReadContainer readContainer = readInfo.getReadContainer();
                    Read read1 = readContainer.getFirst(), read2 = readContainer.getSecond();

                    if (readInfo.getCheckoutResult() instanceof PCheckoutResult) {
                        PCheckoutResult result = (PCheckoutResult) readInfo.getCheckoutResult();
                        // Orient read so master is first and slave is on the masters strand
                        // Master   Slave
                        // -R1---> -R2------>
                        if (result.getOrientation()) {
                            read1 = readContainer.getFirst();
                            read2 = readContainer.getSecond().rc();
                        } else {
                            read1 = readContainer.getSecond();
                            read2 = readContainer.getFirst().rc();
                        }

                        // Trim reads if corresponding option is set
                        // and UMIs were de-novo extracted using adapter search
                        if (preprocessorParameters.trimAdapters()) {
                            // Trim adapters if required
                            // -M-|            |-S-
                            // -R1|---> -R2----|-->

                            read1 = read1.trim5Prime(result.getMasterResult().getTo()); // getTo() is exclusive to
                            if (result.getSlaveResult() != BarcodeSearcherResult.BLANK_RESULT)
                                read2 = read2.trim3Prime(result.getSlaveResult().getFrom());
                        }

                        // Account for 'master first' attribute
                        if (!result.getMasterFirst()) {
                            Read tmp = read1;
                            read1 = read2.rc();
                            read2 = tmp.rc();
                        }
                    }
                    // NOTE: Otherwise the checkout processor is a HeaderExtractor
                    // For preprocessed data, we have a convention that
                    // a) both read headers contain UMI sequence (UMI:seq:qual)
                    // b) reads are oriented in correct direction
                    // c) adapter/primer sequences are trimmed

                    readList1.add(read1);
                    readList2.add(read2);
                }

                return new PMig(new SMig(sample, entry.getKey(), readList1),
                        new SMig(sample, entry.getKey(), readList2));
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
