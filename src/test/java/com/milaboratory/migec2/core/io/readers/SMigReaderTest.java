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
package com.milaboratory.migec2.core.io.readers;

import com.milaboratory.core.sequencing.io.fastq.SFastqReader;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.migec2.core.io.entity.SMig;
import com.milaboratory.migec2.core.io.misc.MigReaderParameters;
import org.junit.Assert;
import org.junit.Test;
import com.milaboratory.migec2.util.testing.TestResources;
import com.milaboratory.migec2.util.Util;

import java.io.File;

public class SMigReaderTest {
    //@Test
    public void run() throws Exception {
        String sampleName = "SPIKE";

        File file = TestResources.getResource("21_SPIKE-1R_R1.fastq");
        SMigReader reader = new SMigReader(file, sampleName, MigReaderParameters.IGNORE_QUAL);

        for (int i = 0; i < 10; i++) {
            // Take next large enough MIG
            SMig sMig = reader.take(sampleName, 100);

            // Check that all reads have correct header
            for (SSequencingRead read : sMig.getReads())
                Assert.assertEquals(Util.extractUmi(read.getDescription()), sMig.getUmi());

            // Manually count number of reads with UMI
            SFastqReader standardReader = new SFastqReader(file);
            SSequencingRead read;
            int rawCount = 0;
            while ((read = standardReader.take()) != null)
                if (read.getDescription().contains(Util.UMI_FIELD_ID + ":" + sMig.getUmi()))
                    rawCount++;

            Assert.assertEquals(sMig.size(), rawCount);
        }
    }
}
