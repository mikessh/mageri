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

package com.milaboratory.oncomigec.core.assemble;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.BinomialDistribution;
import org.apache.commons.math.distribution.BinomialDistributionImpl;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

public class MinorEvaluatorTest {
    @Ignore
    @Test
    public void inverseCumulativeTest() throws MathException {
        // Make sure we understood everything correctly
        BinomialDistribution binomialDistribution;
        double p = 0.001, pVal = 0.95;

        binomialDistribution = new BinomialDistributionImpl(1, p);
        System.out.println(binomialDistribution.inverseCumulativeProbability(pVal));


        binomialDistribution = new BinomialDistributionImpl(11, p);
        System.out.println(binomialDistribution.inverseCumulativeProbability(pVal));
        // -1 - should use Max(0, x)

        binomialDistribution = new BinomialDistributionImpl(64, p);
        int x = binomialDistribution.inverseCumulativeProbability(pVal);
        System.out.println(x);
        System.out.println(1 - binomialDistribution.cumulativeProbability(x));
        System.out.println(1 - binomialDistribution.cumulativeProbability(x + 1));
        // should use > sign, not >=

        binomialDistribution = new BinomialDistributionImpl(128, p);
        System.out.println(binomialDistribution.inverseCumulativeProbability(pVal));

        binomialDistribution = new BinomialDistributionImpl(256, p);
        System.out.println(binomialDistribution.inverseCumulativeProbability(pVal));

        binomialDistribution = new BinomialDistributionImpl(1000, p);
        System.out.println(binomialDistribution.inverseCumulativeProbability(pVal));

        binomialDistribution = new BinomialDistributionImpl(10000, p);
        System.out.println(binomialDistribution.inverseCumulativeProbability(pVal));
    }

    @Test
    public void test() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        SAssembler assembler = new SAssembler();

        Assert.assertFalse(assembler.new MinorEvaluator(10).isGood(0));

        Assert.assertTrue(assembler.new MinorEvaluator(10).isGood(1));
        Assert.assertTrue(assembler.new MinorEvaluator(100).isGood(1));

        Assert.assertFalse(assembler.new MinorEvaluator(1000).isGood(1));
        Assert.assertTrue(assembler.new MinorEvaluator(1000).isGood(3));

        Assert.assertFalse(assembler.new MinorEvaluator(10000).isGood(1));
        Assert.assertTrue(assembler.new MinorEvaluator(10000).isGood(15));
    }
}
