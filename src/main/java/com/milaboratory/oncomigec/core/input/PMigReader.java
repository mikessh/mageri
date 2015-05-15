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

                            BarcodeSearcherResult masterResult = result.getMasterResult(),
                                    slaveResult = result.getSlaveResult();

                            if (masterResult.hasAdapterMatch()) {
                                read1 = read1.trim5Prime(masterResult.getTo()); // getTo() is exclusive to
                            }

                            if (result.slaveFound() && slaveResult.hasAdapterMatch()) {
                                read2 = read2.trim3Prime(slaveResult.getFrom());
                            }
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
