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
import com.milaboratory.core.sequencing.io.fastq.SFastqReader;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.milaboratory.oncomigec.core.io.entity.SMig;
import com.milaboratory.oncomigec.core.io.misc.PreprocessorParameters;
import com.milaboratory.oncomigec.core.io.misc.ReadInfo;
import com.milaboratory.oncomigec.pipeline.RuntimeParameters;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.SCheckoutResult;
import com.milaboratory.oncomigec.preproc.demultiplex.processor.CheckoutProcessor;
import com.milaboratory.util.CompressionType;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class SMigReader extends MigReader<SMig> {
    public SMigReader(SFastqReader reader,
                      CheckoutProcessor checkoutProcessor,
                      PreprocessorParameters preprocessorParameters,
                      RuntimeParameters runtimeParameters)
            throws IOException, InterruptedException {
        super(preprocessorParameters, checkoutProcessor, runtimeParameters);

        buildUmiIndex(new SingleReaderWrapper(reader));
    }

    public SMigReader(InputStream inputStream1,
                      CheckoutProcessor checkoutProcessor,
                      PreprocessorParameters preprocessorParameters,
                      RuntimeParameters runtimeParameters)
            throws IOException, InterruptedException {
        this(new SFastqReader(inputStream1, QualityFormat.Phred33, CompressionType.None),
                checkoutProcessor,
                preprocessorParameters, runtimeParameters);
    }

    public SMigReader(InputStream inputStream1,
                      CheckoutProcessor checkoutProcessor,
                      PreprocessorParameters preprocessorParameters)
            throws IOException, InterruptedException {
        this(inputStream1,
                checkoutProcessor,
                preprocessorParameters, RuntimeParameters.DEFAULT);
    }

    public SMigReader(InputStream inputStream1,
                      CheckoutProcessor checkoutProcessor)
            throws IOException, InterruptedException {
        this(inputStream1,
                checkoutProcessor,
                PreprocessorParameters.DEFAULT);
    }

    @Override
    protected synchronized SMig take(String sampleName, int sizeThreshold) {
        Iterator<Map.Entry<NucleotideSequence, List<ReadInfo>>> iterator = iteratorMap.get(sampleName);
        while (iterator.hasNext()) {
            Map.Entry<NucleotideSequence, List<ReadInfo>> entry = iterator.next();
            if (entry.getValue().size() >= sizeThreshold && checkUmiMismatch(sampleName, entry.getKey())) {
                List<NucleotideSQPair> readList = new ArrayList<>();

                for (ReadInfo readInfo : entry.getValue()) {
                    NucleotideSQPair read = readInfo.getRead().getData(0);
                    if (readInfo.getCheckoutResult() instanceof SCheckoutResult) {
                        if (preprocessorParameters.trimAdapters()) {
                            SCheckoutResult result = (SCheckoutResult) readInfo.getCheckoutResult();
                            read = read.getRange(result.getMasterResult().getTo(), read.size());
                        }
                    }
                    // NOTE: Otherwise the checkout processor is a HeaderExtractor
                    // For single-end preprocessed data, we have a convention that
                    // a) read header contains UMI sequence (UMI:seq:qual)
                    // b) reads are oriented in correct direction
                    // c) adapter/primer sequences are trimmed
                    readList.add(read);
                }

                return new SMig(readList, entry.getKey());
            }
        }
        return null;
    }

    @Override
    public boolean isPairedEnd() {
        return false;
    }

    private class SingleReaderWrapper implements OutputPortCloseable<SequencingRead> {
        private final SFastqReader reader;

        public SingleReaderWrapper(SFastqReader reader) {
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
