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

import com.milaboratory.oncomigec.core.ReadSpecific;

import java.io.Serializable;

public class Sample implements Comparable<Sample>, Serializable, ReadSpecific {
    private final SampleGroup parent;
    private final String name;
    private int id = -1;
    private final boolean submultiplexed;

    public static Sample create(String sampleName, boolean pairedEnd, String groupName, String projectName) {
        return new Project(projectName).createSampleGroup(groupName, pairedEnd).createSample(sampleName);
    }

    public static Sample create(String sampleName, boolean pairedEnd) {
        return create(sampleName, pairedEnd, "dummy", "dummy");
    }

    public Sample(String name, SampleGroup parent) {
        this.name = name;
        this.parent = parent;
        this.submultiplexed = true;
    }

    protected Sample(SampleGroup parent) {
        this.name = null;
        this.parent = parent;
        this.submultiplexed = false;
    }

    public SampleGroup getParent() {
        return parent;
    }

    public int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return submultiplexed ? name : parent.getName();
    }

    public String getGroupName() {
        return parent.getName();
    }

    public String getProjectName() {
        return parent.getParent().getName();
    }

    public String getFullName() {
        return parent.getFullName() + (submultiplexed ? ("." + name) : "");
    }

    public boolean isSubmultiplexed() {
        return submultiplexed;
    }

    @Override
    public int compareTo(Sample o) {
        return this.getFullName().compareTo(o.getFullName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sample sample = (Sample) o;

        if (!getFullName().equals(sample.getFullName())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getFullName().hashCode();
    }

    @Override
    public boolean isPairedEnd() {
        return parent.isPairedEnd();
    }
}
