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

import com.milaboratory.core.sequencing.read.SequencingRead;
import com.milaboratory.oncomigec.core.ReadSpecific;
import com.milaboratory.oncomigec.preprocessing.barcode.BarcodeSearcher;

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

    public long getTotal() {
        return totalCounter.get();
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
