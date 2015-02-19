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
package com.milaboratory.oncomigec.preproc.misc;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.mutations.MutationModels;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.mutations.NucleotideMutationModel;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.quality.QualityFormat;
import com.milaboratory.core.sequence.quality.SequenceQualityPhred;
import com.milaboratory.oncomigec.core.io.misc.NucleotideSQPairTuple;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static com.milaboratory.oncomigec.util.Util.randomSequence;


public class OverlapTest {
    private final int TOTAL_OVERLAP_SIZE = 15, SEQ_LENGTH = 75, TOTAL_ATTEMPTS = 10000;
    private final double MIN_TP = 0.9, MAX_FP = 0.1;

    @Test
    public void positiveTest() throws Exception {
        ReadOverlapper ro = new ReadOverlapper();

        NucleotideMutationModel mutationModel = MutationModels.getEmpiricalNucleotideMutationModel().multiply(2.0);
        mutationModel.reseed(2106803L);
        int overlapped = 0, total = TOTAL_ATTEMPTS;
        for (int i = 0; i < total; i++) {
            NucleotideSequence s1 = randomSequence(SEQ_LENGTH - TOTAL_OVERLAP_SIZE), s2 = randomSequence(SEQ_LENGTH);
            s1 = s1.concatenate(s2.getRange(0, TOTAL_OVERLAP_SIZE));

            int[] mutations1 = Mutations.generateMutations(s1, mutationModel),
                    mutations2 = Mutations.generateMutations(s2, mutationModel);

            Mutations.mutate(s1, mutations1);
            Mutations.mutate(s2, mutations2);

            byte[] q1 = new byte[s1.size()], q2 = new byte[s2.size()];
            Arrays.fill(q1, (byte) (40 + 33));
            Arrays.fill(q2, (byte) (2 + 33));

            NucleotideSQPair r1 = new NucleotideSQPair(s1, SequenceQualityPhred.create(QualityFormat.Phred33, q1, true)),
                    r2 = new NucleotideSQPair(s2, SequenceQualityPhred.create(QualityFormat.Phred33, q2, true));

            NucleotideSQPairTuple r12 = ro.overlap(new NucleotideSQPairTuple(r1,
                    r2)).getReadPair();

            NucleotideSequence overlappedSequence = r12.getFirst().getSequence().
                    concatenate(r12.getSecond().getSequence());
            NucleotideSequence trueSequence = s1.concatenate(s2.getRange(TOTAL_OVERLAP_SIZE, SEQ_LENGTH));

            if (overlappedSequence.equals(trueSequence))
                overlapped++;
        }
        System.out.println("True positives: " + overlapped + " of " + total);
        Assert.assertTrue("TP > 99.9%", (double) overlapped / (double) total >= MIN_TP);
    }

    @Test
    public void readThroughTest() {
        ReadOverlapper ro = new ReadOverlapper();

        int overlappedT = 0, overlappedF = 0, total = TOTAL_ATTEMPTS;
        int barcodeOffset = (int) (0.1 * SEQ_LENGTH), overhangSize = (int) (0.05 * SEQ_LENGTH);

        for (int i = 0; i < total; i++) {
            NucleotideSequence fragment = randomSequence(2 * SEQ_LENGTH),
                    barcode = randomSequence(barcodeOffset),
                    overhang = randomSequence(overhangSize);

            NucleotideSequence s1 = fragment.concatenate(overhang),
                    s2 = barcode.concatenate(fragment);

            NucleotideSQPair r1 = new NucleotideSQPair(s1),
                    r2 = new NucleotideSQPair(s2);

            ReadOverlapper.OverlapResult o12T = ro.overlap(new NucleotideSQPairTuple(r1, r2), barcodeOffset),
                    o12F = ro.overlap(new NucleotideSQPairTuple(r1, r2));

            NucleotideSQPairTuple r12T = o12T.getReadPair();


            if (r12T.getFirst().getSequence().concatenate(r12T.getSecond().getSequence()).equals(fragment))
                overlappedT++;

            if (o12F.isOverlapped())
                overlappedF++;
        }

        System.out.println("True positives (read-through): " + overlappedT + " of " + total);
        Assert.assertTrue("TP > 99.9%", (double) overlappedT / (double) total >= MIN_TP);
        System.out.println("False positives (read-through): " + overlappedF + " of " + total);
        Assert.assertTrue("FP < 2%", (double) overlappedF / (double) total <= MAX_FP);
    }

    @Test
    public void negativeTest() throws Exception {
        ReadOverlapper ro = new ReadOverlapper();

        int overlapped = TOTAL_ATTEMPTS, total = overlapped;
        for (int i = 0; i < total; i++) {
            NucleotideSequence s1 = randomSequence(SEQ_LENGTH), s2 = randomSequence(SEQ_LENGTH);

            byte[] q1 = new byte[s1.size()], q2 = new byte[s2.size()];
            Arrays.fill(q1, (byte) (40 + 33));
            Arrays.fill(q2, (byte) (2 + 33));

            NucleotideSQPair r1 = new NucleotideSQPair(s1, SequenceQualityPhred.create(QualityFormat.Phred33, q1, true)),
                    r2 = new NucleotideSQPair(s2, SequenceQualityPhred.create(QualityFormat.Phred33, q2, true));

            NucleotideSQPairTuple r12 = ro.overlap(new NucleotideSQPairTuple(r1,
                    r2)).getReadPair();

            NucleotideSequence overlappedSequence = r12.getFirst().getSequence().
                    concatenate(r12.getSecond().getSequence());
            NucleotideSequence trueSequence = s1.concatenate(s2);

            if (overlappedSequence.equals(trueSequence))
                overlapped--;
        }
        System.out.println("False positives: " + overlapped + " of " + total);
        Assert.assertTrue("FP < 2%", (double) overlapped / (double) total <= MAX_FP);
    }
}
