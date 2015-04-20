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
 * Last modified on 12.3.2015 by mikesh
 */

package com.milaboratory.oncomigec.pipeline.analysis;

import com.milaboratory.oncomigec.pipeline.input.Input;
import com.milaboratory.oncomigec.pipeline.input.InputChunk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

// Project structure that could be deduced from input
public class Project implements Serializable {
    private final List<SampleGroup> sampleGroups = new ArrayList<>();
    private final List<Sample> samples = new ArrayList<>();
    private final HashMap<Integer, Sample> sampleById = new HashMap<>();
    private final Input input;

    static Project fromInput(Input input) {
        Project project = new Project(input);

        int sampleId = 0;

        for (InputChunk inputChunk : input.getInputChunks()) {
            SampleGroup group = new SampleGroup(inputChunk, project);
            if (inputChunk.getCheckoutRule().hasSubMultiplexing()) {
                for (String sampleName : inputChunk.getCheckoutRule().getSampleNames()) {
                    Sample sample = new Sample(sampleId++, sampleName, group);
                    project.addSample(sample);
                }
            } else {
                Sample sample = new Sample(sampleId++, null, group);
                project.addSample(sample);
                group.addSample(sample);
            }
            project.sampleGroups.add(group);
        }

        return project;
    }

    private void addSample(Sample sample) {
        samples.add(sample);
        sampleById.put(sample.getId(), sample);
    }

    private Project(Input input) {
        this.input = input;
    }

    public String getName() {
        return input.getProjectName();
    }

    public List<Sample> getSamples() {
        return Collections.unmodifiableList(samples);
    }

    public List<SampleGroup> getSampleGroups() {
        return Collections.unmodifiableList(sampleGroups);
    }

    public Sample getSample(int id) {
        return sampleById.get(id);
    }
}
