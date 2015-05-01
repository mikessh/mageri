package com.milaboratory.oncomigec.preprocessing;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.milaboratory.oncomigec.preprocessing.barcode.BarcodeSearcher;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public final class HeaderExtractor extends CheckoutProcessor<SequencingRead, CheckoutResult> {
    private final String sampleName;

    public HeaderExtractor(String sampleName) {
        super(new String[]{sampleName}, new BarcodeSearcher[]{null});
        this.sampleName = sampleName;
    }

    @Override
    public CheckoutResult checkoutImpl(SequencingRead sequencingRead) {
        NucleotideSQPair umiSQPair = extractUmiWithQual(sequencingRead.getDescription(0));

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

    protected static final String UMI_FIELD_ID = "UMI";
    protected static final int UMI_QUAL_OFFSET = UMI_FIELD_ID.length() + 2;

    protected static NucleotideSQPair extractUmiWithQual(String header) {
        for (String field : header.split("[ \t]")) {
            if (field.startsWith(UMI_FIELD_ID)) {
                String seq = field.split(":")[1];
                String qual = field.substring(UMI_QUAL_OFFSET + seq.length());
                return new NucleotideSQPair(seq, qual);
            }
        }

        return null;
    }
}
