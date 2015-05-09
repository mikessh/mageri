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
 * Last modified on 13.3.2015 by mikesh
 */

package com.milaboratory.oncomigec.pipeline.analysis;

import com.milaboratory.oncomigec.core.ReadSpecific;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SampleGroup implements Serializable, ReadSpecific {
    private final Project parent;
    private final boolean pairedEnd;
    private final String name;
    private final List<Sample> samples = new ArrayList<>();

    public SampleGroup(String name, boolean pairedEnd, Project parent) {
        this.name = name;
        this.pairedEnd = pairedEnd;
        this.parent = parent;
    }

    public Sample createSample(String name) {
        Sample sample = new Sample(name, this);
        samples.add(sample);
        parent.addSample(sample);
        return sample;
    }

    public Sample createSample() {
        Sample sample = new Sample(this);
        samples.add(sample);
        parent.addSample(sample);
        return sample;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return parent.getName() + "." + getName();
    }

    public List<Sample> getSamples() {
        return Collections.unmodifiableList(samples);
    }

    public Project getParent() {
        return parent;
    }

    @Override
    public boolean isPairedEnd() {
        return pairedEnd;
    }
}
