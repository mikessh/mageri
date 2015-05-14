/*
 * Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)
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
package com.milaboratory.oncomigec.core.genomic;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;

import java.io.Serializable;

public class Reference implements Serializable {
    private final int index;
    private final String name;
    private final NucleotideSequence sequence;
    private final ReferenceLibrary parent;
    private final GenomicInfo genomicInfo;

    public Reference(ReferenceLibrary parent,
                     int index, String name,
                     NucleotideSequence sequence,
                     GenomicInfo genomicInfo) {
        this.parent = parent;
        this.index = index;
        this.name = name;
        this.sequence = sequence;
        this.genomicInfo = genomicInfo;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public NucleotideSequence getSequence() {
        return sequence;
    }

    public GenomicInfo getGenomicInfo() {
        return genomicInfo;
    }

    public ReferenceLibrary getParent() {
        return parent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reference reference = (Reference) o;

        return index == reference.index;
    }

    @Override
    public int hashCode() {
        return index;
    }

    @Override
    public String toString() {
        return ">" + name + "\n" + sequence.toString();
    }
}
