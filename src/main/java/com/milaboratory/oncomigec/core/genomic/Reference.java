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

public class Reference {
    private final int globalId;
    private final String name, fullName;
    private final NucleotideSequence sequence;
    private final boolean reverseComplement;

    public Reference(int globalId, String name, NucleotideSequence sequence, boolean reverseComplement) {
        this.globalId = globalId;
        this.name = name;
        this.sequence = sequence;
        this.reverseComplement = reverseComplement;
        this.fullName = name + (reverseComplement ? "_RC" : "");
    }

    public int getGlobalId() {
        return globalId;
    }

    @Deprecated
    public String getType() {
        return ".";
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isReverseComplement() {
        return reverseComplement;
    }

    public NucleotideSequence getSequence() {
        return sequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reference reference = (Reference) o;

        return globalId == reference.globalId;
    }

    @Override
    public int hashCode() {
        return globalId;
    }

    @Override
    public String toString() {
        return ">" + fullName + "\n" + sequence.toString();
    }
}
