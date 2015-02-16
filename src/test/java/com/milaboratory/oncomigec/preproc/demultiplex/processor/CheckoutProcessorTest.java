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
import com.milaboratory.core.sequencing.read.PSequencingRead;
import com.milaboratory.oncomigec.preproc.demultiplex.config.BarcodeListParser;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.DemultiplexParameters;
import com.milaboratory.oncomigec.util.testing.TestResources;
import com.milaboratory.util.CompressionType;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.util.List;

public class CheckoutProcessorTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    //@Test
    public void testWithRes() throws Exception {
        List<String> lines = FileUtils.readLines(TestResources.getResource("barcodes.txt"));
        PCheckoutProcessor processor = BarcodeListParser.generatePCheckoutProcessor(lines,
                DemultiplexParameters.DEFAULT);
        PFastqReader reader = new PFastqReader(TestResources.getResource("sample_R1.fastq.gz"),
                TestResources.getResource("sample_R2.fastq.gz"),
                QualityFormat.Phred33, CompressionType.GZIP);
        PSequencingRead read;
        while ((read = reader.take()) != null) {
            processor.checkout(read);
        }
        System.out.println(processor);
        double extractionRatio = processor.extractionRatio();
        System.out.println("Extraction ratio = " + extractionRatio);
        Assert.assertTrue("Extraction ratio in expected bounds", extractionRatio <= 0.7 && extractionRatio >= 0.6);
    }
}
