package com.milaboratory.migec2.core.io.readers;

import cc.redberry.pipe.OutputPort;
import cc.redberry.pipe.blocks.Merger;
import cc.redberry.pipe.blocks.ParallelProcessor;
import cc.redberry.pipe.util.CountLimitingOutputPort;
import cc.redberry.pipe.util.CountingOutputPort;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.milaboratory.migec2.core.io.entity.Mig;
import com.milaboratory.migec2.core.io.misc.MigReaderParameters;
import com.milaboratory.migec2.core.io.misc.ReadInfo;
import com.milaboratory.migec2.core.io.misc.UmiHistogram;
import com.milaboratory.migec2.core.io.misc.index.IndexingInfo;
import com.milaboratory.migec2.core.io.misc.index.UmiIndexer;
import com.milaboratory.migec2.pipeline.MigecCli;
import com.milaboratory.migec2.preproc.demultiplex.processor.CheckoutProcessor;
import com.milaboratory.migec2.preproc.demultiplex.processor.HeaderParser;
import com.milaboratory.migec2.util.ProcessorResultWrapper;

import java.util.*;

public abstract class MigReader<T extends Mig> implements OutputPort<T> {
    private static final boolean ENABLE_BUFFERING = false;

    protected int sizeThreshold;
    protected String currentSample;
    protected double minMismatchRatio = -1;

    protected final MigReaderParameters migReaderParameters;
    private final UmiIndexer umiIndexer;
    protected final List<String> sampleNames;

    // Umi index is here
    protected final Map<String, Iterator<Map.Entry<NucleotideSequence, List<ReadInfo>>>> iteratorMap = new HashMap<>();
    protected final Map<String, UmiHistogram> umiHistogramBySample = new HashMap<>();

    private final CheckoutProcessor checkoutProcessor;

    protected MigReader(MigReaderParameters migReaderParameters, CheckoutProcessor checkoutProcessor) {
        this.migReaderParameters = migReaderParameters;
        this.checkoutProcessor = checkoutProcessor;
        this.sampleNames = checkoutProcessor.getSampleNames();
        currentSample = sampleNames.get(0);
        this.umiIndexer = new UmiIndexer(checkoutProcessor, migReaderParameters.getUmiQualThreshold());
    }

    protected MigReader(MigReaderParameters migReaderParameters, String sampleName) {
        this.migReaderParameters = migReaderParameters;
        this.sampleNames = new ArrayList<>();
        sampleNames.add(sampleName);
        currentSample = sampleName;
        this.checkoutProcessor = new HeaderParser(sampleName);
        this.umiIndexer = new UmiIndexer(checkoutProcessor, migReaderParameters.getUmiQualThreshold());
    }

    protected void buildUmiIndex(OutputPort<SequencingRead> input)
            throws InterruptedException {

        // Set limit if required
        if (migReaderParameters.getLimit() > 0)
            input = new CountLimitingOutputPort<>(input, migReaderParameters.getLimit());

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
        if (migReaderParameters.verbose())
            new Thread(new Runnable() {
                long prevCount = -1;

                @Override
                public void run() {
                    try {
                        while (!countingInput.isClosed()) {
                            long count = countingInput.getCount();
                            if (prevCount != count) {
                                MigecCli.print2("Building UMI index, " +
                                        count + " reads processed, " +
                                        (int) (umiIndexer.getCheckoutProcessor().extractionRatio() * 100) +
                                        "% extracted..");
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
                        migReaderParameters.getThreads());

        // Create temporary index, histograms
        Map<String, Map<NucleotideSequence, List<ReadInfo>>> umiIndexBySample = new HashMap<>();
        for (String sampleName : sampleNames) {
            umiHistogramBySample.put(sampleName, new UmiHistogram());
            umiIndexBySample.put(sampleName, new HashMap<NucleotideSequence, List<ReadInfo>>());
        }

        // Take results, update histogram and index (not parallel)
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

        if (migReaderParameters.verbose())
            MigecCli.print2("Finished building UMI index, " +
                    countingInput.getCount() + " reads processed, " +
                    (int) (umiIndexer.getCheckoutProcessor().extractionRatio() * 100) + "% extracted");
    }

    protected boolean checkUmiMismatch(String sampleName, NucleotideSequence umi) {
        return minMismatchRatio < 1 ||
                !umiHistogramBySample.get(sampleName).isMismatch(umi, minMismatchRatio);
    }

    public T take() {
        return take(currentSample, sizeThreshold);
    }

    protected abstract T take(String sampleName, int sizeThreshold);

    public List<String> getSampleNames() {
        return sampleNames;
    }

    public UmiHistogram getUmiHistogram(String sampleName) {
        return umiHistogramBySample.get(sampleName);
    }

    public CheckoutProcessor getCheckoutProcessor() {
        return checkoutProcessor;
    }

    public String getCurrentSample() {
        return currentSample;
    }

    public int getSizeThreshold() {
        return sizeThreshold;
    }

    public void setSizeThreshold(int sizeThreshold) {
        this.sizeThreshold = sizeThreshold;
    }

    public double getMinMismatchRatio() {
        return minMismatchRatio;
    }

    public void setMinMismatchRatio(double minMismatchRatio) {
        this.minMismatchRatio = minMismatchRatio;
    }

    public void setCurrentSample(String currentSample) {
        this.currentSample = currentSample;
    }
}
