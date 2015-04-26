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

package com.milaboratory.oncomigec.pipeline.analysis;

import com.milaboratory.oncomigec.pipeline.input.ResourceIOProvider;
import com.milaboratory.oncomigec.pipeline.input.InputParser;
import com.milaboratory.oncomigec.TestUtil;
import org.junit.Test;

import java.io.IOException;

public class ProjectTest {
    @Test
    public void test() throws IOException {
        test("pipeline/tabular.pos.json");
        test("pipeline/tabular.pre.json");
        test("pipeline/tabular.pri.json");
        test("pipeline/tabular.sub.json");
    }

    private void test(String json) throws IOException {
        InputParser inputParser = new InputParser(new ResourceIOProvider());

        Project project = Project.fromInput(inputParser.parseJson(json));

        TestUtil.serializationCheck(project);
    }
}
