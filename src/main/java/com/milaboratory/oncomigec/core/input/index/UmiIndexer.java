package com.milaboratory.oncomigec.core.input.index;

import cc.redberry.pipe.Processor;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.milaboratory.oncomigec.preprocessing.CheckoutResult;
import com.milaboratory.oncomigec.preprocessing.CheckoutProcessor;
import com.milaboratory.oncomigec.misc.ProcessorResultWrapper;

public class UmiIndexer implements Processor<SequencingRead, ProcessorResultWrapper<IndexingInfo>> {
    private final CheckoutProcessor checkoutProcessor;
    private final byte umiQualityThreshold;
    private final ReadWrappingFactory readWrappingFactory;

    public UmiIndexer(CheckoutProcessor checkoutProcessor,
                      byte umiQualityThreshold,
                      ReadWrappingFactory readWrappingFactory) {
        this.checkoutProcessor = checkoutProcessor;
        this.umiQualityThreshold = umiQualityThreshold;
        this.readWrappingFactory = readWrappingFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ProcessorResultWrapper<IndexingInfo> process(SequencingRead milibRead) {
        CheckoutResult result = checkoutProcessor.checkout(milibRead);

        if (result != null && result.isGood(umiQualityThreshold)) {
            String sampleName = result.getSampleName();
            NucleotideSequence umi = result.getUmi();
            ReadContainer readContainer = readWrappingFactory.wrap(milibRead);

            ReadInfo readInfo = new ReadInfo(readContainer, result);

            return new ProcessorResultWrapper<>(new IndexingInfo(readInfo, sampleName, umi));
        }

        return ProcessorResultWrapper.BLANK;
    }

    public CheckoutProcessor getCheckoutProcessor() {
        return checkoutProcessor;
    }
}
