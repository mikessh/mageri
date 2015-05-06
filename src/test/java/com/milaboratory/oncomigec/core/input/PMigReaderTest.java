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
package com.milaboratory.oncomigec.core.input;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.io.fastq.SFastqReader;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.oncomigec.PercentRangeAssertion;
import com.milaboratory.oncomigec.preprocessing.DemultiplexParameters;
import com.milaboratory.oncomigec.preprocessing.HeaderExtractor;
import com.milaboratory.oncomigec.preprocessing.PAdapterExtractor;
import com.milaboratory.oncomigec.preprocessing.barcode.BarcodeListParser;
import com.milaboratory.util.CompressionType;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.*;

import static com.milaboratory.oncomigec.TestDataset.*;

public class PMigReaderTest {
    @Test
    public void preprocessedTest() throws Exception {
        PMigReader reader = new PMigReader(getR1(), getR2(),
                new HeaderExtractor(SAMPLE_NAME), PreprocessorParameters.IGNORE_QUAL);

        PMig pMig;
        while ((pMig = reader.take(SAMPLE_NAME, 5)) != null) {
            NucleotideSequence umi = pMig.getUmi();

            // Manually count number of reads with UMI
            SFastqReader standardReader = new SFastqReader(getR1(), CompressionType.None);
            SSequencingRead read;
            int rawCount = 0;
            while ((read = standardReader.take()) != null)
                if (read.getDescription().contains(HeaderExtractor.UMI_FIELD_ID + ":" + umi.toString()))
                    rawCount++;

            Assert.assertEquals("MIG size is correct", pMig.size(), rawCount);
        }
    }

    @Test
    public void checkoutTest() throws Exception {
        PAdapterExtractor processor = BarcodeListParser.generatePCheckoutProcessor(getBarcodesGood(),
                DemultiplexParameters.DEFAULT);

        PMigReader reader = new PMigReader(getR1(), getR2(), processor);
        PMigReader exactReader = new PMigReader(getR1(), getR2(), new HeaderExtractor(SAMPLE_NAME));

        MigSizeDistribution histogram = exactReader.getUmiHistogram(SAMPLE_NAME);

        double avgSizeDifference = 0;
        int readsWithDifferentUmisCount = 0, totalReads = 0;

        PMig pMig;
        while ((pMig = reader.take(SAMPLE_NAME, 5)) != null) {
            NucleotideSequence umi = pMig.getUmi();

            totalReads += pMig.getMig1().size();

            // Use histogram to count number of reads with UMI in original data
            int rawCount = histogram.migSize(umi);

            avgSizeDifference += 2.0 * (pMig.size() - rawCount) / (pMig.size() + rawCount);
        }

        double readsWithDifferentUmisRatio = readsWithDifferentUmisCount / (double) totalReads;
        avgSizeDifference /= histogram.getMigsTotal();

        Assert.assertTrue("Less than 0.1% reads have different UMIs assigned", readsWithDifferentUmisRatio < 0.001);
        System.out.println("Average difference in MIG size is " + (int) (avgSizeDifference * 100) + "%");
        Assert.assertTrue("Average MIG size difference if < 0.1%", Math.abs(avgSizeDifference) < 0.001);
    }

    @Test
    public void orientationTest() throws Exception {
        System.out.println("Testing slave first attribute");
        orientationTest(getR1(), getR2(), getBarcodesSlaveFirst());
        System.out.println("Testing non-oriented reads");
        orientationTest(getR2(), getR1(), getBarcodesGood());
    }

    private static void addToMap(Map<NucleotideSequence, Integer> counters, List<NucleotideSequence> sequences) {
        for (NucleotideSequence sequence : sequences) {
            Integer counter = counters.get(sequence);
            counter = counter == null ? 1 : (counter + 1);
            counters.put(sequence, counter);
        }
    }

    private static double intersect(Map<NucleotideSequence, Integer> countersA,
                                    Map<NucleotideSequence, Integer> countersB,
                                    int totalA, int totalB) {
        double intersection = 0;
        Set<NucleotideSequence> sequences = new HashSet<>(countersA.keySet());
        sequences.addAll(countersB.keySet());
        for (NucleotideSequence sequence : sequences) {
            Integer counterA = countersA.get(sequence),
                    counterB = countersB.get(sequence);
            counterA = counterA == null ? 0 : counterA;
            counterB = counterB == null ? 0 : counterB;
            intersection += Math.sqrt(counterA * counterB / (double) totalA / (double) totalB);
        }
        return intersection;
    }

    private void orientationTest(InputStream r1, InputStream r2,
                                 List<String> barcodes) throws Exception {
        PAdapterExtractor processor = BarcodeListParser.generatePCheckoutProcessor(getBarcodesGood(),
                DemultiplexParameters.ORIENTED),
                processorSlaveFirst = BarcodeListParser.generatePCheckoutProcessor(barcodes,
                        DemultiplexParameters.DEFAULT);

        PMigReader reader = new PMigReader(getR1(), getR2(), processor);
        PMigReader slaveFirstReader = new PMigReader(r1, r2, processorSlaveFirst);

        Map<NucleotideSequence, Integer> counters1 = new HashMap<>(), counters2 = new HashMap<>(),
                slaveFirstCounters1 = new HashMap<>(), slaveFirstCounters2 = new HashMap<>();

        int readsCount = 0, slaveFirstReadsCount = 0;

        PMig mig, slaveFirstMig;
        while ((mig = reader.take(SAMPLE_NAME, 5)) != null) {
            slaveFirstMig = slaveFirstReader.take(SAMPLE_NAME, 5);

            // Add reads for comparison
            addToMap(counters1, mig.getMig1().getSequences());
            addToMap(counters2, mig.getMig2().getSequences());
            addToMap(slaveFirstCounters1, slaveFirstMig.getMig1().getSequences());
            addToMap(slaveFirstCounters2, slaveFirstMig.getMig2().getSequences());

            readsCount += mig.size();
            slaveFirstReadsCount += slaveFirstMig.size();
        }

        PercentRangeAssertion.createLowerBound("MasterFirstSlaveFirstIntersection", "Read1", 90).
                assertInRange(intersect(counters1, slaveFirstCounters1, readsCount, slaveFirstReadsCount));
        PercentRangeAssertion.createLowerBound("MasterFirstSlaveFirstIntersection", "Read2", 90).
                assertInRange(intersect(counters2, slaveFirstCounters2, readsCount, slaveFirstReadsCount));
    }
}
