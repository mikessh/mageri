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
package com.milaboratory.oncomigec.core.input;

import cc.redberry.pipe.OutputPortCloseable;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.quality.QualityFormat;
import com.milaboratory.core.sequencing.io.fastq.SFastqReader;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.milaboratory.oncomigec.core.input.index.Read;
import com.milaboratory.oncomigec.core.input.index.ReadInfo;
import com.milaboratory.oncomigec.pipeline.RuntimeParameters;
import com.milaboratory.oncomigec.pipeline.analysis.Sample;
import com.milaboratory.oncomigec.preprocessing.CheckoutProcessor;
import com.milaboratory.oncomigec.preprocessing.SCheckoutResult;
import com.milaboratory.util.CompressionType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
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
    protected synchronized SMig take(Sample sample, String barcodeName, int sizeThreshold) {
        Iterator<Map.Entry<NucleotideSequence, List<ReadInfo>>> iterator = iteratorMap.get(barcodeName);
        while (iterator.hasNext()) {
            Map.Entry<NucleotideSequence, List<ReadInfo>> entry = iterator.next();
            if (entry.getValue().size() >= sizeThreshold && !checkUmiMismatch(barcodeName, entry.getKey())) {
                List<Read> readList = new LinkedList<>();

                for (ReadInfo readInfo : entry.getValue()) {
                    Read read = readInfo.getReadContainer().getFirst();
                    if (readInfo.getCheckoutResult() instanceof SCheckoutResult) {
                        if (preprocessorParameters.trimAdapters()) {
                            SCheckoutResult result = (SCheckoutResult) readInfo.getCheckoutResult();
                            read = read.trim5Prime(result.getMasterResult().getTo());
                        }
                    }
                    // NOTE: Otherwise the checkout processor is a HeaderExtractor
                    // For single-end preprocessed data, we have a convention that
                    // a) read header contains UMI sequence (UMI:seq:qual)
                    // b) reads are oriented in correct direction
                    // c) adapter/primer sequences are trimmed
                    readList.add(read);
                }

                return new SMig(sample, entry.getKey(), readList);
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
