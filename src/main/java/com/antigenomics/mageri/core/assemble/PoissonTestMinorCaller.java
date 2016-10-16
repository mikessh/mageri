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

package com.antigenomics.mageri.core.assemble;

import com.antigenomics.mageri.core.input.PreprocessorParameters;
import com.antigenomics.mageri.misc.AtomicDouble;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import org.apache.commons.math.MathException;
import org.apache.commons.math.special.Gamma;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PoissonTestMinorCaller extends MinorCaller<PoissonTestMinorCaller> {
    private final AssemblerParameters assemblerParameters;
    private final PreprocessorParameters preprocessorParameters;
    private final double seqErrorRate;
    private final AtomicInteger[][] m1 = new AtomicInteger[4][4],
            m = new AtomicInteger[4][4];
    private final AtomicLong[][] minorReadCountSumArr = new AtomicLong[4][4],
            totalReadCountSumArr = new AtomicLong[4][4];
    private final AtomicDouble[][] pValueSum = new AtomicDouble[4][4];

    PoissonTestMinorCaller(AssemblerParameters assemblerParameters, PreprocessorParameters preprocessorParameters) {
        super("MinorCaller.PoissonTest");
        this.assemblerParameters = assemblerParameters;
        this.preprocessorParameters = preprocessorParameters;
        this.seqErrorRate = Math.pow(10.0, -(double) preprocessorParameters.getGoodQualityThreshold() / 10.0);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                this.m1[i][j] = new AtomicInteger();
                this.m[i][j] = new AtomicInteger();
                this.pValueSum[i][j] = new AtomicDouble();
                this.minorReadCountSumArr[i][j] = new AtomicLong();
                this.totalReadCountSumArr[i][j] = new AtomicLong();
            }
        }
    }

    @Override
    boolean callAndUpdate(int from, int to, int k, int n) {
        if (k == 0) {
            return false;
        }

        boolean pass = false;

        try {
            double p = Gamma.regularizedGammaP(k, n * seqErrorRate);

            pass = p < assemblerParameters.getPcrMinorTestPValue();

            m[from][to].incrementAndGet();
            pValueSum[from][to].addAndGet(2 * Math.min(p, 1 - p));

            if (pass) {
                m1[from][to].incrementAndGet();
                minorReadCountSumArr[from][to].addAndGet(k);
                totalReadCountSumArr[from][to].addAndGet(n);
            }
        } catch (MathException e) {
            e.printStackTrace();
        }

        return pass;
    }

    @Override
    PoissonTestMinorCaller combine(PoissonTestMinorCaller other) {
        PoissonTestMinorCaller poissonTestMinorCaller = new PoissonTestMinorCaller(this.assemblerParameters,
                other.preprocessorParameters);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                poissonTestMinorCaller.m1[i][j].addAndGet(this.getM1(i, j) + other.getM1(i, j));
                poissonTestMinorCaller.m[i][j].addAndGet(this.getM(i, j) + other.getM(i, j));
                poissonTestMinorCaller.pValueSum[i][j].addAndGet(this.getPValueSum(i, j) + other.getPValueSum(i, j));
                poissonTestMinorCaller.minorReadCountSumArr[i][j].addAndGet(minorReadCountSumArr[i][j].get());
                poissonTestMinorCaller.totalReadCountSumArr[i][j].addAndGet(totalReadCountSumArr[i][j].get());
            }
        }

        return poissonTestMinorCaller;
    }

    @Override
    public double getReadFractionForCalledMinors(int from, int to) {
        return minorReadCountSumArr[from][to].get() / (double) totalReadCountSumArr[from][to].get();
    }

    @Override
    public double computeFdr(int from, int to) {
        // FDR estimation according to
        // http://bioinformatics.oxfordjournals.org/content/22/16/1979.full.pdf+html
        int mm = getM(from, to), mm1 = getM1(from, to);

        if (mm1 == 0) {
            return 0.0;
        }

        double pavg = getPValueSum(from, to) / mm;

        return assemblerParameters.getPcrMinorTestPValue() * Math.min(1.0, 2.0 * pavg) * mm / (double) mm1;
    }

    private int getM(int from, int to) {
        return m[from][to].get();
    }

    private int getM1(int from, int to) {
        return m1[from][to].get();
    }

    private double getPValueSum(int from, int to) {
        return pValueSum[from][to].get();
    }

    @Override
    public String getHeader() {
        return "from\tto\tm1\tm\tfdr";
    }

    @Override
    public String getBody() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                sb.append(NucleotideAlphabet.INSTANCE.symbolFromCode((byte) i))
                        .append("\t")
                        .append(NucleotideAlphabet.INSTANCE.symbolFromCode((byte) j))
                        .append("\t")
                        .append(getM1(i, j))
                        .append("\t")
                        .append(getM(i, j))
                        .append("\t")
                        .append(computeFdr(i, j))
                        .append("\n");
            }
        }
        return sb.toString();
    }
}
