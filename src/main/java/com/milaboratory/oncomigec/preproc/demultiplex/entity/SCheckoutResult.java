package com.milaboratory.oncomigec.preproc.demultiplex.entity;

import com.milaboratory.oncomigec.preproc.demultiplex.barcode.BarcodeSearcherResult;

public final class SCheckoutResult extends CheckoutResult {
    public SCheckoutResult(int sampleId, String sampleName, boolean foundInRC, BarcodeSearcherResult masterResult) {
        super(sampleId, sampleName, foundInRC, masterResult);
    }
}
