/*
 * Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)
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
package com.milaboratory.oncomigec.misc;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.quality.QualityFormat;
import com.milaboratory.core.sequence.quality.SequenceQualityPhred;
import com.milaboratory.oncomigec.FastTests;
import com.milaboratory.oncomigec.PercentRangeAssertion;
import com.milaboratory.oncomigec.generators.MutationGenerator;
import com.milaboratory.oncomigec.misc.Overlapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;

import static com.milaboratory.oncomigec.generators.RandomUtil.randomSequence;


public class OverlapTest {
    private final int totalOverlapSize = 15, seqSize = 75, totalRuns = 10000;

    @Test
    @Category(FastTests.class)
    public void positiveTest() {
        positiveTest(MutationGenerator.DEFAULT,
                PercentRangeAssertion.createLowerBound("Correct overlap rate", "Overlap with indels", 70),
                PercentRangeAssertion.createUpperBound("Incorrect overlap rate", "Overlap with indels", 30));
        positiveTest(MutationGenerator.NO_INDEL,
                PercentRangeAssertion.createLowerBound("Correct overlap rate", "Overlap without indels", 90),
                PercentRangeAssertion.createUpperBound("Incorrect overlap rate", "Overlap without indels", 10));
    }

    public void positiveTest(MutationGenerator mutationModel,
                             PercentRangeAssertion correctOverlapRate,
                             PercentRangeAssertion incorrectOverlapRate) {
        Overlapper ro = new Overlapper();

        int overlapped = 0, overlappedCorrectly = 0, total = totalRuns;
        for (int i = 0; i < total; i++) {
            NucleotideSequence s1orig = randomSequence(seqSize - totalOverlapSize),
                    s2 = randomSequence(seqSize);

            NucleotideSequence s1 = s1orig.concatenate(s2.getRange(0, totalOverlapSize));

            int[] mutations1 = mutationModel.nextMutations(s1),
                    mutations2 = mutationModel.nextMutations(s2);

            s1 = Mutations.mutate(s1, mutations1);
            s2 = Mutations.mutate(s2, mutations2);

            NucleotideSequence s12 = Mutations.mutate(s1orig,
                    Mutations.extractMutationsForRange(mutations1, 0,
                            seqSize - totalOverlapSize)
            ).concatenate(s2);

            byte[] q1 = new byte[s1.size()], q2 = new byte[s2.size()];
            Arrays.fill(q1, (byte) (40 + 33));
            Arrays.fill(q2, (byte) (2 + 33));

            NucleotideSQPair r1 = new NucleotideSQPair(s1, SequenceQualityPhred.create(QualityFormat.Phred33, q1, true)),
                    r2 = new NucleotideSQPair(s2, SequenceQualityPhred.create(QualityFormat.Phred33, q2, true));

            Overlapper.OverlapResult result = ro.overlap(r1, r2);

            if (result.overlapped()) {
                overlapped++;
                NucleotideSequence overlappedSequence = result.getSQPair().getSequence();

                if (overlappedSequence.equals(s12))
                    overlappedCorrectly++;
            }
        }

        correctOverlapRate.assertInRange(overlappedCorrectly, total);
        incorrectOverlapRate.assertInRange(overlapped - overlappedCorrectly, overlapped);
    }

    @Test
    @Category(FastTests.class)
    public void readThroughTest() {
        Overlapper ro = new Overlapper();

        int correctOverlap = 0, correctOffset = 0,
                readThroughIdentified = 0,
                total = totalRuns;
        int barcodeOffset = (int) (0.1 * seqSize), overhangSize = (int) (0.05 * seqSize);

        for (int i = 0; i < total; i++) {
            NucleotideSequence fragment = randomSequence(2 * seqSize),
                    barcode = randomSequence(barcodeOffset),
                    overhang = randomSequence(overhangSize);

            NucleotideSequence s1 = fragment.concatenate(overhang),
                    s2 = barcode.concatenate(fragment), s12 = barcode.concatenate(fragment).concatenate(overhang);

            NucleotideSQPair r1 = new NucleotideSQPair(s1),
                    r2 = new NucleotideSQPair(s2);

            Overlapper.OverlapResult result = ro.overlap(r1, r2);

            if (result.overlapped()) {
                if (result.readThrough())
                    readThroughIdentified++;

                if (result.getSQPair().getSequence().equals(s12))
                    correctOverlap++;

                if (result.getOffset1() == barcodeOffset)
                    correctOffset++;
            }
        }

        PercentRangeAssertion.createLowerBound("Correct overlap", "Read-through", 95).
                assertInRange(correctOverlap, total);

        Assert.assertEquals("Correct offset identified", correctOverlap, correctOffset);
        Assert.assertEquals("Read-through identified", correctOverlap, readThroughIdentified);
    }

    @Test
    @Category(FastTests.class)
    public void negativeTest() throws Exception {
        Overlapper ro = new Overlapper();

        int overlapped = totalRuns, total = overlapped;
        for (int i = 0; i < total; i++) {
            NucleotideSequence s1 = randomSequence(seqSize), s2 = randomSequence(seqSize);

            byte[] q1 = new byte[s1.size()], q2 = new byte[s2.size()];
            Arrays.fill(q1, (byte) (40 + 33));
            Arrays.fill(q2, (byte) (2 + 33));

            NucleotideSQPair r1 = new NucleotideSQPair(s1, SequenceQualityPhred.create(QualityFormat.Phred33, q1, true)),
                    r2 = new NucleotideSQPair(s2, SequenceQualityPhred.create(QualityFormat.Phred33, q2, true));

            Overlapper.OverlapResult result = ro.overlap(r1, r2);

            if (!result.overlapped()) {
                overlapped--;
            }
        }

        PercentRangeAssertion.createUpperBound("Overlap identified", "Negative test", 5).
                assertInRange(overlapped, total);
    }
}
