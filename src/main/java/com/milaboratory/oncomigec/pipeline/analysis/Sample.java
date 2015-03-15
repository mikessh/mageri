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

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.Serializable;

public class Sample implements Comparable<Sample>, Serializable {
    private final SampleGroup parent;
    private final String name;

    Sample(@Nullable String name, @NotNull SampleGroup parent) {
        this.name = name;
        this.parent = parent;
    }

    public SampleGroup getParent() {
        return parent;
    }

    public String getName() {
        return name != null ? name : parent.getName();
    }

    public String getFullName() {
        return parent.getFullName() + (name != null ? ("." + name) : "");
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
}
