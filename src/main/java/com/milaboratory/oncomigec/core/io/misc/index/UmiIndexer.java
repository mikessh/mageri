package com.milaboratory.oncomigec.core.io.misc.index;

import cc.redberry.pipe.Processor;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.milaboratory.oncomigec.core.io.misc.ReadInfo;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.CheckoutResult;
import com.milaboratory.oncomigec.preproc.demultiplex.processor.CheckoutProcessor;
import com.milaboratory.oncomigec.util.ProcessorResultWrapper;

public class UmiIndexer implements Processor<SequencingRead, ProcessorResultWrapper<IndexingInfo>> {
    private final CheckoutProcessor checkoutProcessor;
    private final byte umiQualityThreshold;

    public UmiIndexer(CheckoutProcessor checkoutProcessor, byte umiQualityThreshold) {
        this.checkoutProcessor = checkoutProcessor;
        this.umiQualityThreshold = umiQualityThreshold;
    }

    @Override
    public ProcessorResultWrapper<IndexingInfo> process(SequencingRead sequencingRead) {
        CheckoutResult result = checkoutProcessor.checkout(sequencingRead);

        if (result != null && result.isGood(umiQualityThreshold)) {
            String sampleName = result.getSampleName();
            NucleotideSequence umi = result.getUmi();

            ReadInfo readInfo = new ReadInfo(sequencingRead, result);

            return new ProcessorResultWrapper<>(new IndexingInfo(readInfo, sampleName, umi));
        }

        return ProcessorResultWrapper.BLANK;
    }

    public CheckoutProcessor getCheckoutProcessor() {
        return checkoutProcessor;
    }
}
