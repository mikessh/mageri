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
package com.milaboratory.oncomigec.core.io.readers;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.io.fastq.SFastqReader;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.oncomigec.core.io.entity.PMig;
import com.milaboratory.oncomigec.core.io.misc.MigReaderParameters;
import com.milaboratory.oncomigec.core.io.misc.UmiHistogram;
import com.milaboratory.oncomigec.preproc.demultiplex.config.BarcodeListParser;
import com.milaboratory.oncomigec.util.Util;
import com.milaboratory.oncomigec.util.testing.TestResources;
import org.junit.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PMigReaderTest {
    //todo: make tests
    //@Test
    public void preprocessedTest() throws Exception {
        String sampleName = "21_SPIKE-1R";

        File file1 = TestResources.getResource("21_SPIKE-1R_R1.fastq"),
                file2 = TestResources.getResource("21_SPIKE-1R_R2.fastq");

        PMigReader reader = new PMigReader(file1, file2, sampleName, MigReaderParameters.IGNORE_QUAL);

        for (int i = 0; i < 10; i++) {
            // Take next large enough MIG
            PMig pMig = reader.take(sampleName, 100);
            NucleotideSequence umi = pMig.getUmi();

            // Check that all reads have correct header
            //for (NucleotideSQPairTuple read : pMig.getMig1().getReads())
            //    Assert.assertEquals("Correct UMI header in read",
            //            Util.extractUmi(read.getDescription()),
            //           umi);

            // Manually count number of reads with UMI
            SFastqReader standardReader = new SFastqReader(file1);
            SSequencingRead read;
            int rawCount = 0;
            while ((read = standardReader.take()) != null)
                if (read.getDescription().contains(Util.UMI_FIELD_ID + ":" + umi.toString()))
                    rawCount++;

            Assert.assertEquals("MIG size is correct", pMig.size(), rawCount);
        }
    }

    //@Test
    public void checkoutTest() throws Exception {
        File file1 = TestResources.getResource("21_SPIKE-1R_R1.fastq"),
                file2 = TestResources.getResource("21_SPIKE-1R_R2.fastq");
        String barcode = "SPIKE-1R\t1\tNNNNNNNNNNNNtgatcttGACGTTGTagatgag\t-";
        List<String> barcodes = new ArrayList<>();
        barcodes.add(barcode);

        String sampleName = "SPIKE-1R";

        PMigReader reader = new PMigReader(file1, file2,
                BarcodeListParser.generatePCheckoutProcessor(barcodes));

        PMigReader reader2 = new PMigReader(file1, file2, sampleName);
        UmiHistogram histogram = reader2.getUmiHistogram("SPIKE-1R");

        double avgSizeDifference = 0;
        int readsWithDifferentUmisCount = 0, totalReads = 0, n = 100;

        for (int i = 0; i < n; i++) {
            // Take next large enough MIG
            PMig pMig = reader.take(sampleName, 100);
            NucleotideSequence umi = pMig.getUmi();

            // Check that all reads have correct header
            //for (NucleotideSQPair read : pMig.getMig1().getReads())
            //    if (!Util.extractUmi(read.getDescription()).equals(umi))
            //        readsWithDifferentUmisCount++;

            totalReads += pMig.getMig1().size();

            // Use histogram to count number of reads with UMI in original data
            int rawCount = histogram.migSize(umi);

            avgSizeDifference += 2.0 * (pMig.size() - rawCount) / (pMig.size() + rawCount);
        }

        double readsWithDifferentUmisRatio = readsWithDifferentUmisCount / (double) totalReads;
        avgSizeDifference /= n;

        System.out.println("Different UMI extraction in " + (readsWithDifferentUmisRatio * 100) + "% cases");
        Assert.assertTrue("Less than 0.1% reads have different UMIs assigned", readsWithDifferentUmisRatio < 0.001);
        System.out.println("Average difference in MIG size is " + (avgSizeDifference * 100) + "%");
        Assert.assertTrue("Average MIG size difference if < 0.1%", Math.abs(avgSizeDifference) < 0.001);
    }
}
