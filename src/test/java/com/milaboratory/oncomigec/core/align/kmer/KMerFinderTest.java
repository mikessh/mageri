package com.milaboratory.oncomigec.core.align.kmer;

import com.milaboratory.oncomigec.core.align.reference.ReferenceLibrary;
import com.milaboratory.oncomigec.util.testing.IntRange;
import com.milaboratory.oncomigec.util.testing.PercentRange;
import com.milaboratory.oncomigec.util.testing.generators.GeneratorMutationModel;
import com.milaboratory.oncomigec.util.testing.generators.RandomReferenceGenerator;
import org.junit.Test;

import java.util.Arrays;

public class KMerFinderTest {
    int nReferences = 100, nRepetitions1 = 100, nRepetitions2 = 100, k = 11;
    int nBins = 100;

    @Test
    public void kmerFinderStressTest() {
        System.out.println("Generating real-world-size reference library");
        // human exome
        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator(GeneratorMutationModel.DEFAULT,
                150, 150);
        ReferenceLibrary referenceLibrary = randomReferenceGenerator.nextReferenceLibrary(233785);

        System.out.println("Building K-mer finder");
        long now = System.nanoTime(); // todo: timer
        new KMerFinder(referenceLibrary, k);
        long elpased = System.nanoTime() - now;
        IntRange intRange = IntRange.createUpperBound("Time elpased, s", "KmerFinder-BuildHash", 60);
        intRange.assertInRange((int) (elpased / 1_000_000_000L));
    }

    @Test
    public void kmerFinderHitTest() {
        kmerFinderHitTest(false, PercentRange.createLowerBound("CorrectHits", "KmerFinder-NonHomologous", 99));
        kmerFinderHitTest(true, PercentRange.createLowerBound("CorrectHits", "KmerFinder-Homologous", 70));
    }

    private void kmerFinderHitTest(boolean homologous, PercentRange assertRange) {
        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator();

        int nCorrect = 0;
        int[] correctInformationHistogram = new int[nBins], incorrectInformationHistogram = new int[nBins];

        for (int i = 0; i < nRepetitions1; i++) {
            ReferenceLibrary referenceLibrary = homologous ?
                    randomReferenceGenerator.nextHomologousReferenceLibrary(nReferences) :
                    randomReferenceGenerator.nextReferenceLibrary(nReferences);
            KMerFinder kMerFinder = new KMerFinder(referenceLibrary, k);

            for (int j = 0; j < nRepetitions2; j++) {
                RandomReferenceGenerator.ParentChildPair parentChildPair =
                        randomReferenceGenerator.nextParentChildPair(referenceLibrary);

                KMerFinderResult result = kMerFinder.find(parentChildPair.getChildSequence());

                if (result.getBestHit().equals(parentChildPair.getParentReference())) {
                    nCorrect++;
                    correctInformationHistogram[getBin(result.getInformation())]++;
                } else {
                    incorrectInformationHistogram[getBin(result.getInformation())]++;
                }
            }
        }

        assertRange.assertInRange(nCorrect, nRepetitions1 * nRepetitions2);
        System.out.println("CorrectInformationHistogram=" + Arrays.toString(correctInformationHistogram));
        System.out.println("IncorrectInformationHistogram=" + Arrays.toString(incorrectInformationHistogram));
    }

    private int getBin(double value) {
        return Math.min(nBins - 1, (int) (value * nBins));
    }
}
