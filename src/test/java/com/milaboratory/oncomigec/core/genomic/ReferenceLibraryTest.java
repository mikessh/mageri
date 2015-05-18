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

package com.milaboratory.oncomigec.core.genomic;

import com.milaboratory.oncomigec.FastTests;
import com.milaboratory.oncomigec.TestUtil;
import com.milaboratory.oncomigec.pipeline.input.ResourceIOProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;

public class ReferenceLibraryTest {
    @Test
    @Category(FastTests.class)
    public void basicTest() throws IOException {

        ReferenceLibrary referenceLibrary = ReferenceLibrary.fromInput(
                ResourceIOProvider.INSTANCE.getWrappedStream("pipeline/refs.fa"),
                new BasicGenomicInfoProvider());

        System.out.println(referenceLibrary);

        Assert.assertTrue(!referenceLibrary.getReferences().isEmpty());

        for (Reference reference : referenceLibrary.getReferences()) {
            Assert.assertEquals(reference.getSequence().size(),
                    reference.getGenomicInfo().getContig().getLength());
            Assert.assertEquals(0,
                    reference.getGenomicInfo().getStart());
            Assert.assertEquals(reference.getSequence().size(),
                    reference.getGenomicInfo().getEnd() + 1);
        }

        TestUtil.serializationCheck(referenceLibrary);
    }

    @Test
    @Category(FastTests.class)
    public void genomicTest() throws IOException {

        ReferenceLibrary referenceLibrary = ReferenceLibrary.fromInput(
                ResourceIOProvider.INSTANCE.getWrappedStream("pipeline/refs.fa"),
                new BedGenomicInfoProvider(
                        ResourceIOProvider.INSTANCE.getWrappedStream("pipeline/refs.bed"),
                        ResourceIOProvider.INSTANCE.getWrappedStream("pipeline/contigs.txt")
                ));

        System.out.println(referenceLibrary);

        Assert.assertTrue(!referenceLibrary.getReferences().isEmpty());

        Reference braf = referenceLibrary.getByName("BRAF_E15");
        Assert.assertNotNull(braf);
        Assert.assertEquals("chr7", braf.getGenomicInfo().getChrom());
        Assert.assertEquals(140453124, braf.getGenomicInfo().getStart());
        Assert.assertEquals(140453232, braf.getGenomicInfo().getEnd());

        TestUtil.serializationCheck(referenceLibrary);
    }
}
