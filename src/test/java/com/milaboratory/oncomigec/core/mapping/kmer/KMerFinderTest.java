package com.milaboratory.oncomigec.core.mapping.kmer;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.IntRangeAssertion;
import com.milaboratory.oncomigec.PercentRangeAssertion;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.generators.RandomReferenceGenerator;
import com.milaboratory.oncomigec.generators.ReferenceParentChildPair;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.junit.Ignore;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class KMerFinderTest {
    int k = 11;

    @Ignore("Too heavy")
    @Test
    public void kmerFinderStressTest() {
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
        KMerFinder kMerFinder = new KMerFinder(referenceLibrary, k);
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
    public void kmerFinderHitTest() {
        kmerFinderHitTest(false, PercentRangeAssertion.createLowerBound("CorrectHits", "KmerFinder-NonHomologous", 99));
        kmerFinderHitTest(true, PercentRangeAssertion.createLowerBound("CorrectHits", "KmerFinder-Homologous", 90));
    }

    private void kmerFinderHitTest(boolean homologous, PercentRangeAssertion assertRange) {
        int nReferences = homologous ? 50 : 100, nRepetitions1 = 100, nRepetitions2 = 100;

        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator();

        int nCorrect = 0, nFailed = 0;
        DescriptiveStatistics correctInformation = new DescriptiveStatistics(),
                incorrectInformation = new DescriptiveStatistics(),
                correctMapq = new DescriptiveStatistics(),
                incorrectMapq = new DescriptiveStatistics();

        for (int i = 0; i < nRepetitions1; i++) {
            ReferenceLibrary referenceLibrary = homologous ?
                    randomReferenceGenerator.nextHomologousReferenceLibrary(nReferences) :
                    randomReferenceGenerator.nextReferenceLibrary(nReferences);
            KMerFinder kMerFinder = new KMerFinder(referenceLibrary, k);

            for (int j = 0; j < nRepetitions2; j++) {
                ReferenceParentChildPair parentChildPair =
                        randomReferenceGenerator.nextParentChildPair(referenceLibrary);

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

        assertRange.assertInRange(nCorrect, nRepetitions1 * nRepetitions2);

        System.out.println("Correct reference:");
        System.out.println("Information=" + correctInformation.getMean() + "±" + correctInformation.getStandardDeviation());
        System.out.println("MAPQ=" + correctMapq.getMean() + "±" + correctMapq.getStandardDeviation());
        System.out.println();
        System.out.println("Incorrect reference:");
        System.out.println("Information=" + incorrectInformation.getMean() + "±" + incorrectInformation.getStandardDeviation());
        System.out.println("MAPQ=" + incorrectMapq.getMean() + "±" + incorrectMapq.getStandardDeviation());
        System.out.println();
    }
}
