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
package com.milaboratory.oncomigec.preprocessing;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequencing.read.PSequencingRead;
import com.milaboratory.oncomigec.preprocessing.barcode.BarcodeSearcher;
import com.milaboratory.oncomigec.preprocessing.barcode.BarcodeSearcherResult;
import com.milaboratory.oncomigec.preprocessing.barcode.SeedAndExtendBarcodeSearcher;
import com.milaboratory.oncomigec.preprocessing.barcode.SlidingBarcodeSearcher;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

public final class PAdapterExtractor extends CheckoutProcessor<PSequencingRead, PCheckoutResult> {
    private final AtomicLongArray slaveCounters;
    private final AtomicLong masterFirstCounter;
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
        super(sampleNames, masterBarcodes);
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
        this.masterFirstCounter = new AtomicLong();
        this.slaveCounters = new AtomicLongArray(masterBarcodes.length);
    }

    @Override
    public PCheckoutResult checkoutImpl(PSequencingRead read) {
        boolean orientation;

        BarcodeSearcherResult masterResult, slaveResult;

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
            } else {
                masterFirstCounter.incrementAndGet();
            }

            // If master is found check for slave
            if (masterResult != null) {
                masterCounters.incrementAndGet(i);

                // No search is performed when slave barcode is blank
                if (slaveBarcodes[i] == null) {
                    slaveCounters.incrementAndGet(i);
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

                if ((slaveResult = slaveBarcodes[i].search(slaveRead)) != null) {
                    slaveCounters.incrementAndGet(i);
                    return new PCheckoutResult(i, sampleNames[i], orientation, masterFirst[i],
                            masterResult, slaveResult);
                } else {
                    return null;
                }
            }
        }

        return null;
    }

    public long getSlaveCounter(String sampleName) {
        List<Integer> sampleIds = sampleNameToId.get(sampleName);
        if (sampleIds == null)
            throw new RuntimeException("Sample " + sampleName + " doesn't exist");
        long count = 0;
        for (int id : sampleIds)
            count += slaveCounters.get(id);
        return count;
    }

    @Override
    public double getMasterFirstRatio() {
        return masterFirstCounter.get() / (double) totalCounter.get();
    }

    @Override
    public double extractionRatio() {
        double total = totalCounter.get(), slaveFoundTotal = 0;

        for (String sampleName : sampleNames) {
            slaveFoundTotal += getSlaveCounter(sampleName);
        }

        return 1 - slaveFoundTotal / total;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("\n");
        sb.append("Slave");
        for (int i = 0; i < sampleNames.length; i++) {
            sb.append("\t");
            sb.append(slaveCounters.get(i));
        }
        sb.append("\t");
        sb.append(totalCounter.get());
        return sb.toString();
    }

    @Override
    public boolean isPairedEnd() {
        return true;
    }
}
