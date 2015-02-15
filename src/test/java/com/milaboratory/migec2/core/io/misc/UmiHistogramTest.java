package com.milaboratory.migec2.core.io.misc;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.migec2.util.Util;
import org.apache.commons.math.random.MersenneTwister;
import org.apache.commons.math.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;


public class UmiHistogramTest {
    private static RandomGenerator randomGenerator = new MersenneTwister(51102);

    @Test
    public void test() {
        UmiHistogram histogram = new UmiHistogram();

        // correct
        for (int i = 0; i < 1000; i++) {
            int migSize = (int) Math.pow(2, randomGenerator.nextGaussian() + 5);
            NucleotideSequence umi = Util.randomSequence(12);
            for (int k = 0; k < migSize; k++)
                histogram.update(umi);
        }

        // errors
        for (int i = 0; i < 10000; i++) {
            int migSize = (int) Math.pow(2, randomGenerator.nextGaussian() * 0.5);
            NucleotideSequence umi = Util.randomSequence(12);
            for (int k = 0; k < migSize; k++)
                histogram.update(umi);
        }

        histogram.calculateHistogram();
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
}
