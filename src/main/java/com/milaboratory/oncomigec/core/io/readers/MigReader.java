package com.milaboratory.oncomigec.core.io.readers;

import cc.redberry.pipe.OutputPort;
import cc.redberry.pipe.OutputPortCloseable;
import cc.redberry.pipe.blocks.Merger;
import cc.redberry.pipe.blocks.ParallelProcessor;
import cc.redberry.pipe.util.CountLimitingOutputPort;
import cc.redberry.pipe.util.CountingOutputPort;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.milaboratory.oncomigec.ReadSpecific;
import com.milaboratory.oncomigec.core.io.entity.Mig;
import com.milaboratory.oncomigec.core.io.misc.PreprocessorParameters;
import com.milaboratory.oncomigec.core.io.misc.ReadInfo;
import com.milaboratory.oncomigec.core.io.misc.UmiHistogram;
import com.milaboratory.oncomigec.core.io.misc.index.IndexingInfo;
import com.milaboratory.oncomigec.core.io.misc.index.UmiIndexer;
import com.milaboratory.oncomigec.pipeline.RuntimeParameters;
import com.milaboratory.oncomigec.pipeline.Speaker;
import com.milaboratory.oncomigec.preprocessing.processor.CheckoutProcessor;
import com.milaboratory.oncomigec.util.ProcessorResultWrapper;

import java.io.Serializable;
import java.util.*;

public abstract class MigReader<MigType extends Mig> implements Serializable, ReadSpecific {
    private static final boolean ENABLE_BUFFERING = false;

    protected final PreprocessorParameters preprocessorParameters;
    protected final RuntimeParameters runtimeParameters;
    private transient final UmiIndexer umiIndexer;
    protected final List<String> sampleNames;

    // Umi index is here
    protected transient final Map<String, Iterator<Map.Entry<NucleotideSequence, List<ReadInfo>>>> iteratorMap = new HashMap<>();
    protected final Map<String, UmiHistogram> umiHistogramBySample = new HashMap<>();

    protected final CheckoutProcessor checkoutProcessor;

    @SuppressWarnings("unchecked")
    protected MigReader(PreprocessorParameters preprocessorParameters,
                        CheckoutProcessor checkoutProcessor,
                        RuntimeParameters runtimeParameters) {
        this.preprocessorParameters = preprocessorParameters;
        this.checkoutProcessor = checkoutProcessor;
        this.sampleNames = checkoutProcessor.getSampleNames();
        this.umiIndexer = new UmiIndexer(checkoutProcessor, preprocessorParameters.getUmiQualThreshold());
        this.runtimeParameters = runtimeParameters;
    }

    protected void buildUmiIndex(OutputPortCloseable<SequencingRead> input)
            throws InterruptedException {

        // Set limit if required
        if (runtimeParameters.getReadLimit() > -1)
            input = new CountLimitingOutputPort<>(input, runtimeParameters.getReadLimit());

        // Buffering reads in separate thread
        if (ENABLE_BUFFERING) {
            final Merger<SequencingRead> bufferedInput = new Merger<>();
            bufferedInput.merge(input);
            bufferedInput.start();
            input = bufferedInput;
        }

        //To count input sequences
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
                                        (int) (umiIndexer.getCheckoutProcessor().extractionRatio() * 100) +
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
            umiHistogramBySample.put(sampleName, new UmiHistogram(preprocessorParameters));
            umiIndexBySample.put(sampleName, new HashMap<NucleotideSequence, List<ReadInfo>>());
        }

        // Take results, update histogram and index (single thread)
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
        for (UmiHistogram histogram : umiHistogramBySample.values())
            histogram.calculateHistogram();

        Speaker.INSTANCE.sout("[Indexer] Finished building UMI index, " +
                countingInput.getCount() + " reads processed, " +
                (int) (umiIndexer.getCheckoutProcessor().extractionRatio() * 100) + "% extracted", 1);
    }

    protected boolean checkUmiMismatch(String sampleName, NucleotideSequence umi) {
        return umiHistogramBySample.get(sampleName).isMismatch(umi);
    }

    protected abstract MigType take(String sampleName, int sizeThreshold);

    @SuppressWarnings("unchecked")
    public List<String> getSampleNames() {
        return checkoutProcessor.getSampleNames();
    }

    public UmiHistogram getUmiHistogram(String sampleName) {
        return umiHistogramBySample.get(sampleName);
    }

    public CheckoutProcessor getCheckoutProcessor() {
        return checkoutProcessor;
    }

    public synchronized void empty() {
        iteratorMap.clear();
    }
}
