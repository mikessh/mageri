package com.milaboratory.oncomigec.preprocessing.entity;

import com.milaboratory.oncomigec.preprocessing.barcode.BarcodeSearcherResult;

public final class SCheckoutResult extends CheckoutResult {
    public SCheckoutResult(int sampleId, String sampleName, BarcodeSearcherResult masterResult) {
        super(sampleId, sampleName, masterResult);
    }
}
