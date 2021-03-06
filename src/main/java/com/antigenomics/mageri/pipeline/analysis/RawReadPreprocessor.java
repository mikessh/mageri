/*
 * Copyright 2014-2016 Mikhail Shugay
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

package com.antigenomics.mageri.pipeline.analysis;

import cc.redberry.pipe.*;
import cc.redberry.pipe.blocks.Merger;
import cc.redberry.pipe.util.CountLimitingOutputPort;
import com.antigenomics.mageri.core.Mig;
import com.antigenomics.mageri.core.assemble.Consensus;
import com.antigenomics.mageri.core.input.*;
import com.antigenomics.mageri.core.input.index.ReadContainer;
import com.antigenomics.mageri.core.input.raw.IndexedReadContainer;
import com.antigenomics.mageri.core.input.raw.PRawReadProcessor;
import com.antigenomics.mageri.core.input.raw.SRawReadProcessor;
import com.antigenomics.mageri.core.input.raw.SomewhatRawReadProperlyWrapped;
import com.antigenomics.mageri.misc.ProcessorResultWrapper;
import com.antigenomics.mageri.pipeline.RuntimeParameters;
import com.antigenomics.mageri.pipeline.input.CheckoutRule;
import com.antigenomics.mageri.pipeline.input.Input;
import com.antigenomics.mageri.pipeline.input.InputChunk;
import com.antigenomics.mageri.preprocessing.CheckoutProcessor;
import com.antigenomics.mageri.preprocessing.DemultiplexParameters;
import com.milaboratory.core.sequence.quality.QualityFormat;
import com.milaboratory.core.sequencing.io.fastq.PFastqReader;
import com.milaboratory.core.sequencing.io.fastq.SFastqReader;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.milaboratory.util.CompressionType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class RawReadPreprocessor<MigType extends Mig> implements Preprocessor<MigType> {
    private final SampleGroup sampleGroup;
    private final Map<String, LinkedBlockingQueue<ReadContainer>> buffersBySample = new HashMap<>();
    private final CheckoutProcessor checkoutProcessor;
    private final Thread readerThread;
    private final VoidProcessorFactory<SequencingRead> innerProcessorFactory;

    public RawReadPreprocessor(Input input, SampleGroup sampleGroup) throws IOException, InterruptedException {
        this(input, sampleGroup, DemultiplexParameters.DEFAULT, PreprocessorParameters.DEFAULT);
    }

    public RawReadPreprocessor(Input input, SampleGroup sampleGroup,
                               DemultiplexParameters demultiplexParameters,
                               PreprocessorParameters preprocessorParameters) throws IOException, InterruptedException {
        this(input, sampleGroup, demultiplexParameters, preprocessorParameters, RuntimeParameters.DEFAULT);
    }

    @SuppressWarnings("unchecked")
    public RawReadPreprocessor(Input input, SampleGroup sampleGroup,
                               DemultiplexParameters demultiplexParameters,
                               PreprocessorParameters preprocessorParameters,
                               final RuntimeParameters runtimeParameters) throws IOException, InterruptedException {
        this.sampleGroup = sampleGroup;

        final InputChunk inputChunk = input.getByName(sampleGroup.getName());

        CheckoutRule checkoutRule = inputChunk.getCheckoutRule();
        checkoutRule.setDemultiplexParameters(demultiplexParameters);
        this.checkoutProcessor = checkoutRule.getProcessor();

        for (Sample sample : sampleGroup.getSamples()) {
            buffersBySample.put(sample.getName(), new LinkedBlockingQueue<ReadContainer>(524288));
        }

        final Processor<SequencingRead, IndexedReadContainer> demultiplexer = sampleGroup.isPairedEnd() ?
                new PRawReadProcessor(checkoutProcessor, preprocessorParameters) :
                new SRawReadProcessor(checkoutProcessor, preprocessorParameters);

        this.innerProcessorFactory = new VoidProcessorFactory() {
            @Override
            public VoidProcessor<SequencingRead> create() {
                return new VoidProcessor<SequencingRead>() {
                    @Override
                    public void process(SequencingRead sequencingRead) {
                        try {
                            IndexedReadContainer result = demultiplexer.process(sequencingRead);
                            if (result.isIndexed()) {
                                LinkedBlockingQueue<ReadContainer> buffer = buffersBySample.get(result.getSampleName());
                                buffer.put(result.getRead());
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
            }
        };

        this.readerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OutputPort<SequencingRead> input = inputChunk.isPairedEnd() ?
                            new PReadOutputPort(inputChunk) :
                            new SReadOutputPort(inputChunk);

                    if (runtimeParameters.getReadLimit() > -1) {
                        input = new CountLimitingOutputPort<>(input, runtimeParameters.getReadLimit());
                    }

                    // Buffer the input - speed up and protect from parallelization problems
                    final Merger<SequencingRead> bufferedInput = new Merger<>(524288);
                    bufferedInput.merge(input);
                    bufferedInput.start();
                    input = bufferedInput;

                    CUtils.processAllInParallel(input, innerProcessorFactory,
                            runtimeParameters.getNumberOfThreads());

                    for (LinkedBlockingQueue<ReadContainer> buffer : buffersBySample.values()) {
                        buffer.put(ReadContainer.LAST); // Redberry-pipe is null-based, but here nulls are not allowed
                    }
                } catch (InterruptedException ignored) {
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private static class SReadOutputPort implements OutputPort<SequencingRead> {
        private final SFastqReader reader;

        public SReadOutputPort(InputChunk inputChunk) throws IOException {
            this.reader = new SFastqReader(inputChunk.getInputStream1(), QualityFormat.Phred33, CompressionType.None);
        }

        @Override
        public SequencingRead take() {
            return reader.take();
        }
    }

    private static class PReadOutputPort implements OutputPort<SequencingRead> {
        private final PFastqReader reader;

        public PReadOutputPort(InputChunk inputChunk) throws IOException {
            this.reader = new PFastqReader(inputChunk.getInputStream1(), inputChunk.getInputStream2(),
                    QualityFormat.Phred33, CompressionType.None, null, false, false);
        }

        @Override
        public SequencingRead take() {
            return reader.take();
        }
    }

    @Override
    public MigSizeDistribution getUmiHistogram(Sample sample) {
        throw new NotImplementedException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public OutputPort<ProcessorResultWrapper<Consensus>> createRaw(final Sample sample) {
        if (!sampleGroup.getSamples().contains(sample))
            throw new RuntimeException("Sample " + sample + " not found in sample group " + sampleGroup);

        final LinkedBlockingQueue<ReadContainer> buffer = buffersBySample.get(sample.getName());

        return new OutputPort<ProcessorResultWrapper<Consensus>>() {
            @Override
            public SomewhatRawReadProperlyWrapped take() {
                try {
                    ReadContainer readContainer = buffer.take();

                    if (readContainer.isLast()) {
                        return null;
                    } else {
                        return new SomewhatRawReadProperlyWrapped(sample, readContainer);
                    }
                } catch (InterruptedException ignored) {
                    return null;
                }
            }
        };
    }

    @Override
    public MigOutputPort<MigType> create(Sample sample) {
        throw new NotImplementedException();
    }

    @Override
    public int getOverSeq(String sampleName) {
        return -1;
    }

    @Override
    public SampleGroup getSampleGroup() {
        return sampleGroup;
    }

    @Override
    public CheckoutProcessor getCheckoutProcessor() {
        return checkoutProcessor;
    }

    @Override
    public boolean isPairedEnd() {
        return sampleGroup.isPairedEnd();
    }

    public void start() {
        readerThread.start();
    }

    public void stop() throws InterruptedException {
        readerThread.join();
    }
}
