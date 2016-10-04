/*
 * Copyright 2014-2016 Mikhail Shugay
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
package com.antigenomics.mageri.core.genomic;

import com.milaboratory.core.sequence.Alphabet;
import com.milaboratory.core.sequence.Sequence;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;

import java.io.Serializable;

public class Reference extends Sequence implements Serializable {
    private final int index;
    private final String name;
    private final NucleotideSequence sequence;
    private final ReferenceLibrary parent;
    private final GenomicInfo genomicInfo;
    private final int nMaskedBases;

    public Reference(ReferenceLibrary parent,
                     int index, String name,
                     NucleotideSequence sequence,
                     GenomicInfo genomicInfo) {
        this(parent, index, name, sequence, genomicInfo, 0);
    }

    public Reference(ReferenceLibrary parent,
                     int index, String name,
                     NucleotideSequence sequence,
                     GenomicInfo genomicInfo,
                     int nMaskedBases) {
        this.parent = parent;
        this.index = index;
        this.name = name;
        this.sequence = sequence;
        this.genomicInfo = genomicInfo;
        this.nMaskedBases = nMaskedBases;
    }

    public int getnMaskedBases() {
        return nMaskedBases;
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
    public byte codeAt(int i) {
        return sequence.codeAt(i);
    }

    @Override
    public int size() {
        return sequence.size();
    }

    @Override
    public Alphabet getAlphabet() {
        return NucleotideAlphabet.INSTANCE;
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

    public static final String HEADER = GenomicInfo.HEADER + "\tname\tsequence";

    @Override
    public String toString() {
        return genomicInfo.toString() + "\t" + name + "\t" + sequence.toString();
    }
}
