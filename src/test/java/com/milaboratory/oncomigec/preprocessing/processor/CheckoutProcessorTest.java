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
package com.milaboratory.oncomigec.preprocessing.processor;

import com.milaboratory.core.sequence.quality.QualityFormat;
import com.milaboratory.core.sequencing.io.fastq.PFastqReader;
import com.milaboratory.core.sequencing.io.fastq.SFastqReader;
import com.milaboratory.core.sequencing.read.PSequencingRead;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.oncomigec.preprocessing.*;
import com.milaboratory.oncomigec.preprocessing.barcode.BarcodeListParser;
import com.milaboratory.oncomigec.misc.testing.PercentRange;
import com.milaboratory.oncomigec.misc.testing.TestUtil;
import com.milaboratory.util.CompressionType;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.milaboratory.oncomigec.misc.testing.DefaultTestSet.*;

public class CheckoutProcessorTest {
    private static void assertProcessor(CheckoutProcessor processor) {
        System.out.println(processor);
        double extractionRatio = processor.extractionRatio();

        System.out.println("Extraction ratio = " + extractionRatio);
        Assert.assertTrue("Extraction ratio in expected bounds", extractionRatio >= 0.99);
    }

    private static CheckoutProcessor runOnSampleData1Positional() throws IOException {
        return runOnSampleData1Positional("NNNNNNNNNNNNNN");
    }

    private static CheckoutProcessor runOnSampleData1Positional(String mask) throws IOException {
        SPositionalExtractor processor = new SPositionalExtractor(SAMPLE_NAME, mask);

        SFastqReader reader = new SFastqReader(getR1(),
                CompressionType.None);

        SSequencingRead read;
        while ((read = reader.take()) != null) {
            processor.checkout(read);
        }

        return processor;
    }

    private static CheckoutProcessor runOnSampleData2Positional() throws IOException {
        return runOnSampleData2Positional("NNNNNNNNNNNNNN", null, null);
    }

    private static CheckoutProcessor runOnSampleData2Positional(String mask1,
                                                                String mask2,
                                                                String seed) throws IOException {
        PPositionalExtractor processor = mask2 == null ? new PPositionalExtractor(SAMPLE_NAME, mask1) :
                new PPositionalExtractor(SAMPLE_NAME, mask1, mask2);

        PFastqReader reader = new PFastqReader(getR1(), getR2(),
                QualityFormat.Phred33, CompressionType.None,
                null, false, false);

        PSequencingRead read;

        int nReads = 0, seedFound = 0;

        while ((read = reader.take()) != null) {
            PCheckoutResult checkoutResult = processor.checkout(read);
            if (checkoutResult != null &&
                    checkoutResult.slaveFound() &&
                    seed != null &&
                    checkoutResult.getSlaveResult().getUmi().toString().equals(seed)) {
                seedFound++;
            }
            nReads++;
        }

        if (mask2 != null && seed != null)
            PercentRange.createLowerBound("CorrectUMIExtracted", "SlaveSlidingSearcher", 95).assertInRange(seedFound, nReads);

        return processor;
    }

    @Test
    public void headerParserTest() throws Exception {
        System.out.println("Running performance test for Header parser");
        HeaderExtractor processor = new HeaderExtractor(SAMPLE_NAME);

        SFastqReader reader = new SFastqReader(getR1(),
                CompressionType.None);

        SSequencingRead read;
        while ((read = reader.take()) != null) {
            processor.checkout(read);
        }

        double extractionRatio = processor.extractionRatio();

        System.out.println("Extraction ratio = " + extractionRatio);
        Assert.assertTrue("Extraction ratio in expected bounds", extractionRatio == 1);
    }

    @Test
    public void positionalSingleEnd() throws Exception {
        System.out.println("Running performance test for positional Checkout processor (single)");

        CheckoutProcessor processor = runOnSampleData1Positional("nnnnnnNNNNNNNNNNNtNNNNNtNNNNNtgta");

        assertProcessor(processor);
    }

    @Test
    public void positionalPairedEnd() throws Exception {
        System.out.println("Running performance test for positional Checkout processor (paired-end)");

        CheckoutProcessor processor = runOnSampleData2Positional(
                "nnnnnnNNNNNNNNNNNtNNNNNtNNNNNtgta",
                "aggactgNNNNNNNaagtcgggnnnnnn",
                "CTTAAAG");

        assertProcessor(processor);
    }

    private static CheckoutProcessor runOnSampleData1Adapter() throws IOException {
        SAdapterExtractor processor = BarcodeListParser.generateSCheckoutProcessor(getBarcodes(),
                DemultiplexParameters.DEFAULT);

        SFastqReader reader = new SFastqReader(getR1(),
                CompressionType.None);

        SSequencingRead read;
        while ((read = reader.take()) != null) {
            processor.checkout(read);
        }

        return processor;
    }

    private static CheckoutProcessor runOnSampleData2Adapter() throws IOException {
        return runOnSampleData2Adapter(getBarcodes());
    }

    private static CheckoutProcessor runOnSampleData2Adapter(List<String> barcodes) throws IOException {
        PAdapterExtractor processor = BarcodeListParser.generatePCheckoutProcessor(barcodes,
                DemultiplexParameters.DEFAULT);

        PFastqReader reader = new PFastqReader(getR1(), getR2(),
                QualityFormat.Phred33, CompressionType.None,
                null, false, false);

        PSequencingRead read;
        while ((read = reader.take()) != null) {
            processor.checkout(read);
        }

        return processor;
    }

    @Test
    public void adapterSingleEnd() throws Exception {
        System.out.println("Running performance test for vanilla Checkout processor (single)");

        CheckoutProcessor processor = runOnSampleData1Adapter();

        assertProcessor(processor);
    }

    @Test
    public void adapterPairedEnd() throws Exception {
        System.out.println("Running performance test for vanilla Checkout processor (paired-end)");

        CheckoutProcessor processor = runOnSampleData2Adapter();

        assertProcessor(processor);
    }

    @Test
    public void adapterBadSlave() throws Exception {
        System.out.println("Running performance test for vanilla Checkout processor (paired-end), bad slave barcode");

        CheckoutProcessor processor = runOnSampleData2Adapter(getBarcodesBadSlave());

        System.out.println(processor);
        double extractionRatio = processor.extractionRatio();

        System.out.println("Extraction ratio = " + extractionRatio);
        Assert.assertTrue("Extraction ratio in expected bounds", extractionRatio <= 0.01);
    }

    @Test
    public void serializationTest() throws IOException {
        System.out.println("Running serialization test for Checkout processors");

        CheckoutProcessor processor = runOnSampleData1Positional();

        TestUtil.serializationCheck(processor);

        processor = runOnSampleData2Positional();

        TestUtil.serializationCheck(processor);

        processor = runOnSampleData1Adapter();

        TestUtil.serializationCheck(processor);

        processor = runOnSampleData2Adapter();

        TestUtil.serializationCheck(processor);
    }
}
