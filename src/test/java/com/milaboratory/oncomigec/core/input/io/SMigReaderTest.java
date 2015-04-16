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
package com.milaboratory.oncomigec.core.input.io;

import com.milaboratory.core.sequencing.io.fastq.SFastqReader;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.oncomigec.core.input.PreprocessorParameters;
import com.milaboratory.oncomigec.core.input.SMig;
import com.milaboratory.oncomigec.core.input.SMigReader;
import com.milaboratory.oncomigec.preprocessing.HeaderExtractor;
import com.milaboratory.oncomigec.misc.Util;
import com.milaboratory.util.CompressionType;
import org.junit.Assert;
import org.junit.Test;

import static com.milaboratory.oncomigec.misc.testing.DefaultTestSet.SAMPLE_NAME;
import static com.milaboratory.oncomigec.misc.testing.DefaultTestSet.getR1;

public class SMigReaderTest {
    @Test
    public void run() throws Exception {
        SMigReader reader = new SMigReader(getR1(), new HeaderExtractor(SAMPLE_NAME), PreprocessorParameters.IGNORE_QUAL);

        SMig sMig;
        while ((sMig = reader.take(SAMPLE_NAME, 5)) != null) {
            // Check that all reads have correct header
            //for (SSequencingRead read : sMig.getReads())
            //    Assert.assertEquals(Util.extractUmi(read.getDescription()), sMig.getUmi());

            // Manually count number of reads with UMI
            SFastqReader standardReader = new SFastqReader(getR1(), CompressionType.None);
            SSequencingRead read;
            int rawCount = 0;
            while ((read = standardReader.take()) != null)
                if (read.getDescription().contains(Util.UMI_FIELD_ID + ":" + sMig.getUmi()))
                    rawCount++;
            Assert.assertEquals(sMig.size(), rawCount);
        }
    }
}
