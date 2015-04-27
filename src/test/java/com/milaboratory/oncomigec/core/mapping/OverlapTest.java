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
package com.milaboratory.oncomigec.core.mapping;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.quality.QualityFormat;
import com.milaboratory.core.sequence.quality.SequenceQualityPhred;
import com.milaboratory.oncomigec.PercentRangeAssertion;
import com.milaboratory.oncomigec.generators.GeneratorMutationModel;
import com.milaboratory.oncomigec.misc.Overlapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static com.milaboratory.oncomigec.misc.Util.randomSequence;


public class OverlapTest {
    private final int TOTAL_OVERLAP_SIZE = 15, SEQ_LENGTH = 75, TOTAL_ATTEMPTS = 10000;

    @Test
    public void positiveTest() {
        positiveTest(GeneratorMutationModel.DEFAULT,
                PercentRangeAssertion.createLowerBound("Correct overlap rate", "Overlap with indels", 70),
                PercentRangeAssertion.createUpperBound("Incorrect overlap rate", "Overlap with indels", 30));
        positiveTest(GeneratorMutationModel.NO_INDEL,
                PercentRangeAssertion.createLowerBound("Correct overlap rate", "Overlap without indels", 90),
                PercentRangeAssertion.createUpperBound("Incorrect overlap rate", "Overlap without indels", 10));
    }

    public void positiveTest(GeneratorMutationModel mutationModel,
                             PercentRangeAssertion correctOverlapRate,
                             PercentRangeAssertion incorrectOverlapRate) {
        Overlapper ro = new Overlapper();

        int overlapped = 0, overlappedCorrectly = 0, total = TOTAL_ATTEMPTS;
        for (int i = 0; i < total; i++) {
            NucleotideSequence s1orig = randomSequence(SEQ_LENGTH - TOTAL_OVERLAP_SIZE),
                    s2 = randomSequence(SEQ_LENGTH);

            NucleotideSequence s1 = s1orig.concatenate(s2.getRange(0, TOTAL_OVERLAP_SIZE));

            int[] mutations1 = mutationModel.nextMutations(s1),
                    mutations2 = mutationModel.nextMutations(s2);

            s1 = Mutations.mutate(s1, mutations1);
            s2 = Mutations.mutate(s2, mutations2);

            NucleotideSequence s12 = Mutations.mutate(s1orig,
                    Mutations.extractMutationsForRange(mutations1, 0,
                            SEQ_LENGTH - TOTAL_OVERLAP_SIZE)
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
    public void readThroughTest() {
        Overlapper ro = new Overlapper();

        int correctOverlap = 0, correctOffset = 0,
                readThroughIdentified = 0,
                total = TOTAL_ATTEMPTS;
        int barcodeOffset = (int) (0.1 * SEQ_LENGTH), overhangSize = (int) (0.05 * SEQ_LENGTH);

        for (int i = 0; i < total; i++) {
            NucleotideSequence fragment = randomSequence(2 * SEQ_LENGTH),
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
    public void negativeTest() throws Exception {
        Overlapper ro = new Overlapper();

        int overlapped = TOTAL_ATTEMPTS, total = overlapped;
        for (int i = 0; i < total; i++) {
            NucleotideSequence s1 = randomSequence(SEQ_LENGTH), s2 = randomSequence(SEQ_LENGTH);

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
