/*
 * Copyright 2014-2016 Mikhail Shugay
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

package com.antigenomics.mageri.core.genomic;

import com.antigenomics.mageri.TestUtil;
import com.antigenomics.mageri.pipeline.input.ResourceIOProvider;
import com.antigenomics.mageri.FastTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;

public class ReferenceLibraryTest {
    @Test
    @Category(FastTests.class)
    public void basicTest() throws IOException {

        ReferenceLibrary referenceLibrary = ReferenceLibrary.fromInput(
                ResourceIOProvider.INSTANCE.getWrappedStream("genomic/panel_refs.fa"),
                new BasicGenomicInfoProvider());

        System.out.println(referenceLibrary);

        Assert.assertTrue(!referenceLibrary.getReferences().isEmpty());

        for (Reference reference : referenceLibrary.getReferences()) {
            Assert.assertEquals(reference.getSequence().size(),
                    reference.getGenomicInfo().getContig().getLength());
            Assert.assertEquals(0,
                    reference.getGenomicInfo().getStart());
            Assert.assertEquals(reference.getSequence().size(),
                    reference.getGenomicInfo().getEnd());
        }

        TestUtil.serializationCheck(referenceLibrary);
    }

    @Test
    @Category(FastTests.class)
    public void genomicTest() throws IOException {

        ReferenceLibrary referenceLibrary = ReferenceLibrary.fromInput(
                ResourceIOProvider.INSTANCE.getWrappedStream("genomic/panel_refs.fa"),
                new BedGenomicInfoProvider(
                        ResourceIOProvider.INSTANCE.getWrappedStream("genomic/panel_refs.bed"),
                        ResourceIOProvider.INSTANCE.getWrappedStream("pipeline/contigs.txt")
                ));

        System.out.println(referenceLibrary);

        Assert.assertTrue(!referenceLibrary.getReferences().isEmpty());

        Reference braf = referenceLibrary.getByName("BRAF_E15");
        Assert.assertNotNull(braf);
        Assert.assertEquals("chr7", braf.getGenomicInfo().getChrom());
        Assert.assertEquals(140453124, braf.getGenomicInfo().getStart());
        Assert.assertEquals(140453231, braf.getGenomicInfo().getEnd());

        TestUtil.serializationCheck(referenceLibrary);
    }

    @Test
    @Category(FastTests.class)
    public void cgcGenomicTest() throws IOException {

        GenomicInfoProvider giProvider = new BedGenomicInfoProvider(
                ResourceIOProvider.INSTANCE.getWrappedStream("genomic/cgc_exons_flank50.bed"),
                ResourceIOProvider.INSTANCE.getWrappedStream("genomic/contigs_hg38.txt")
        );

        ReferenceLibrary referenceLibrary = ReferenceLibrary.fromInput(
                ResourceIOProvider.INSTANCE.getWrappedStream("genomic/cgc_exons_flank50.fa"),
                giProvider);

        Assert.assertTrue(!referenceLibrary.getReferences().isEmpty());
        Assert.assertEquals(referenceLibrary.size(), giProvider.size());


        Reference ref = referenceLibrary.getByName("RANBP2_ENSE00001785652");
        Assert.assertNotNull(ref);
        Assert.assertEquals("chr2", ref.getGenomicInfo().getChrom());
        Assert.assertEquals(108740438, ref.getGenomicInfo().getStart());
        Assert.assertEquals(108740731, ref.getGenomicInfo().getEnd());
        Assert.assertTrue(ref.getGenomicInfo().positiveStrand());

        ref = referenceLibrary.getByName("CEBPA_ENSE00001973852");
        Assert.assertNotNull(ref);
        Assert.assertEquals("chr19", ref.getGenomicInfo().getChrom());
        Assert.assertEquals(33299883, ref.getGenomicInfo().getStart());
        Assert.assertEquals(33302614, ref.getGenomicInfo().getEnd());
        Assert.assertTrue(!ref.getGenomicInfo().positiveStrand());

        TestUtil.serializationCheck(referenceLibrary);
    }

    @Test(expected = RuntimeException.class)
    @Category(FastTests.class)
    public void cgcGenomicBadContigsTest() throws IOException {

        GenomicInfoProvider giProvider = new BedGenomicInfoProvider(
                ResourceIOProvider.INSTANCE.getWrappedStream("genomic/cgc_exons_flank50.bed"),
                ResourceIOProvider.INSTANCE.getWrappedStream("genomic/contigs_grch38.txt")
        );

        ReferenceLibrary.fromInput(
                ResourceIOProvider.INSTANCE.getWrappedStream("genomic/cgc_exons_flank50.fa"),
                giProvider);
    }
}
