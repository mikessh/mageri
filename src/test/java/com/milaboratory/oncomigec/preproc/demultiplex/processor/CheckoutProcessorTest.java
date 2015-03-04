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
import com.milaboratory.util.CompressionType;
import org.junit.Assert;
import org.junit.Test;

import static com.milaboratory.oncomigec.util.testing.DefaultTestSet.*;

public class CheckoutProcessorTest {

    @Test
    public void test() throws Exception {
        PCheckoutProcessor processor = BarcodeListParser.generatePCheckoutProcessor(getBarcodes(),
                DemultiplexParameters.DEFAULT);

        PFastqReader reader = new PFastqReader(getR1(), getR2(),
                QualityFormat.Phred33, CompressionType.None,
                null, false, false);

        PSequencingRead read;
        while ((read = reader.take()) != null) {
            processor.checkout(read);
        }

        System.out.println(processor);
        double extractionRatio = processor.extractionRatio();

        System.out.println("Extraction ratio = " + extractionRatio);
        Assert.assertTrue("Extraction ratio in expected bounds", extractionRatio >= 0.99);
    }
}
