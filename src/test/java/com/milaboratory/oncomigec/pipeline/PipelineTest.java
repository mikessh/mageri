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

package com.milaboratory.oncomigec.pipeline;

import com.milaboratory.oncomigec.FastTests;
import com.milaboratory.oncomigec.TestUtil;
import com.milaboratory.oncomigec.core.mapping.AlignedConsensus;
import com.milaboratory.oncomigec.core.output.SamWriter;
import com.milaboratory.oncomigec.core.output.VcfWriter;
import com.milaboratory.oncomigec.core.variant.Variant;
import com.milaboratory.oncomigec.core.variant.VariantCaller;
import com.milaboratory.oncomigec.pipeline.analysis.ProjectAnalysis;
import com.milaboratory.oncomigec.pipeline.analysis.Sample;
import com.milaboratory.oncomigec.pipeline.analysis.SampleAnalysis;
import com.milaboratory.oncomigec.pipeline.input.Input;
import com.milaboratory.oncomigec.pipeline.input.InputParser;
import com.milaboratory.oncomigec.pipeline.input.ResourceIOProvider;
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

    public static final InputParser INPUT_PARSER = new InputParser(new ResourceIOProvider());

    @Test
    @Category(FastTests.class)
    public void fullTest() throws Exception {
        Input input = INPUT_PARSER.parseJson("pipeline/tabular.pri.json");

        //System.out.println(input);
        ProjectAnalysis projectAnalysis = new ProjectAnalysis(input);
        projectAnalysis.run();

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

        TestUtil.serializationCheck(projectAnalysis);
    }

    private void samOutputTest(SampleAnalysis sampleAnalysis) throws IOException {
        System.out.println("Testing SAM output");
        File file = new File("target/test." + sampleAnalysis.getSample().getName() + ".sam");

        SamWriter samWriter = new SamWriter(sampleAnalysis.getSample(),
                new FileOutputStream(file), sampleAnalysis.getConsensusAligner());

        for (AlignedConsensus alignedConsensus : sampleAnalysis.getAlignmentDataList()) {
            samWriter.write(alignedConsensus);
        }

        samWriter.close();

        SamReader samReader = SamReaderFactory.makeDefault().open(file);
        for (SAMRecord samRecord : samReader) {
            System.out.println(samRecord);
        }
    }

    private void vcfOutputTest(SampleAnalysis sampleAnalysis) throws IOException {
        System.out.println("Testing VCF output");

        File file = new File("target/test." + sampleAnalysis.getSample().getName() + ".vcf");

        VcfWriter vcfWriter = new VcfWriter(sampleAnalysis.getSample(),
                new FileOutputStream(file), sampleAnalysis.getVariantCaller());

        for (Variant variant : sampleAnalysis.getVariantCaller().getVariants()) {
            vcfWriter.write(variant);
        }

        vcfWriter.close();

        VCFFileReader vcfFileReader = new VCFFileReader(file, false);

        for (VariantContext variantContext : vcfFileReader) {
            System.out.println(variantContext);
        }
    }
}
