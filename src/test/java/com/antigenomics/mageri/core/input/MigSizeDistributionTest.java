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

import com.antigenomics.mageri.TestUtil;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.antigenomics.mageri.FastTests;
import org.apache.commons.math.random.MersenneTwister;
import org.apache.commons.math.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;

import static com.antigenomics.mageri.generators.RandomUtil.randomSequence;


public class MigSizeDistributionTest {
    private static RandomGenerator randomGenerator = new MersenneTwister(51102);

    private static MigSizeDistribution prepareHistogram() {
        MigSizeDistribution histogram = new MigSizeDistribution();

        // correct
        for (int i = 0; i < 1000; i++) {
            int migSize = (int) Math.pow(2, randomGenerator.nextGaussian() + 5);
            NucleotideSequence umi = randomSequence(12);
            for (int k = 0; k < migSize; k++)
                histogram.update(umi);
        }

        // errors
        for (int i = 0; i < 10000; i++) {
            int migSize = (int) Math.pow(2, randomGenerator.nextGaussian() * 0.5);
            NucleotideSequence umi = randomSequence(12);
            for (int k = 0; k < migSize; k++)
                histogram.update(umi);
        }

        histogram.calculateHistogram();

        return histogram;
    }

    @Test
    @Category(FastTests.class)
    public void test() {
        System.out.println("Running performance test for UmiHistogram");
        MigSizeDistribution histogram = prepareHistogram();

        System.out.println(histogram);

        int overseq = histogram.getMigSizeThreshold();
        System.out.println("Overseq = " + overseq);

        System.out.println("Reads dropped count with overseq = " +
                histogram.calculateReadsDropped(overseq) +
                " of total = " + histogram.getReadTotal());
        System.out.println("MIGs dropped count with overseq = " +
                histogram.calculateMigsDropped(overseq) +
                " of total = " + histogram.getMigsTotal());

        Assert.assertEquals("Correct overseq", 4, overseq);
    }

    @Test
    @Category(FastTests.class)
    public void serializationTest() throws IOException {
        System.out.println("Testing serialization test for UmiHistogram");
        MigSizeDistribution histogram = prepareHistogram();
        TestUtil.serializationCheck(histogram);
    }
}
