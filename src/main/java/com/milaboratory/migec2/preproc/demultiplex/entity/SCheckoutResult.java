package com.milaboratory.migec2.preproc.demultiplex.entity;

import com.milaboratory.migec2.preproc.demultiplex.barcode.BarcodeSearcherResult;

public final class SCheckoutResult extends CheckoutResult {
    public SCheckoutResult(int sampleId, String sampleName, boolean foundInRC, BarcodeSearcherResult masterResult) {
        super(sampleId, sampleName, foundInRC, masterResult);
    }
}
