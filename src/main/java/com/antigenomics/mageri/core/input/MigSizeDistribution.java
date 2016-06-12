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
package com.antigenomics.mageri.core.input;

import com.antigenomics.mageri.core.PipelineBlock;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.util.Bit2Array;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MigSizeDistribution extends PipelineBlock {
    private final PreprocessorParameters preprocessorParameters;
    private transient final ConcurrentHashMap<NucleotideSequence, AtomicInteger> umiCounterMap =
            new ConcurrentHashMap<>();

    private static final int N = 20;

    private static int convertToIndex(long value) {
        return (int) (Math.log((double) value) / base);
    }

    private static int convertToValue(int index) {
        return (int) Math.pow(2.0, index);
    }

    private final long[] migHistogram = new long[N], readHistogram = new long[N];
    private long readTotal = 0;
    private int migsTotal;
    private static final double base = Math.log(2.0);

    public MigSizeDistribution(PreprocessorParameters preprocessorParameters) {
        super("umi.histogram");
        this.preprocessorParameters = preprocessorParameters;
    }

    public MigSizeDistribution() {
        this(PreprocessorParameters.DEFAULT);
    }

    public boolean isMismatch(NucleotideSequence umi) {
        if (preprocessorParameters.getMinUmiMismatchRatio() < 1)
            return false;

        Bit2Array innerData = umi.getInnerData();
        int counter = umiCounterMap.get(umi).get();

        for (int i = 0; i < umi.size(); i++) {
            byte oldNt = (byte) innerData.get(i);
            for (byte j = 0; j < 4; j++) {
                if (j != oldNt) {
                    innerData.set(i, j);

                    NucleotideSequence otherUmi = new NucleotideSequence(innerData);
                    AtomicInteger otherCounter = umiCounterMap.get(otherUmi);

                    if (otherCounter != null &&
                            counter * preprocessorParameters.getMinUmiMismatchRatio() < otherCounter.get())
                        return true;
                }
            }
            innerData.set(i, oldNt);
        }

        return false;
    }

    public void update(NucleotideSequence umi) {
        AtomicInteger blankUmiCounter = new AtomicInteger(),
                umiCounter = umiCounterMap.putIfAbsent(umi, blankUmiCounter);

        if (umiCounter == null)
            umiCounter = blankUmiCounter;

        umiCounter.incrementAndGet();
    }

    public void calculateHistogram() {
        for (AtomicInteger counter : umiCounterMap.values()) {
            long value = counter.get();

            int sizeIndex = Math.min(convertToIndex(value), N - 1);

            readHistogram[sizeIndex] += value;
            readTotal += value;

            migHistogram[sizeIndex]++;
        }

        migsTotal = umiCounterMap.size();
    }

    public int getMigSizeThreshold() {
        // empirical for now
        int overSeqPeak = -1;
        long valueAtPeak = -1;

        for (int i = 0; i < N; i++) {
            if (readHistogram[i] > valueAtPeak) {
                valueAtPeak = readHistogram[i];
                overSeqPeak = i;
            }
        }

        return convertToValue(overSeqPeak / 2);
    }

    public long calculateReadsRetained(int migSizeThreshold) {
        return readTotal - calculateReadsDropped(migSizeThreshold);
    }

    public long calculateReadsDropped(int migSizeThreshold) {
        long result = 0;
        for (int i = 0; i < convertToIndex(migSizeThreshold); i++) {
            result += readHistogram[i];
        }
        return result;
    }

    public int calculateMigsRetained(int migSizeThreshold) {
        return migsTotal - calculateMigsDropped(migSizeThreshold);
    }

    public int calculateMigsDropped(int migSizeThreshold) {
        int result = 0;
        for (int i = 0; i < convertToIndex(migSizeThreshold); i++) {
            result += migHistogram[i];
        }
        return result;
    }


    public int migSize(NucleotideSequence umi) {
        return umiCounterMap.get(umi).get();
    }

    public long getReadTotal() {
        return readTotal;
    }

    public int getMigsTotal() {
        return migsTotal;
    }


    @Override
    public String getHeader() {
        return "mig.size.bin\tmig.count\tread.count\tmig.size.threshold";
    }

    @Override
    public String getBody() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < N; i++) {
            stringBuilder.append(convertToValue(i)).append("\t").
                    append(migHistogram[i]).append("\t").
                    append(readHistogram[i]).append("\t").
                    append(getMigSizeThreshold()).append("\n");
        }

        return stringBuilder.toString();
    }
}
