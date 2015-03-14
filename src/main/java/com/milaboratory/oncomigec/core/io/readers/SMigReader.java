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
import com.milaboratory.oncomigec.core.io.misc.MigReaderParameters;
import com.milaboratory.oncomigec.core.io.misc.ReadInfo;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.CheckoutResult;
import com.milaboratory.oncomigec.preproc.demultiplex.processor.SAdapterExtractor;
import com.milaboratory.util.CompressionType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class SMigReader extends MigReader<SMig> {
    public SMigReader(SFastqReader reader, SAdapterExtractor checkoutProcessor, MigReaderParameters migReaderParameters)
            throws IOException, InterruptedException {
        super(migReaderParameters, checkoutProcessor);

        buildUmiIndex(new SingleReaderWrapper(reader));
    }

    public SMigReader(File file1,
                      SAdapterExtractor checkoutProcessor) throws IOException, InterruptedException {
        this(file1, checkoutProcessor, MigReaderParameters.DEFAULT);
    }

    public SMigReader(File file1,
                      SAdapterExtractor checkoutProcessor, MigReaderParameters migReaderParameters)
            throws IOException, InterruptedException {
        this(new SFastqReader(file1), checkoutProcessor, migReaderParameters);
    }

    SMigReader(InputStream inputStream1,
               SAdapterExtractor checkoutProcessor, MigReaderParameters migReaderParameters)
            throws IOException, InterruptedException {
        this(new SFastqReader(inputStream1, QualityFormat.Phred33, CompressionType.None),
                checkoutProcessor, migReaderParameters);
    }

    SMigReader(InputStream inputStream1,
               SAdapterExtractor checkoutProcessor)
            throws IOException, InterruptedException {
        this(inputStream1, checkoutProcessor, MigReaderParameters.DEFAULT);
    }

    public SMigReader(SFastqReader reader, String sampleName, MigReaderParameters migReaderParameters)
            throws IOException, InterruptedException {
        super(migReaderParameters, sampleName);

        buildUmiIndex(new SingleReaderWrapper(reader));
    }

    public SMigReader(File file1,
                      String sampleName) throws Exception {
        this(file1, sampleName, MigReaderParameters.DEFAULT);
    }

    public SMigReader(File file1, String sampleName,
                      MigReaderParameters migReaderParameters) throws Exception {
        this(new SFastqReader(file1), sampleName, migReaderParameters);
    }

    SMigReader(InputStream inputStream1,
               String sampleName, MigReaderParameters migReaderParameters)
            throws IOException, InterruptedException {
        this(new SFastqReader(inputStream1, QualityFormat.Phred33, CompressionType.None),
                sampleName, migReaderParameters);
    }

    SMigReader(InputStream inputStream1,
               String sampleName)
            throws IOException, InterruptedException {
        this(inputStream1, sampleName, MigReaderParameters.DEFAULT);
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
                    CheckoutResult result = readInfo.getCheckoutResult();
                    read = read.getRange(result.getMasterResult().getTo(), read.size());
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
