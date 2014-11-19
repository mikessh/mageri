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
package com.milaboratory.migec2.core.io.readers;

import cc.redberry.pipe.OutputPortCloseable;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.quality.QualityFormat;
import com.milaboratory.core.sequencing.io.fastq.SFastqReader;
import com.milaboratory.core.sequencing.io.fastq.SRandomAccessFastqReader;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.milaboratory.migec2.core.io.entity.SMig;
import com.milaboratory.migec2.core.io.misc.MigReaderParameters;
import com.milaboratory.migec2.core.io.misc.ReadInfo;
import com.milaboratory.migec2.preproc.demultiplex.processor.SCheckoutProcessor;
import com.milaboratory.migec2.util.Util;
import com.milaboratory.util.CompressionType;
import com.milaboratory.util.io.RecordIndexer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class SMigReader extends MigReader<SMig> {
    private SRandomAccessFastqReader rar;

    public SMigReader(File file, SCheckoutProcessor checkoutProcessor) throws Exception {
        this(file, checkoutProcessor, MigReaderParameters.DEFAULT);
    }

    //todo: store chekout and from stored checkout
    public SMigReader(File file, SCheckoutProcessor checkoutProcessor, MigReaderParameters migReaderParameters)
            throws IOException, InterruptedException {
        super(migReaderParameters, checkoutProcessor);

        preprocess(file);
    }

    public SMigReader(File file, String sampleName) throws Exception {
        this(file, sampleName, MigReaderParameters.DEFAULT);
    }

    public SMigReader(File file, String sampleName, MigReaderParameters migReaderParameters) throws Exception {
        super(migReaderParameters, sampleName);

        preprocess(file);
    }

    private void preprocess(File file) throws IOException, InterruptedException {
        // Only work with uncompressed files
        final SFastqReader reader = new SFastqReader(file, QualityFormat.Phred33, CompressionType.None);

        // Creating indexer
        final RecordIndexer indexer = new RecordIndexer(1L);
        reader.attachIndexer(indexer);

        // Build UMI index
        buildUmiIndex(new SingleReaderWrapper(reader));

        // Creating random access fastq reader
        rar = new SRandomAccessFastqReader(indexer.createIndex(), file);
    }

    @Override
    protected SMig take(String sampleName, int sizeThreshold) {
        Iterator<Map.Entry<NucleotideSequence, List<ReadInfo>>> iterator = iteratorMap.get(sampleName);
        while (iterator.hasNext()) {
            Map.Entry<NucleotideSequence, List<ReadInfo>> entry = iterator.next();
            if (entry.getValue().size() >= sizeThreshold && checkUmiMismatch(sampleName, entry.getKey())) {
                List<SSequencingRead> readList = new ArrayList<>();

                for (ReadInfo readInfo : entry.getValue()) {
                    rar.seek(readInfo.id());
                    readList.add(readInfo.rcMe() ? Util.rc(rar.readNext()) : rar.readNext());
                }

                return new SMig(readList, entry.getKey());
            }
        }
        return null;
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
            synchronized (reader) {
                return reader.take();
            }
        }
    }
}
