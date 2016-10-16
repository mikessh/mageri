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

import com.antigenomics.mageri.FastTests;
import com.antigenomics.mageri.core.input.PreprocessorParameters;
import com.antigenomics.mageri.core.variant.model.MinorBasedErrorModel;
import org.apache.commons.math.MathException;
import org.apache.commons.math.random.JDKRandomGenerator;
import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class MinorCallerTest {
    private final RandomData rnd;

    public MinorCallerTest() {
        RandomGenerator rng = new JDKRandomGenerator();
        rng.setSeed(480011L);
        rnd = new RandomDataImpl(rng);
    }

    @Test
    @Category(FastTests.class)
    public void simulationTest() throws MathException {
        simulationTest(20, 0.8, (byte) 30, 1e-5, 100, 10000, 100);
        simulationTest(20, 0.8, (byte) 30, 5e-6, 100, 10000, 100);
        simulationTest(20, 0.8, (byte) 30, 1e-6, 100, 10000, 100);

        simulationTest(20, 0.8, (byte) 25, 1e-5, 100, 10000, 100);
        simulationTest(20, 0.8, (byte) 20, 1e-5, 100, 10000, 100);

        simulationTest(20, 0.8, (byte) 30, 1e-5, 10, 10000, 100);
        simulationTest(20, 0.8, (byte) 20, 1e-5, 10, 10000, 100);
    }

    private MinorCaller simulationTest(int nCycles, double lambda,
                                       byte seqQual,
                                       double pcrErrorRate,
                                       int migSize, int nMigs, int readLength) throws MathException {
        System.out.println("---\nRunning for parameters: " +
                "cycles=" + nCycles +
                ";efficiency=" + (1.0 + lambda) +
                ";quality=" + seqQual +
                ";pcr_err=" + pcrErrorRate +
                ";mig_sz=" + migSize +
                ";n_migs=" + nMigs +
                ";read_len=" + readLength);

        MinorCaller minorCaller = new PoissonTestMinorCaller(AssemblerParameters.DEFAULT,
                PreprocessorParameters.DEFAULT.withGoodQualityThreshold(seqQual));

        int totalPCRMinors = 0, calledTruePCRMinors = 0, calledFalsePCRMinors = 0;
        double errorRateMean = 0;

        double expectedSeqErrors = migSize * Math.pow(10.0, -seqQual / 10.0);

        for (int i = 0; i < nMigs * readLength; i++) {
            double erroneousTemplateFraction = 0;
            for (int j = 0; j < nCycles; j++) {
                erroneousTemplateFraction += pcrErrorRate;

                int nTemplates = (int) Math.pow(1.0 + lambda, j);

                if (rnd.nextUniform(0.0, 1.0) < pcrErrorRate * nTemplates) {
                    erroneousTemplateFraction += 1.0 / nTemplates;
                }
            }

            erroneousTemplateFraction = Math.min(1.0, erroneousTemplateFraction);

            errorRateMean += erroneousTemplateFraction;

            int pcrErrorSize = (int) rnd.nextPoisson(erroneousTemplateFraction * migSize);

            boolean truePcrMinor = pcrErrorSize >= 1;

            if (truePcrMinor) {
                totalPCRMinors++;
            }

            int sequencingErrorSize = (int) rnd.nextPoisson(expectedSeqErrors);

            if (minorCaller.callAndUpdate(0, 0, pcrErrorSize + sequencingErrorSize, migSize)) {
                if (truePcrMinor) {
                    calledTruePCRMinors++;
                } else {
                    calledFalsePCRMinors++;
                }
            }
        }

        System.out.println("Total PCR minors = " + totalPCRMinors);
        System.out.println("Called true PCR minors = " + calledTruePCRMinors);
        System.out.println("Called false PCR minors = " + calledFalsePCRMinors);
        System.out.println("FDR estimate = " + minorCaller.computeFdr(0, 0));
        double errorRateExp = errorRateMean / (nMigs * readLength) / nCycles / lambda * (1.0 + lambda);
        // Will differ from the original rate as the model is quite crude
        System.out.println("Error rate exp = " + errorRateExp);
        double minorRate = (calledTruePCRMinors + calledFalsePCRMinors) / (double) (nMigs * readLength);
        double errorRateEst = MinorBasedErrorModel.computeBaseErrorRateEstimate(minorRate,
                minorCaller.computeFdr(0, 0), minorCaller.getReadFractionForCalledMinors(0, 0), lambda);
        System.out.println("Error rate est = " + errorRateEst);

        Assert.assertTrue("No more than order of magnitude difference between true PCR " +
                        "error rate and its estimate",
                Math.abs(Math.log10(errorRateEst) - Math.log10(pcrErrorRate)) <= 1.0);

        return minorCaller;
    }
}
