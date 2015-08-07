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

package com.milaboratory.mageri.core.input;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.mageri.FastTests;
import com.milaboratory.mageri.TestUtil;
import org.apache.commons.math.random.MersenneTwister;
import org.apache.commons.math.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;

import static com.milaboratory.mageri.generators.RandomUtil.randomSequence;


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
