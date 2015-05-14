package com.milaboratory.oncomigec.core.mapping.kmer;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.*;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.generators.MutationGenerator;
import com.milaboratory.oncomigec.generators.RandomReferenceGenerator;
import com.milaboratory.oncomigec.generators.ReferenceParentChildPair;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.LinkedList;
import java.util.List;

public class KMerFinderTest {
    @Test
    @Category(PerformanceTests.class)
    public void stressTest() {
        int referenceLibrarySize = 233785, benchmarkSetSize = 100000, exonMedianSize = 150;
        long now, elapsed;

        System.out.println("Generating real-world-size reference library, n=" + referenceLibrarySize);
        // human exome
        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator();
        randomReferenceGenerator.setReferenceSizeMin(exonMedianSize);
        randomReferenceGenerator.setReferenceSizeMax(exonMedianSize);
        ReferenceLibrary referenceLibrary = randomReferenceGenerator.nextReferenceLibrary(referenceLibrarySize);

        System.out.println("Building K-mer finder");
        now = System.nanoTime(); // todo: timer
        KMerFinder kMerFinder = new KMerFinder(referenceLibrary);
        elapsed = System.nanoTime() - now;
        IntRangeAssertion.createUpperBound("Time elpased, s", "KmerFinder: building kmer hash", 60).
                assertInRange((int) (elapsed / 1_000_000_000L));

        System.out.println("Preparing references to align");
        List<NucleotideSequence> sequenceList = new LinkedList<>();

        for (int i = 0; i < benchmarkSetSize; i++) {
            sequenceList.add(randomReferenceGenerator.nextMutatedReferenceSequence(referenceLibrary));
        }

        System.out.println("Mapping n=" + benchmarkSetSize + " reads");
        now = System.nanoTime();
        for (NucleotideSequence sequence : sequenceList) {
            kMerFinder.find(sequence);
        }
        elapsed = System.nanoTime() - now;
        IntRangeAssertion.createUpperBound("Time elpased, s", "KmerFinder: mapping", 60).
                assertInRange((int) (elapsed / 1_000_000_000L));
    }

    @Test
    @Category(FastTests.class)
    public void hitTest() {
        String model, range1Name = "Correct hits percent", range2Name = "Mean MAPQ score";

        model = "KmerFinder: Homologous references";
        hitTest(50, 1.0, 1.0,
                PercentRangeAssertion.createLowerBound(range1Name, model, 95),
                DoubleRangeAssertion.createLowerBound(range2Name, model, 15));

        model = "KmerFinder: Less-homologous references";
        hitTest(100, 10.0, 1.0,
                PercentRangeAssertion.createLowerBound(range1Name, model, 99),
                DoubleRangeAssertion.createLowerBound(range2Name, model, 25));

        model = "KmerFinder: Non-homologous references, more errors";
        hitTest(500, 100.0, 3.0,
                PercentRangeAssertion.createLowerBound(range1Name, model, 99),
                DoubleRangeAssertion.createLowerBound(range2Name, model, 35));

        model = "KmerFinder: Single reference, extreme errors";
        hitTest(1, 1.0, 10.0,
                PercentRangeAssertion.createLowerBound(range1Name, model, 95),
                DoubleRangeAssertion.createLowerBound(range2Name, model, 5));
    }

    private void hitTest(int nReferences,
                         double nonHomologyMultiplier, double errorMultiplier,
                         PercentRangeAssertion correctRateRange,
                         DoubleRangeAssertion mapqRange) {
        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator(), randomReferenceGenerator1 = new RandomReferenceGenerator();

        randomReferenceGenerator.setMutationGenerator(MutationGenerator.DEFAULT.multiply(nonHomologyMultiplier));
        randomReferenceGenerator1.setMutationGenerator(MutationGenerator.DEFAULT.multiply(errorMultiplier));

        int nRepetitions1 = 100, nRepetitions2 = 100;

        int nCorrect = 0, nFailed = 0;
        DescriptiveStatistics correctInformation = new DescriptiveStatistics(),
                incorrectInformation = new DescriptiveStatistics(),
                correctMapq = new DescriptiveStatistics(),
                incorrectMapq = new DescriptiveStatistics();

        for (int i = 0; i < nRepetitions1; i++) {
            ReferenceLibrary referenceLibrary = nonHomologyMultiplier > 100 ?
                    randomReferenceGenerator.nextReferenceLibrary(nReferences) :
                    randomReferenceGenerator.nextHomologousReferenceLibrary(nReferences);

            KMerFinder kMerFinder = new KMerFinder(referenceLibrary);

            for (int j = 0; j < nRepetitions2; j++) {
                ReferenceParentChildPair parentChildPair =
                        randomReferenceGenerator1.nextParentChildPair(referenceLibrary);

                KMerFinderResult result = kMerFinder.find(parentChildPair.getChildSequence());

                if (result == null) {
                    nFailed++;
                    continue;
                }

                if (result.getHit().equals(parentChildPair.getParentReference())) {
                    nCorrect++;
                    correctInformation.addValue(result.getInformation());
                    correctMapq.addValue(result.getScore());
                } else {
                    incorrectInformation.addValue(result.getInformation());
                    incorrectMapq.addValue(result.getScore());
                }
            }
        }

        System.out.println("Correct reference:");
        System.out.println("Information=" + correctInformation.getMean() + "±" + correctInformation.getStandardDeviation());
        System.out.println("MAPQ=" + correctMapq.getMean() + "±" + correctMapq.getStandardDeviation());
        System.out.println();
        System.out.println("Incorrect reference:");
        System.out.println("Information=" + incorrectInformation.getMean() + "±" + incorrectInformation.getStandardDeviation());
        System.out.println("MAPQ=" + incorrectMapq.getMean() + "±" + incorrectMapq.getStandardDeviation());
        System.out.println();

        correctRateRange.assertInRange(nCorrect, nRepetitions1 * nRepetitions2);
        mapqRange.assertInRange(correctMapq.getMean());
    }
}
