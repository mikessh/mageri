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
package com.milaboratory.mageri.preprocessing.barcode;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.mutations.GenericNucleotideMutationModel;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.mutations.NucleotideMutationModel;
import com.milaboratory.core.sequence.mutations.SubstitutionModels;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.quality.SequenceQualityPhred;
import com.milaboratory.core.sequencing.io.fastq.SFastqReader;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.mageri.FastTests;
import com.milaboratory.mageri.TestDataset;
import com.milaboratory.mageri.misc.QualityDefaults;
import com.milaboratory.util.CompressionType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static com.milaboratory.mageri.generators.RandomUtil.randomSequence;

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
    @Category(FastTests.class)
    public void exactTest() {
        String left = "cagtggtcctc", seed = "ATTAC", right = "tactgcgggta",
                signature = left + seed + right;
        SeedAndExtendBarcodeSearcher bs = new SeedAndExtendBarcodeSearcher(signature);

        // Check for correct determination of position
        for (int n = 0; n < 10000; n++) {
            int leftRndSize = rnd.nextInt(50), rightRndSize = rnd.nextInt(50);
            NucleotideSequence sequence = randomSequence(leftRndSize);
            sequence = sequence.concatenate(new NucleotideSequence(signature));
            sequence = sequence.concatenate(randomSequence(rightRndSize));
            BarcodeSearcherResult result = bs.search(new NucleotideSQPair(sequence));
            Assert.assertNotNull("BC found", result);
            Assert.assertEquals("Start position correct", leftRndSize, result.getFrom());
            Assert.assertEquals("End position correct", leftRndSize + signature.length(), result.getTo());
        }
    }

    @Test
    @Category(FastTests.class)
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
            NucleotideSequence sequence = randomSequence(leftRndSize);

            mutations = Mutations.generateMutations(leftRef,
                    mm);
            sequence = sequence.concatenate(Mutations.mutate(leftRef, mutations));

            sequence = sequence.concatenate(seedRef);

            mutations = Mutations.generateMutations(rightRef,
                    mm);
            sequence = sequence.concatenate(Mutations.mutate(rightRef, mutations));

            sequence = sequence.concatenate(randomSequence(rightRndSize));
            BarcodeSearcherResult result = bs.search(new NucleotideSQPair(sequence));

            if (result != null)
                found++;
        }
        double tpRate = found / (double) total;
        System.out.println("Fuzzy test1 (good qual mms), true positive rate " + tpRate);
        Assert.assertTrue("TP for good qual mms > 88%", tpRate > 0.88);
    }

    @Test
    @Category(FastTests.class)
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
            NucleotideSequence sequence = randomSequence(leftRndSize);

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

            sequence = sequence.concatenate(randomSequence(rightRndSize));

            byte[] quality = new byte[sequence.size()];
            for (int i = 0; i < sequence.size(); i++)
                quality[i] = mutationPositions.contains(i) ?
                        (byte) rnd.nextInt(QualityDefaults.PH33_BAD_QUAL + 1) :
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
    @Category(FastTests.class)
    public void negativeTest() {
        String left = "cagtggtcctcaga", seed = "ATTAC", right = "tactgcgggtagggtgctact",
                signature = left + seed + right;
        SeedAndExtendBarcodeSearcher bs = new SeedAndExtendBarcodeSearcher(signature);

        // Negative testing, mainly for purposes of catching errors
        for (int n = 0; n < 100000; n++) {
            NucleotideSequence sequence = randomSequence(150);
            BarcodeSearcherResult result = bs.search(new NucleotideSQPair(sequence));
            Assert.assertNull("BC should not be found at random", result);
        }
        // We mix seed here
        for (int n = 0; n < 100000; n++) {
            int leftRndSize = rnd.nextInt(50), rightRndSize = rnd.nextInt(50);
            NucleotideSequence sequence = randomSequence(leftRndSize);
            sequence = sequence.concatenate(new NucleotideSequence(seed));
            sequence = sequence.concatenate(randomSequence(rightRndSize));
            BarcodeSearcherResult result = bs.search(new NucleotideSQPair(sequence));
            Assert.assertNull("BC should not be found at random, even with seed", result);
        }
    }

    @Test
    @Category(FastTests.class)
    public void resourcesTest() throws IOException {
        SFastqReader reader = new SFastqReader(TestDataset.getR1(),
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
