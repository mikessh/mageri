/*
 * Copyright (c) 2014-2015, Bolotin Dmitry, Chudakov Dmitry, Shugay Mikhail
 * (here and after addressed as Inventors)
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact the Inventors using one of the following
 * email addresses: chudakovdm@mail.ru, chudakovdm@gmail.com
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */
package com.milaboratory.mageri.preprocessing;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequencing.read.PSequencingRead;
import com.milaboratory.mageri.preprocessing.barcode.BarcodeSearcher;
import com.milaboratory.mageri.preprocessing.barcode.BarcodeSearcherResult;
import com.milaboratory.mageri.preprocessing.barcode.SeedAndExtendBarcodeSearcher;
import com.milaboratory.mageri.preprocessing.barcode.SlidingBarcodeSearcher;

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
