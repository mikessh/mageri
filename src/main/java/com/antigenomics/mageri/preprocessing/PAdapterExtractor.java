/*
 * Copyright 2014-2016 Mikhail Shugay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.antigenomics.mageri.preprocessing;

import com.antigenomics.mageri.preprocessing.barcode.BarcodeSearcherResult;
import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequencing.read.PSequencingRead;
import com.antigenomics.mageri.preprocessing.barcode.BarcodeSearcher;
import com.antigenomics.mageri.preprocessing.barcode.SeedAndExtendBarcodeSearcher;
import com.antigenomics.mageri.preprocessing.barcode.SlidingBarcodeSearcher;

public final class PAdapterExtractor extends PCheckoutProcessor {
    private final BarcodeSearcher[] slaveBarcodes;
    private final boolean[] masterFirst;
    private final boolean orientedReads;

    public PAdapterExtractor(String[] sampleNames,
                             SeedAndExtendBarcodeSearcher[] masterBarcodes, BarcodeSearcher[] slaveBarcodes,
                             boolean[] masterFirst) {
        this(sampleNames, masterBarcodes, slaveBarcodes, masterFirst, true);
    }

    public PAdapterExtractor(String[] sampleNames,
                             SeedAndExtendBarcodeSearcher[] masterBarcodes, BarcodeSearcher[] slaveBarcodes,
                             boolean[] masterFirst, boolean orientedReads) {
        super(sampleNames, masterBarcodes, slaveBarcodes);
        if (masterBarcodes.length != slaveBarcodes.length)
            throw new RuntimeException("Number of master and slave barcodes provided doesn't agree");

        this.slaveBarcodes = new BarcodeSearcher[slaveBarcodes.length];
        for (int i = 0; i < slaveBarcodes.length; i++) {
            BarcodeSearcher slaveBarcode = slaveBarcodes[i];
            if (slaveBarcode != null) {
                // could also contain sliding barcode searcher
                // in case no capital letters exist in barcode
                // we of course don't forget to wrap it into SlidingBarcodeSearcherR
                if (slaveBarcode instanceof SlidingBarcodeSearcher) {
                    this.slaveBarcodes[i] = ((SlidingBarcodeSearcher) slaveBarcode).getForSlave();
                } else if (slaveBarcode instanceof SeedAndExtendBarcodeSearcher) {
                    this.slaveBarcodes[i] = slaveBarcode;
                } else {
                    throw new RuntimeException("Unsupported barcode searcher: " + slaveBarcode.getClass().getName());
                }
            }
        }
        this.orientedReads = orientedReads;
        this.masterFirst = masterFirst;
    }

    @Override
    public PCheckoutResult checkoutImpl(PSequencingRead read) {
        boolean orientation;

        BarcodeSearcherResult masterResult;

        // Illumina convention (FR), orientation #1
        NucleotideSQPair read1o1 = read.getData(0),
                read2o1 = null,
                read1o2 = read.getData(1),
                read2o2 = null;

        for (int i = 0; i < sampleNames.length; i++) {
            // Search for master, orientation#1
            masterResult = masterBarcodes[i].search(read1o1);
            orientation = true;

            // For non-oriented reads (master is not forced to be in read#1)
            // Search orientation#2
            if (masterResult == null) {
                if (!orientedReads) {
                    masterResult = masterBarcodes[i].search(read1o2);
                    orientation = false;
                }
            }

            // If master is found check for slave
            if (masterResult != null) {

                // No search is performed when slave barcode is blank
                if (slaveBarcodes[i] == null) {
                    return new PCheckoutResult(i, sampleNames[i], orientation, masterFirst[i],
                            masterResult, BarcodeSearcherResult.BLANK_RESULT);
                }

                // Position slave to master strand
                // Lazy calculation here
                if (orientation) {
                    if (read2o1 == null) {
                        read2o1 = read.getData(1).getRC();
                    }
                } else if (read2o2 == null) {
                    read2o2 = read.getData(0).getRC();
                }

                // Get slave for correct orientation
                NucleotideSQPair slaveRead = orientation ? read2o1 : read2o2;

                return new PCheckoutResult(i, sampleNames[i], orientation, masterFirst[i],
                        masterResult, slaveBarcodes[i].search(slaveRead));
            }
        }

        return null;
    }
}
