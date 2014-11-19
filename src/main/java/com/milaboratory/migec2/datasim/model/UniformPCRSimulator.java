package com.milaboratory.migec2.datasim.model;

import org.apache.commons.math.MathException;
import org.apache.commons.math.random.MersenneTwister;
import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.random.RandomGenerator;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

public class UniformPCRSimulator implements Callable<PCRResult> {
    private final static AtomicLong seed = new AtomicLong(System.currentTimeMillis());
    private final static int stochasticThreshold = 64, gaussianApproxThreshold = 10;
    private final int numberOfCycles;
    private final long[] nucleotideCounts;

    private final double Q, Mu, MuQ;

    public UniformPCRSimulator(double mu, double lambda,
                               int startingMolecules, int numberOfCycles) {
        this.nucleotideCounts = new long[]{startingMolecules, 0, 0, 0};
        this.numberOfCycles = numberOfCycles;

        this.Q = (1.0 - lambda) / 2.0; // template loss prob
        this.Mu = mu / (1.0 - Q); // prob of error scaled to prob of PCR propagation
        this.MuQ = Mu + Q;
    }

    private void simulatePCRCycle(RandomDataImpl rdi, RandomGenerator rnd) throws MathException {
        final int[] delta = new int[]{0, 0, 0, 0};
        for (int i = 0; i < 4; i++) {
            long nucleotideCount = nucleotideCounts[i];
            if (nucleotideCount <= stochasticThreshold) {
                for (int n = 0; n < nucleotideCount; n++) {
                    double p = rnd.nextDouble();
                    if (p <= Mu) {
                        // Error
                        int i2 = rnd.nextInt(3);
                        delta[(i + i2 + 1) % 4]++;
                    } else if (p <= MuQ) {
                        // PCR failed
                        delta[i]--;
                    } else {
                        // Normal PCR
                        delta[i]++;
                    }
                }
            } else {
                for (int i2 = 0; i2 < 3; i2++) {
                    long countU = sample(rdi, nucleotideCount, Mu);
                    delta[(i + i2) % 4] += countU;
                    delta[i] -= countU;
                }
                long countQ = sample(rdi, nucleotideCount, Q);
                delta[i] += nucleotideCount - 2 * countQ;
            }
        }

        for (int i = 0; i < 4; i++) {
            nucleotideCounts[i] += delta[i];
            nucleotideCounts[i] = Math.max(0, nucleotideCounts[i]);
        }
    }

    private long sample(RandomDataImpl rdi, long N, double p) throws MathException {
        if (N * p < gaussianApproxThreshold)
            return rdi.nextBinomial((int) N, p);
        else {
            double mean = N * p, std = Math.sqrt(mean * (1.0 - p));
            return (long) rdi.nextGaussian(mean, std);
        }
    }

    @Override
    public PCRResult call() throws Exception {
        // Move those to call, otherwise we'll have an out-of-memory problem
        final RandomGenerator rnd = new MersenneTwister(seed.incrementAndGet());
        final RandomDataImpl rdi = new RandomDataImpl(rnd);

        for (int n = 0; n < numberOfCycles; n++)
            simulatePCRCycle(rdi, rnd);

        return new PCRResult(nucleotideCounts, (byte) 0);
    }
}