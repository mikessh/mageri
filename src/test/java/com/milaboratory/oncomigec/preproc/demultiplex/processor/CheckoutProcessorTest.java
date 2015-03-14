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
package com.milaboratory.oncomigec.preproc.demultiplex.processor;

import com.milaboratory.core.sequence.quality.QualityFormat;
import com.milaboratory.core.sequencing.io.fastq.PFastqReader;
import com.milaboratory.core.sequencing.io.fastq.SFastqReader;
import com.milaboratory.core.sequencing.read.PSequencingRead;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.oncomigec.preproc.demultiplex.config.BarcodeListParser;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.DemultiplexParameters;
import com.milaboratory.oncomigec.util.testing.TestUtil;
import com.milaboratory.util.CompressionType;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.milaboratory.oncomigec.util.testing.DefaultTestSet.*;

public class CheckoutProcessorTest {
    private static CheckoutProcessor runOnSampleData1() throws IOException {
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

    private static CheckoutProcessor runOnSampleData2() throws IOException {
        return runOnSampleData2(getBarcodes());
    }

    private static CheckoutProcessor runOnSampleData2(List<String> barcodes) throws IOException {
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
    public void test1() throws Exception {
        System.out.println("Running performance test for Checkout processor (single)");

        CheckoutProcessor processor = runOnSampleData1();

        System.out.println(processor);
        double extractionRatio = processor.extractionRatio();

        System.out.println("Extraction ratio = " + extractionRatio);
        Assert.assertTrue("Extraction ratio in expected bounds", extractionRatio >= 0.99);
    }

    @Test
    public void test2() throws Exception {
        System.out.println("Running performance test for Checkout processor (paired-end)");

        CheckoutProcessor processor = runOnSampleData2();

        System.out.println(processor);
        double extractionRatio = processor.extractionRatio();

        System.out.println("Extraction ratio = " + extractionRatio);
        Assert.assertTrue("Extraction ratio in expected bounds", extractionRatio >= 0.99);
    }

    @Test
    public void test3() throws Exception {
        System.out.println("Running performance test for Checkout processor (paired-end), bad slave barcode");

        CheckoutProcessor processor = runOnSampleData2(getBarcodesBadSlave());

        System.out.println(processor);
        double extractionRatio = processor.extractionRatio();

        System.out.println("Extraction ratio = " + extractionRatio);
        Assert.assertTrue("Extraction ratio in expected bounds", extractionRatio <= 0.01);
    }

    @Test
    public void serializationTest() throws IOException {
        System.out.println("Running serialization test for Checkout processor");

        CheckoutProcessor processor = runOnSampleData1();

        TestUtil.serializationCheckForOutputData(processor);

        processor = runOnSampleData2();

        TestUtil.serializationCheckForOutputData(processor);
    }
}
