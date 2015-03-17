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

import com.milaboratory.core.sequencing.read.SequencingRead;
import com.milaboratory.oncomigec.ReadSpecific;
import com.milaboratory.oncomigec.preproc.demultiplex.barcode.BarcodeSearcher;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.CheckoutResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

public abstract class CheckoutProcessor<ReadType extends SequencingRead, ResultType extends CheckoutResult>
        implements ReadSpecific, Serializable {
    protected final AtomicLongArray masterCounters;
    protected final AtomicLong masterNotFoundCounter, totalCounter;
    protected final String[] sampleNames;
    protected final List<String> sampleNameList = new ArrayList<>();
    protected final BarcodeSearcher[] masterBarcodes;
    protected final HashMap<String, List<Integer>> sampleNameToId = new HashMap<>();

    protected CheckoutProcessor(String[] sampleNames, BarcodeSearcher[] masterBarcodes) {
        this.sampleNames = sampleNames;
        this.masterBarcodes = masterBarcodes;
        this.masterNotFoundCounter = new AtomicLong();
        this.totalCounter = new AtomicLong();
        this.masterCounters = new AtomicLongArray(masterBarcodes.length);

        for (int i = 0; i < masterBarcodes.length; i++) {
            List<Integer> idsForSample = sampleNameToId.get(sampleNames[i]);
            if (idsForSample == null) {
                sampleNameToId.put(sampleNames[i], idsForSample = new ArrayList<>());
                sampleNameList.add(sampleNames[i]);
            }
            idsForSample.add(i);
        }
    }

    public long getMasterCounter(String sampleName) {
        List<Integer> sampleIds = sampleNameToId.get(sampleName);
        if (sampleIds == null)
            throw new RuntimeException("Sample " + sampleName + " doesn't exist");
        long count = 0;
        for (int id : sampleIds)
            count += masterCounters.get(id);
        return count;
    }


    public abstract ResultType checkoutImpl(ReadType read);

    public ResultType checkout(ReadType read) {
        totalCounter.incrementAndGet();
        ResultType result = checkoutImpl(read);

        if (result == null)
            masterNotFoundCounter.incrementAndGet();

        return result;
    }

    public List<String> getSampleNames() {
        return Collections.unmodifiableList(sampleNameList);
    }

    public double getMasterFirstRatio() {
        return 1.0;
    }

    public double extractionRatio() {
        double total = totalCounter.get(), notFound = masterNotFoundCounter.get();
        return 1.0 - notFound / total;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Counter");
        for (int i = 0; i < sampleNames.length; i++) {
            sb.append("\t");
            sb.append(sampleNames[i]);
        }
        sb.append("\tNot found\tTotal\nMaster");
        for (int i = 0; i < sampleNames.length; i++) {
            sb.append("\t");
            sb.append(masterCounters.get(i));
        }
        sb.append("\t");
        sb.append(masterNotFoundCounter.get());
        sb.append("\t");
        sb.append(totalCounter.get());
        return sb.toString();
    }
}
