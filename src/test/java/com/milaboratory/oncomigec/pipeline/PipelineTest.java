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

public class PipelineTest {
    /*
    public static final InputParser INPUT_PARSER = new InputParser(new ResourceIOProvider());

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
            MigSizeDistribution migSizeDistribution = analysis.getMigSizeDistribution();
            Assert.assertEquals(migSizeDistribution.calculateMigsRetained(migSizeDistribution.getMigSizeThreshold()),
                    analysis.getAssembler().getMigsTotal());
        }

        projectAnalysis.serialize("./test_output/", false);

        TestUtil.serializationCheck(projectAnalysis);
    }
    */
}
