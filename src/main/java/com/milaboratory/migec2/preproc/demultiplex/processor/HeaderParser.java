package com.milaboratory.migec2.preproc.demultiplex.processor;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.milaboratory.migec2.preproc.demultiplex.barcode.BarcodeSearcher;
import com.milaboratory.migec2.preproc.demultiplex.entity.CheckoutResult;
import com.milaboratory.migec2.preproc.demultiplex.entity.SimpleCheckoutResult;
import com.milaboratory.migec2.util.Util;

public final class HeaderParser extends CheckoutProcessor<CheckoutResult, SequencingRead> {
    private final String sampleName;

    public HeaderParser(String sampleName) {
        super(new String[]{sampleName}, new BarcodeSearcher[1], false);
        this.sampleName = sampleName;
    }

    @Override
    public CheckoutResult checkout(SequencingRead sequencingRead) {
        totalCounter.incrementAndGet();

        NucleotideSQPair umiSQPair = Util.extractUmiWithQual(sequencingRead.getDescription(0));

        if (umiSQPair == null) {
            masterNotFoundCounter.incrementAndGet();
            return null;
        } else
            return new SimpleCheckoutResult(sampleName, umiSQPair);
    }

    @Override
    public boolean[] getMasterFirst() {
        return new boolean[]{true};
    }
}
