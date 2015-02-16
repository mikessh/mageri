/*
 * Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)
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
package com.milaboratory.oncomigec.preproc.demultiplex.processor;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequencing.read.PSequencingRead;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.PCheckoutResult;
import com.milaboratory.oncomigec.preproc.demultiplex.barcode.BarcodeSearcher;
import com.milaboratory.oncomigec.preproc.demultiplex.barcode.BarcodeSearcherResult;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

public final class PCheckoutProcessor extends CheckoutProcessor<PCheckoutResult, PSequencingRead> {
    private final AtomicLongArray slaveCounters;
    private final AtomicLong slaveNotFoundCounter;
    private final BarcodeSearcher[] slaveBarcodes;
    private final boolean[] masterFirst;
    private final boolean nonOrientedReads, illuminaReads;

    public PCheckoutProcessor(String[] sampleNames,
                              BarcodeSearcher[] masterBarcodes, BarcodeSearcher[] slaveBarcodes,
                              boolean[] masterFirst) throws Exception {
        this(sampleNames, masterBarcodes, slaveBarcodes, masterFirst, false, false, true);
    }

    public PCheckoutProcessor(String[] sampleNames,
                              BarcodeSearcher[] masterBarcodes, BarcodeSearcher[] slaveBarcodes,
                              boolean[] masterFirst,
                              boolean checkRC, boolean nonOrientedReads, boolean illuminaReads) throws Exception {
        super(sampleNames, masterBarcodes, checkRC);
        if (masterBarcodes.length != slaveBarcodes.length)
            throw new Exception("Number of master and slave barcodes provided doesn't agree");

        this.slaveBarcodes = slaveBarcodes;
        this.nonOrientedReads = nonOrientedReads;
        this.illuminaReads = illuminaReads;
        this.masterFirst = masterFirst;
        this.slaveNotFoundCounter = new AtomicLong();
        this.slaveCounters = new AtomicLongArray(masterBarcodes.length);
    }

    @Override
    public PCheckoutResult checkout(PSequencingRead read) {
        totalCounter.incrementAndGet();

        int masterReadId;
        boolean masterRC;
        BarcodeSearcherResult masterResult, slaveResult;

        for (int i = 0; i < sampleNames.length; i++) {
            // Search for master
            // Search 1st and 2nd read if non-oriented (only 1st read otherwise)
            // Search both strands if checkRC is specified
            masterRC = false;
            masterReadId = 0;
            masterResult = masterBarcodes[i].search(read.getData(0));

            if (masterResult == null && nonOrientedReads) {
                masterReadId = 1;
                masterResult = masterBarcodes[i].search(read.getData(1));
            }

            if (masterResult == null && checkRC) {
                masterRC = true;
                masterReadId = 0;
                masterResult = masterBarcodes[i].search(read.getData(0).getRC());
                if (masterResult == null && nonOrientedReads) {
                    masterRC = true;
                    masterReadId = 1;
                    masterResult = masterBarcodes[i].search(read.getData(1).getRC());
                }
            }

            // If master is found check for slave
            if (masterResult != null) {
                masterCounters.incrementAndGet(i);
                NucleotideSQPair slaveRead = read.getData(1 - masterReadId);

                // No search is performed when slave barcode is blank
                if (slaveBarcodes[i] == null) {
                    slaveCounters.incrementAndGet(i);
                    return new PCheckoutResult(i, sampleNames[i], masterReadId == 0, masterRC,
                            masterResult, BarcodeSearcherResult.BLANK_RESULT);
                }

                // Position slave to master strand
                slaveRead = (illuminaReads ^ masterRC) ? slaveRead.getRC() : slaveRead;

                if ((slaveResult = slaveBarcodes[i].search(slaveRead)) != null)
                    slaveCounters.incrementAndGet(i);
                else
                    slaveNotFoundCounter.incrementAndGet();

                return new PCheckoutResult(i, sampleNames[i], masterReadId == 0, masterRC,
                        masterResult, slaveResult);
            }
        }

        masterNotFoundCounter.incrementAndGet();

        return null;
    }

    public long getSlaveCounter(String sampleName) throws Exception {
        List<Integer> sampleIds = sampleNameToId.get(sampleName);
        if (sampleIds == null)
            throw new Exception("Sample " + sampleName + " doesn't exist");
        long count = 0;
        for (int id : sampleIds)
            count += slaveCounters.get(id);
        return count;
    }

    @Override
    public boolean[] getMasterFirst() {
        return masterFirst;
    }

    public boolean illuminaReads() {
        return illuminaReads;
    }

    @Override
    public double extractionRatio() {
        double total = totalCounter.get(),
                notFoundMaster = masterNotFoundCounter.get(),
                notFoundSlave = slaveNotFoundCounter.get();
        return 1 - (notFoundMaster + notFoundSlave) / total;
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
        sb.append(slaveNotFoundCounter.get());
        sb.append("\t");
        sb.append(totalCounter.get());
        return sb.toString();
    }
}
