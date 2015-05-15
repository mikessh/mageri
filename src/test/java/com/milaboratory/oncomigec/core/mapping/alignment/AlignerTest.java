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
package com.milaboratory.oncomigec.core.mapping.alignment;

import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.FastTests;
import com.milaboratory.oncomigec.PercentRangeAssertion;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.generators.MutationGenerator;
import com.milaboratory.oncomigec.generators.RandomReferenceGenerator;
import com.milaboratory.oncomigec.generators.ReferenceParentChildPair;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class AlignerTest {
    private final int mapqThreshold = 20;

    @Test
    @Category(FastTests.class)
    public void falsePositiveTest() {
        int nReferences = 1000, nRepetitions = 10000;
        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator();
        randomReferenceGenerator.setReferenceSizeMin(75);
        randomReferenceGenerator.setReferenceSizeMax(75);

        ReferenceLibrary referenceLibrary = randomReferenceGenerator.nextReferenceLibrary(nReferences);

        AlignerFactory alignerFactory = new ExtendedKmerAlignerFactory(referenceLibrary);
        Aligner aligner = alignerFactory.create();


        int nAligned = 0, nMapqFitlered = 0, nEvaluatorFiltered = 0;

        for (int i = 0; i < nRepetitions; i++) {
            NucleotideSequence query = randomReferenceGenerator.nextReferenceSequence();

            AlignmentResult alignmentResult = aligner.align(query);

            if (alignmentResult != null) {
                nAligned++;

                if (!alignmentResult.isGood()) {
                    nEvaluatorFiltered++;
                }
                if (alignmentResult.getScore() < mapqThreshold) {
                    nMapqFitlered++;
                }
            }
        }

        PercentRangeAssertion.createUpperBound("Kmer alignment", "Aligner false positive test", 90).assertInRange(nAligned, nRepetitions);
        PercentRangeAssertion.createLowerBound("MAPQ threshold filtered filtering", "Aligner false positive test", 95).assertInRange(nMapqFitlered, nAligned);
        PercentRangeAssertion.createLowerBound("Alignment evaluator filtering", "Aligner false positive test", 90).assertInRange(nEvaluatorFiltered, nAligned);
    }

    @Test
    @Category(FastTests.class)
    public void test() {
        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator(),
                randomReferenceGenerator1 = new RandomReferenceGenerator();

        randomReferenceGenerator.setMutationGenerator(MutationGenerator.DEFAULT.multiply(10));
        randomReferenceGenerator1.setMutationGenerator(MutationGenerator.DEFAULT.multiply(10));

        int nReferences = 500, nRepetitions1 = 100, nRepetitions2 = 100;

        int nAligned = 0, nFailed = 0,
                TP = 0, TN = 0, FP = 0, FN = 0;

        for (int i = 0; i < nRepetitions1; i++) {
            ReferenceLibrary referenceLibrary = randomReferenceGenerator.nextHomologousReferenceLibrary(nReferences);

            AlignerFactory alignerFactory = new ExtendedKmerAlignerFactory(referenceLibrary);
            Aligner aligner = alignerFactory.create();

            for (int j = 0; j < nRepetitions2; j++) {
                ReferenceParentChildPair parentChildPair =
                        randomReferenceGenerator1.nextParentChildPair(referenceLibrary);

                NucleotideSequence subject = parentChildPair.getParentSequence(),
                        query = parentChildPair.getChildSequence();

                AlignmentResult alignmentResult = aligner.align(parentChildPair.getChildSequence());

                if (alignmentResult == null) {
                    nFailed++;
                } else {
                    nAligned++;
                    LocalAlignment alignment = alignmentResult.getAlignment();

                    boolean correctAlignment = false;

                    if (alignmentResult.getReference().equals(parentChildPair.getParentReference())) {
                        subject = subject.getRange(alignment.getSequence1Range());
                        query = query.getRange(alignment.getSequence2Range());

                        correctAlignment = Mutations.mutate(subject, alignment.getMutations()).equals(query);
                    }

                    if (correctAlignment) {
                        if (alignmentResult.isGood()) {
                            TP++;
                        } else {
                            FN++;
                        }
                    } else {
                        if (alignmentResult.isGood() && alignmentResult.getScore() > mapqThreshold) {
                            FP++;
                        } else {
                            TN++;
                        }
                    }
                }
            }
        }

        PercentRangeAssertion.createLowerBound("Alignment rate", "Full aligner test", 95).assertInRange(nAligned, nAligned + nFailed);
        PercentRangeAssertion.createLowerBound("Specificity", "Full aligner test", 95).assertInRange(TN, FP + TN);
        PercentRangeAssertion.createLowerBound("Sensitivity", "Full aligner test", 95).assertInRange(TP, TP + FN);
    }
}
