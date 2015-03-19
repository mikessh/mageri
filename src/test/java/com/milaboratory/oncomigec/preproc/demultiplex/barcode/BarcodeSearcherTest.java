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
package com.milaboratory.oncomigec.preproc.demultiplex.barcode;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.mutations.GenericNucleotideMutationModel;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.mutations.NucleotideMutationModel;
import com.milaboratory.core.sequence.mutations.SubstitutionModels;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.quality.SequenceQualityPhred;
import com.milaboratory.core.sequencing.io.fastq.SFastqReader;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.oncomigec.util.Util;
import com.milaboratory.oncomigec.util.testing.DefaultTestSet;
import com.milaboratory.util.CompressionType;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class BarcodeSearcherTest {
    // todo: more sliding barcode searcher tests
    private static Random rnd = new Random(2106803L);
    private static NucleotideMutationModel mm = new GenericNucleotideMutationModel(
            SubstitutionModels.getEmpiricalNucleotideSubstitutionModel(),
            0.00522, 0.00198, 51102L).multiply(0.5),
            mmNoIndel = new GenericNucleotideMutationModel(
                    SubstitutionModels.getEmpiricalNucleotideSubstitutionModel(),
                    0, 0, 51102L).multiply(20.0);

    @Test
    public void exactTest() {
        String left = "cagtggtcctc", seed = "ATTAC", right = "tactgcgggta",
                signature = left + seed + right;
        SeedAndExtendBarcodeSearcher bs = new SeedAndExtendBarcodeSearcher(signature);

        // Check for correct determination of position
        for (int n = 0; n < 10000; n++) {
            int leftRndSize = rnd.nextInt(50), rightRndSize = rnd.nextInt(50);
            NucleotideSequence sequence = Util.randomSequence(leftRndSize);
            sequence = sequence.concatenate(new NucleotideSequence(signature));
            sequence = sequence.concatenate(Util.randomSequence(rightRndSize));
            BarcodeSearcherResult result = bs.search(new NucleotideSQPair(sequence));
            Assert.assertNotNull("BC found", result);
            Assert.assertEquals("Start position correct", leftRndSize, result.getFrom());
            Assert.assertEquals("End position correct", leftRndSize + signature.length(), result.getTo());
        }
    }

    @Test
    /**
     * Test with random errors present
     */
    public void fuzzyTest1() {
        String left = "cagtggtcctcaga", seed = "ATTAC", right = "tactgcgggtagggtgctact",
                signature = left + seed + right;
        BarcodeSearcher bs = new SeedAndExtendBarcodeSearcher(signature);
        NucleotideSequence leftRef = new NucleotideSequence(left), seedRef = new NucleotideSequence(seed),
                rightRef = new NucleotideSequence(right);
        int[] mutations;

        // Check for correct determination of position
        int total = 10000, found = 0;
        for (int n = 0; n < total; n++) {
            int leftRndSize = rnd.nextInt(50), rightRndSize = rnd.nextInt(50);
            NucleotideSequence sequence = Util.randomSequence(leftRndSize);

            mutations = Mutations.generateMutations(leftRef,
                    mm);
            sequence = sequence.concatenate(Mutations.mutate(leftRef, mutations));

            sequence = sequence.concatenate(seedRef);

            mutations = Mutations.generateMutations(rightRef,
                    mm);
            sequence = sequence.concatenate(Mutations.mutate(rightRef, mutations));

            sequence = sequence.concatenate(Util.randomSequence(rightRndSize));
            BarcodeSearcherResult result = bs.search(new NucleotideSQPair(sequence));

            if (result != null)
                found++;
        }
        double tpRate = found / (double) total;
        System.out.println("Fuzzy test1 (good qual mms), true positive rate " + tpRate);
        Assert.assertTrue("TP for good qual mms > 88%", tpRate > 0.88);
    }

    @Test
    /**
     * Tests with random errors present and quality lowered for errors
     */
    public void fuzzyTest2() {
        String left = "cagtggtcctcaga", seed = "ATTAC", right = "tactgcgggtagggtgctact",
                signature = left + seed + right;
        SeedAndExtendBarcodeSearcher bs = new SeedAndExtendBarcodeSearcher(signature);
        NucleotideSequence leftRef = new NucleotideSequence(left), seedRef = new NucleotideSequence(seed),
                rightRef = new NucleotideSequence(right);
        int[] mutations;

        int total = 10000, found = 0;
        for (int n = 0; n < total; n++) {
            Set<Integer> mutationPositions = new HashSet<>();
            int leftRndSize = rnd.nextInt(50), rightRndSize = rnd.nextInt(50);
            NucleotideSequence sequence = Util.randomSequence(leftRndSize);

            mutations = Mutations.generateMutations(leftRef,
                    mmNoIndel);
            for (int mutation : mutations)
                mutationPositions.add(sequence.size() + Mutations.getPosition(mutation));
            sequence = sequence.concatenate(Mutations.mutate(leftRef, mutations));

            sequence = sequence.concatenate(seedRef);

            mutations = Mutations.generateMutations(rightRef,
                    mmNoIndel);
            for (int mutation : mutations)
                mutationPositions.add(sequence.size() + Mutations.getPosition(mutation));
            sequence = sequence.concatenate(Mutations.mutate(rightRef, mutations));

            sequence = sequence.concatenate(Util.randomSequence(rightRndSize));

            byte[] quality = new byte[sequence.size()];
            for (int i = 0; i < sequence.size(); i++)
                quality[i] = mutationPositions.contains(i) ?
                        (byte) rnd.nextInt(Util.PH33_BAD_QUAL + 1) :
                        (byte) rnd.nextInt(40);

            BarcodeSearcherResult result = bs.search(new NucleotideSQPair(sequence,
                    new SequenceQualityPhred(quality)));

            if (result != null)
                found++;
        }
        double tpRate = found / (double) total;
        System.out.println("Fuzzy test2 (bad qual mms), true positive rate " + tpRate);
        Assert.assertTrue("TP for bad qual mms > 95%", tpRate > 0.95);
    }

    @Test
    public void negativeTest() {
        String left = "cagtggtcctcaga", seed = "ATTAC", right = "tactgcgggtagggtgctact",
                signature = left + seed + right;
        SeedAndExtendBarcodeSearcher bs = new SeedAndExtendBarcodeSearcher(signature);

        // Negative testing, mainly for purposes of catching errors
        for (int n = 0; n < 100000; n++) {
            NucleotideSequence sequence = Util.randomSequence(150);
            BarcodeSearcherResult result = bs.search(new NucleotideSQPair(sequence));
            Assert.assertNull("BC should not be found at random", result);
        }
        // We mix seed here
        for (int n = 0; n < 100000; n++) {
            int leftRndSize = rnd.nextInt(50), rightRndSize = rnd.nextInt(50);
            NucleotideSequence sequence = Util.randomSequence(leftRndSize);
            sequence = sequence.concatenate(new NucleotideSequence(seed));
            sequence = sequence.concatenate(Util.randomSequence(rightRndSize));
            BarcodeSearcherResult result = bs.search(new NucleotideSQPair(sequence));
            Assert.assertNull("BC should not be found at random, even with seed", result);
        }
    }

    @Test
    public void resourcesTest() throws IOException {
        SFastqReader reader = new SFastqReader(DefaultTestSet.getR1(),
                CompressionType.None);

        String signature = "NNNNNNNNNNNNNNtgatcttGACGTTGtagatgag";
        SeedAndExtendBarcodeSearcher bs = new SeedAndExtendBarcodeSearcher(signature);
        SSequencingRead read;
        int badCount = 0;

        while ((read = reader.take()) != null) {
            BarcodeSearcherResult result = bs.search(read.getData());
            if (result == null) {
                System.out.println("Failed to extract barcode: ");
                System.out.println(read.getDescription());
                System.out.println(NucleotideSQPair.toPrettyString(read.getData()));
                System.out.println();
                badCount++;
            }
        }

        Assert.assertEquals("Checkout is not worse than its old version", 0, badCount);
    }
}
