/*
 * Copyright 2013-2015 Mikhail Shugay (mikhail.shugay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last modified on 16.3.2015 by mikesh
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
                new ResourceIOProvider().getWrappedStream("pipeline/refs.fa"),
                new BasicGenomicInfoProvider());

        Assert.assertTrue(!referenceLibrary.getReferences().isEmpty());

        TestUtil.serializationCheck(referenceLibrary);
    }

    @Test
    @Category(FastTests.class)
    public void genomicTest() throws IOException {

        ReferenceLibrary referenceLibrary = ReferenceLibrary.fromInput(
                new ResourceIOProvider().getWrappedStream("pipeline/refs.fa"),
                new BedGenomicInfoProvider(
                        new ResourceIOProvider().getWrappedStream("pipeline/refs.bed"),
                        new ResourceIOProvider().getWrappedStream("pipeline/contigs.txt")
                ));

        Assert.assertTrue(!referenceLibrary.getReferences().isEmpty());

        TestUtil.serializationCheck(referenceLibrary);
    }
}
