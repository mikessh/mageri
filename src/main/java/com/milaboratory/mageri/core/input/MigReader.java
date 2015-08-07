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

package com.milaboratory.mageri.core.input;

import cc.redberry.pipe.OutputPort;
import cc.redberry.pipe.OutputPortCloseable;
import cc.redberry.pipe.blocks.Merger;
import cc.redberry.pipe.blocks.ParallelProcessor;
import cc.redberry.pipe.util.CountLimitingOutputPort;
import cc.redberry.pipe.util.CountingOutputPort;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.milaboratory.mageri.core.Mig;
import com.milaboratory.mageri.core.ReadSpecific;
import com.milaboratory.mageri.core.input.index.*;
import com.milaboratory.mageri.misc.ProcessorResultWrapper;
import com.milaboratory.mageri.pipeline.RuntimeParameters;
import com.milaboratory.mageri.pipeline.Speaker;
import com.milaboratory.mageri.pipeline.analysis.Sample;
import com.milaboratory.mageri.preprocessing.CheckoutProcessor;

import java.io.Serializable;
import java.util.*;

public abstract class MigReader<MigType extends Mig> implements Serializable, ReadSpecific {
    protected final PreprocessorParameters preprocessorParameters;
    protected final RuntimeParameters runtimeParameters;
    private transient final UmiIndexer umiIndexer;
    protected final List<String> sampleNames;

    // Umi index is here
    protected transient final Map<String,
            Iterator<Map.Entry<NucleotideSequence, List<ReadInfo>>>> iteratorMap = new HashMap<>();
    protected final Map<String, MigSizeDistribution> umiHistogramBySample = new HashMap<>();

    protected final CheckoutProcessor checkoutProcessor;

    @SuppressWarnings("unchecked")
    protected MigReader(PreprocessorParameters preprocessorParameters,
                        CheckoutProcessor checkoutProcessor,
                        RuntimeParameters runtimeParameters) {
        this.preprocessorParameters = preprocessorParameters;
        this.checkoutProcessor = checkoutProcessor;
        this.sampleNames = checkoutProcessor.getSampleNames();
        QualityProvider qualityProvider = new QualityProvider(preprocessorParameters.getGoodQualityThreshold());
        this.umiIndexer = new UmiIndexer(checkoutProcessor,
                preprocessorParameters.getUmiQualThreshold(),
                isPairedEnd() ?
                        new PairedEndReadWrappingFactory(qualityProvider) :
                        new SingleEndReadWrappingFactory(qualityProvider));
        this.runtimeParameters = runtimeParameters;
    }

    protected void buildUmiIndex(OutputPortCloseable<SequencingRead> input)
            throws InterruptedException {

        // Set limit if required
        if (runtimeParameters.getReadLimit() > -1) {
            input = new CountLimitingOutputPort<>(input, runtimeParameters.getReadLimit());
        }

        // Buffer the input - speed up and protect from parallelization problems
        final Merger<SequencingRead> bufferedInput = new Merger<>(524288);
        bufferedInput.merge(input);
        bufferedInput.start();
        input = bufferedInput;

        // To count input sequences
        final CountingOutputPort<SequencingRead> countingInput = new CountingOutputPort<>(input);

        // Run checkout in parallel
        if (runtimeParameters.getVerbosityLevel() > 1)
            new Thread(new Runnable() {
                long prevCount = -1;

                @Override
                public void run() {
                    try {
                        while (!countingInput.isClosed()) {
                            long count = countingInput.getCount();
                            if (prevCount != count) {
                                Speaker.INSTANCE.sout("[Indexer] Building UMI index, " +
                                        count + " reads processed, " +
                                        ((int) (umiIndexer.getCheckoutProcessor().extractionRatio() * 10000) / 100.0) +
                                        "% extracted..", 2);
                                prevCount = count;
                            }
                            Thread.sleep(10000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        final OutputPort<ProcessorResultWrapper<IndexingInfo>> indexingResults =
                new ParallelProcessor<>(countingInput, umiIndexer,
                        runtimeParameters.getNumberOfThreads());

        // Create temporary index, histograms
        Map<String, Map<NucleotideSequence, List<ReadInfo>>> umiIndexBySample = new HashMap<>();
        for (String sampleName : sampleNames) {
            umiHistogramBySample.put(sampleName, new MigSizeDistribution(preprocessorParameters));
            umiIndexBySample.put(sampleName, new HashMap<NucleotideSequence, List<ReadInfo>>());
        }

        // Take results, extractMutations histogram and index (single thread)
        ProcessorResultWrapper<IndexingInfo> result;
        while ((result = indexingResults.take()) != null) {
            if (result.hasResult()) {
                IndexingInfo indexingInfo = result.getResult();
                umiHistogramBySample.get(indexingInfo.getSampleName()).update(indexingInfo.getUmi());
                Map<NucleotideSequence, List<ReadInfo>> umiIndex = umiIndexBySample.get(indexingInfo.getSampleName());
                List<ReadInfo> readInfoList = umiIndex.get(indexingInfo.getUmi());
                if (readInfoList == null)
                    umiIndex.put(indexingInfo.getUmi(), readInfoList = new LinkedList<>());
                readInfoList.add(indexingInfo.getReadInfo());
            }
        }

        // Copy index
        for (String sampleName : sampleNames)
            iteratorMap.put(sampleName, umiIndexBySample.get(sampleName).entrySet().iterator());

        // Finalize histograms
        for (MigSizeDistribution histogram : umiHistogramBySample.values())
            histogram.calculateHistogram();

        Speaker.INSTANCE.sout("[Indexer] Finished building UMI index, " +
                countingInput.getCount() + " reads processed, " +
                ((int) (umiIndexer.getCheckoutProcessor().extractionRatio() * 10000) / 100.0) + "% extracted", 1);
    }

    protected boolean checkUmiMismatch(String sampleName, NucleotideSequence umi) {
        return umiHistogramBySample.get(sampleName).isMismatch(umi);
    }

    protected abstract MigType take(Sample sample, String barcodeName, int sizeThreshold);

    public MigType take(Sample sample, int sizeThreshold) {
        return take(sample, sample.getName(), sizeThreshold);
    }

    public MigType take(Sample sample) {
        return take(sample, 1);
    }

    public MigType take(String barcodeName, int sizeThreshold) {
        return take(Sample.create(barcodeName, isPairedEnd()), barcodeName, sizeThreshold);
    }

    public MigType take(String barcodeName) {
        return take(barcodeName, 1);
    }

    @SuppressWarnings("unchecked")
    public List<String> getSampleNames() {
        return checkoutProcessor.getSampleNames();
    }

    public MigSizeDistribution getUmiHistogram(String sampleName) {
        return umiHistogramBySample.get(sampleName);
    }

    public CheckoutProcessor getCheckoutProcessor() {
        return checkoutProcessor;
    }

    public synchronized void clear(Sample sample) {
        iteratorMap.remove(sample.getName());
    }
}