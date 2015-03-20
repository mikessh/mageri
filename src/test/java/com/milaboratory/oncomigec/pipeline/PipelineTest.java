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

package com.milaboratory.oncomigec.pipeline;

import com.milaboratory.oncomigec.core.haplotype.Haplotype;
import com.milaboratory.oncomigec.core.haplotype.HaplotypeAssembler;
import com.milaboratory.oncomigec.core.io.misc.UmiHistogram;
import com.milaboratory.oncomigec.model.classifier.BaseVariantClassifier;
import com.milaboratory.oncomigec.pipeline.analysis.ProjectAnalysis;
import com.milaboratory.oncomigec.pipeline.analysis.Sample;
import com.milaboratory.oncomigec.pipeline.analysis.SampleAnalysis;
import com.milaboratory.oncomigec.pipeline.input.Input;
import com.milaboratory.oncomigec.pipeline.input.InputParser;
import com.milaboratory.oncomigec.util.testing.TestUtil;
import org.junit.Assert;
import org.junit.Test;

public class PipelineTest {
    public static final InputParser INPUT_PARSER = new InputParser(new TestIOProvider());

    @Test
    public void fullTest() throws Exception {
        Input input = INPUT_PARSER.parseJson("pipeline/tabular.pri.json");

        //System.out.println(input);
        ProjectAnalysis projectAnalysis = new ProjectAnalysis(input);
        projectAnalysis.run();

        for (Sample sample : projectAnalysis.getProject().getSamples()) {
            HaplotypeAssembler haplotypeAssembler = projectAnalysis.getAnalysis(sample).getHaplotypeAssembler();
            Assert.assertTrue(haplotypeAssembler.getFilteredHaplotypes().size() > 0);
            //Assert.assertTrue(haplotypeAssembler.getFilteredHaplotypes().size() <= 3);
            for (Haplotype haplotype : haplotypeAssembler.getFilteredHaplotypes()) {
                Assert.assertEquals(
                        projectAnalysis.getReferenceLibrary().getByName("SPIKE1"),
                        haplotype.getReference());
            }

            SampleAnalysis analysis = projectAnalysis.getAnalysis(sample);
            UmiHistogram umiHistogram = analysis.getUmiHistogram();
            Assert.assertEquals(umiHistogram.calculateMigsRetained(umiHistogram.getMigSizeThreshold()),
                    analysis.getAssembler().getMigsTotal());
        }

        projectAnalysis.serialize("./test_output/", false);

        TestUtil.serializationCheck(projectAnalysis);
    }
}
