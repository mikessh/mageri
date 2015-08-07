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

import com.milaboratory.core.sequencing.io.fastq.SFastqReader;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.mageri.FastTests;
import com.milaboratory.mageri.preprocessing.HeaderExtractor;
import com.milaboratory.util.CompressionType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.milaboratory.mageri.TestDataset.SAMPLE_NAME;
import static com.milaboratory.mageri.TestDataset.getR1;

public class SMigReaderTest {
    @Test
    @Category(FastTests.class)
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
                if (read.getDescription().contains(HeaderExtractor.UMI_FIELD_ID + ":" + sMig.getUmi()))
                    rawCount++;
            Assert.assertEquals(sMig.size(), rawCount);
        }
    }
}
