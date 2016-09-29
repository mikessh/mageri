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

package com.antigenomics.mageri.pipeline;

import com.antigenomics.mageri.TestUtil;
import com.antigenomics.mageri.core.output.VcfWriter;
import com.antigenomics.mageri.core.variant.Variant;
import com.antigenomics.mageri.pipeline.analysis.ProjectAnalysisRaw;
import com.antigenomics.mageri.pipeline.input.Input;
import com.antigenomics.mageri.pipeline.input.InputParser;
import com.antigenomics.mageri.FastTests;
import com.antigenomics.mageri.core.mapping.AlignedConsensus;
import com.antigenomics.mageri.core.output.SamWriter;
import com.antigenomics.mageri.core.variant.VariantCaller;
import com.antigenomics.mageri.pipeline.analysis.ProjectAnalysis;
import com.antigenomics.mageri.pipeline.analysis.Sample;
import com.antigenomics.mageri.pipeline.analysis.SampleAnalysis;
import com.antigenomics.mageri.pipeline.input.ResourceIOProvider;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class PipelineTest {
    public static final InputParser INPUT_PARSER = new InputParser(ResourceIOProvider.INSTANCE);

    @Test
    @Category(FastTests.class)
    public void fullTest() throws Exception {
        fullTest(false);
        fullTest(true);
    }

    public void fullTest(boolean rawReads) throws Exception {
        Input input = INPUT_PARSER.parseJson("pipeline/tabular.pri.json");

        ProjectAnalysis projectAnalysis = rawReads ? new ProjectAnalysisRaw(input) : new ProjectAnalysis(input);

        Set<String> expectedVariants = new HashSet<>();
        expectedVariants.add("S30:T>C");
        expectedVariants.add("S87:T>C");
        expectedVariants.add("S88:T>C");

        for (Sample sample : projectAnalysis.getProject().getSamples()) {
            System.out.println(sample.getFullName());

            SampleAnalysis sampleAnalysis = projectAnalysis.getAnalysis(sample);
            VariantCaller variantCaller = sampleAnalysis.getVariantCaller();

            Set<String> observedVariants = new HashSet<>();
            for (Variant variant : variantCaller.getVariants()) {
                System.out.println(variant.getReference().getName() + "\t" + variant.toString());
                observedVariants.add(variant.getMutation().toString());
            }

            for (String v : expectedVariants) {
                Assert.assertTrue("Real variant present", observedVariants.contains(v));
            }

            samOutputTest(sampleAnalysis);

            vcfOutputTest(sampleAnalysis);
        }

        if (!rawReads) {
            TestUtil.serializationCheck(projectAnalysis);
        }
    }

    private void samOutputTest(SampleAnalysis sampleAnalysis) throws IOException {
        System.out.println("Testing SAM output");
        File file = new File("target/test." + sampleAnalysis.getSample().getName() + ".sam");

        SamWriter samWriter = new SamWriter(sampleAnalysis.getSample(),
                new FileOutputStream(file), sampleAnalysis.getConsensusAligner(),
                Platform.ILLUMINA);

        for (AlignedConsensus alignedConsensus : sampleAnalysis.getAlignmentDataList()) {
            samWriter.write(alignedConsensus);
        }

        samWriter.close();

        SamReader samReader = SamReaderFactory.makeDefault().open(file);
        int i = 0;
        for (SAMRecord samRecord : samReader) {
            if (++i <= 100) {
                System.out.println(samRecord);
            }
        }
    }

    private void vcfOutputTest(SampleAnalysis sampleAnalysis) throws IOException {
        System.out.println("Testing VCF output");

        File file = new File("target/test." + sampleAnalysis.getSample().getName() + ".vcf");

        VcfWriter vcfWriter = new VcfWriter(sampleAnalysis.getSample(),
                new FileOutputStream(file), sampleAnalysis.getVariantCaller(),
                Platform.ILLUMINA);

        for (Variant variant : sampleAnalysis.getVariantCaller().getVariants()) {
            vcfWriter.write(variant);
        }

        vcfWriter.close();

        VCFFileReader vcfFileReader = new VCFFileReader(file, false);

        int i = 0;
        for (VariantContext variantContext : vcfFileReader) {
            if (++i <= 100) {
                System.out.println(variantContext);
            }
        }
    }
}
