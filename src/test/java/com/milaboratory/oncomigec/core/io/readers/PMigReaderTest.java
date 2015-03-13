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
import com.milaboratory.oncomigec.preproc.demultiplex.entity.DemultiplexParameters;
import com.milaboratory.oncomigec.preproc.demultiplex.processor.PCheckoutProcessor;
import com.milaboratory.oncomigec.util.Util;
import com.milaboratory.util.CompressionType;
import org.junit.Assert;
import org.junit.Test;

import static com.milaboratory.oncomigec.util.testing.DefaultTestSet.*;

public class PMigReaderTest {
    @Test
    public void preprocessedTest() throws Exception {
        PMigReader reader = new PMigReader(getR1(), getR2(),
                SAMPLE_NAME, MigReaderParameters.IGNORE_QUAL);

        PMig pMig;
        while ((pMig = reader.take(SAMPLE_NAME, 5)) != null) {
            NucleotideSequence umi = pMig.getUmi();

            // Manually count number of reads with UMI
            SFastqReader standardReader = new SFastqReader(getR1(), CompressionType.None);
            SSequencingRead read;
            int rawCount = 0;
            while ((read = standardReader.take()) != null)
                if (read.getDescription().contains(Util.UMI_FIELD_ID + ":" + umi.toString()))
                    rawCount++;

            Assert.assertEquals("MIG size is correct", pMig.size(), rawCount);
        }
    }

    @Test
    public void checkoutTest() throws Exception {
        PCheckoutProcessor processor = BarcodeListParser.generatePCheckoutProcessor(getBarcodes(),
                DemultiplexParameters.DEFAULT);

        PMigReader reader = new PMigReader(getR1(), getR2(), processor);
        PMigReader reader2 = new PMigReader(getR1(), getR2(), SAMPLE_NAME);

        UmiHistogram histogram = reader2.getUmiHistogram(SAMPLE_NAME);

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
        System.out.println("Average difference in MIG size is " + (avgSizeDifference * 100) + "%");
        Assert.assertTrue("Average MIG size difference if < 0.1%", Math.abs(avgSizeDifference) < 0.001);
    }
}
