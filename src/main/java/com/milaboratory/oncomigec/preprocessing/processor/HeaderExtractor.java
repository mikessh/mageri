package com.milaboratory.oncomigec.preprocessing.processor;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.milaboratory.oncomigec.preprocessing.barcode.BarcodeSearcher;
import com.milaboratory.oncomigec.preprocessing.entity.CheckoutResult;
import com.milaboratory.oncomigec.preprocessing.entity.SimpleCheckoutResult;
import com.milaboratory.oncomigec.util.Util;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public final class HeaderExtractor extends CheckoutProcessor<SequencingRead, CheckoutResult> {
    private final String sampleName;

    public HeaderExtractor(String sampleName) {
        super(new String[]{sampleName}, new BarcodeSearcher[]{null});
        this.sampleName = sampleName;
    }

    @Override
    public CheckoutResult checkoutImpl(SequencingRead sequencingRead) {
        NucleotideSQPair umiSQPair = Util.extractUmiWithQual(sequencingRead.getDescription(0));

        if (umiSQPair == null) {
            return null;
        } else {
            return new SimpleCheckoutResult(sampleName, umiSQPair);
        }
    }

    @Override
    public boolean isPairedEnd() {
        throw new NotImplementedException();
    }
}
