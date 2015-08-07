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
package com.milaboratory.mageri.core.input;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.io.fastq.SFastqReader;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.mageri.FastTests;
import com.milaboratory.mageri.PercentRangeAssertion;
import com.milaboratory.mageri.pipeline.RuntimeParameters;
import com.milaboratory.mageri.preprocessing.DemultiplexParameters;
import com.milaboratory.mageri.preprocessing.HeaderExtractor;
import com.milaboratory.mageri.preprocessing.PAdapterExtractor;
import com.milaboratory.mageri.preprocessing.barcode.BarcodeListParser;
import com.milaboratory.util.CompressionType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.InputStream;
import java.util.*;

import static com.milaboratory.mageri.TestDataset.*;

public class PMigReaderTest {
    @Test
    @Category(FastTests.class)
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
    @Category(FastTests.class)
    public void checkoutTest() throws Exception {
        limitTest(getBarcodesGood());
        limitTest(getBarcodesNoSlave());
        extractionTest(getBarcodesGood(), "Correct master&slave");
        extractionTest(getBarcodesNoSlave(), "Correct master, no slave");
    }

    public void limitTest(List<String> barcodes) throws Exception {
        PAdapterExtractor processor = BarcodeListParser.generatePCheckoutProcessor(barcodes,
                DemultiplexParameters.DEFAULT);

        int totalReads = 0, readLimit = 1000;

        PMigReader reader = new PMigReader(getR1(), getR2(), processor,
                PreprocessorParameters.DEFAULT.withMinUmiMismatchRatio(-1).withUmiQualThreshold((byte) 0),
                RuntimeParameters.DEFAULT.withReadLimit(readLimit));

        PMig pMig;
        while ((pMig = reader.take(SAMPLE_NAME)) != null) {
            totalReads += pMig.size();
        }

        Assert.assertEquals("Correct number of reads taken", readLimit, processor.getTotal());
        Assert.assertEquals("Correct number of reads in MIGs", processor.getSlaveCounter(SAMPLE_NAME), totalReads);
    }

    public void extractionTest(List<String> barcodes, String condition) throws Exception {
        PAdapterExtractor processor = BarcodeListParser.generatePCheckoutProcessor(barcodes,
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

            avgSizeDifference += 2.0 * Math.abs(pMig.size() - rawCount) / (pMig.size() + rawCount);
        }

        avgSizeDifference /= histogram.getMigsTotal();

        PercentRangeAssertion.createUpperBound("Reads have different UMIs assigned", condition, 1).
                assertInRange(readsWithDifferentUmisCount, totalReads);
        PercentRangeAssertion.createUpperBound("Average MIG size difference", condition, 1).
                assertInRange(avgSizeDifference * 100);
    }

    @Test
    public void orientationTest() throws Exception {
        System.out.println("Testing slave first attribute");
        orientationTest(getR1(), getR2(), getBarcodesSlaveFirst());
        System.out.println("Testing non-oriented reads");
        orientationTest(getR2(), getR1(), getBarcodesGood());
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
}
