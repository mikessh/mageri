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

import com.milaboratory.oncomigec.TestUtil;
import com.milaboratory.oncomigec.core.variant.Variant;
import com.milaboratory.oncomigec.core.variant.VariantCaller;
import com.milaboratory.oncomigec.pipeline.analysis.ProjectAnalysis;
import com.milaboratory.oncomigec.pipeline.analysis.Sample;
import com.milaboratory.oncomigec.pipeline.analysis.SampleAnalysis;
import com.milaboratory.oncomigec.pipeline.input.Input;
import com.milaboratory.oncomigec.pipeline.input.InputParser;
import com.milaboratory.oncomigec.pipeline.input.ResourceIOProvider;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class PipelineTest {

    public static final InputParser INPUT_PARSER = new InputParser(new ResourceIOProvider());

    @Test
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
        }

        TestUtil.serializationCheck(projectAnalysis);
    }

}
