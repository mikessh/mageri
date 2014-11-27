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
package com.milaboratory.migec2.core.align.reference;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;

import java.util.List;

public class Reference {
    private final int globalId;
    private final String name, type, fullName;
    private final NucleotideSequence sequence;
    private final boolean reverseComplement;
    private final boolean isDeNovo;

    public Reference(int globalId, String name, String type, NucleotideSequence sequence, boolean reverseComplement) {
        this.globalId = globalId;
        this.name = name;
        this.type = type;
        this.sequence = sequence;
        this.reverseComplement = reverseComplement;
        this.isDeNovo = false;
        this.fullName = name + "_" + type + (reverseComplement ? "_RC" : "");
    }

    public int getGlobalId() {
        return globalId;
    }

    public String getType() {
        return type;
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

    public boolean isDeNovo() {
        return isDeNovo;
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

    public static String toString(List<Reference> referenceList) {
        StringBuilder sb = new StringBuilder();

        int n = referenceList.size();

        for (int i = 0; i < n - 1; i++) {
            sb.append(referenceList.get(i).fullName);
            sb.append(",");
        }
        sb.append(referenceList.get(n - 1).fullName);

        return sb.toString();
    }
}
