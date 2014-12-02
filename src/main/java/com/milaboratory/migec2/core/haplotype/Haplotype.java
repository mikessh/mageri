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
package com.milaboratory.migec2.core.haplotype;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.migec2.core.correct.CorrectedConsensus;
import com.milaboratory.migec2.core.mutations.MutationDifference;

import java.util.List;

public final class Haplotype {
    private final CorrectedConsensus correctedConsensus;
    private final NucleotideSequence haplotypeSequence, referenceSequence;
    private final String maskedSequence;

    public Haplotype(CorrectedConsensus correctedConsensus,
                     NucleotideSequence haplotypeSequence, NucleotideSequence referenceSequence,
                     String maskedSequence) {
        this.correctedConsensus = correctedConsensus;
        this.haplotypeSequence = haplotypeSequence;
        this.referenceSequence = referenceSequence;
        this.maskedSequence = maskedSequence;
    }

    public static List<MutationDifference> getMutationDifferences(Haplotype parent, Haplotype child) throws Exception {
        return CorrectedConsensus.getMutationDifferences(parent.correctedConsensus,
                child.correctedConsensus);
    }

    public NucleotideSequence getReferenceSequence() {
        return referenceSequence;
    }

    public NucleotideSequence getHaplotypeSequence() {
        return haplotypeSequence;
    }

    public double getWorstPointPvalue() {
        return correctedConsensus.getWorstPValue();
    }

    public String getMaskedSequence() {
        return maskedSequence;
    }

    public String getMutationsSignature() {
        StringBuilder sb = new StringBuilder();

        int n = correctedConsensus.getNumberOfReferences();

        for (int i = 0; i < n - 1; i++) {
            sb.append(correctedConsensus.getMajorMutations(i));
            sb.append(";");
        }
        sb.append(correctedConsensus.getMajorMutations(n - 1));

        return sb.toString();
    }

    public String getReferencesSignature() {
        StringBuilder sb = new StringBuilder();

        int n = correctedConsensus.getNumberOfReferences();

        for (int i = 0; i < n - 1; i++) {
            sb.append(correctedConsensus.getReference(i).getFullName());
            sb.append(";");
        }
        sb.append(correctedConsensus.getReference(n - 1).getFullName());

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Haplotype haplotype = (Haplotype) o;

        if (!haplotypeSequence.equals(haplotype.haplotypeSequence)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return haplotypeSequence.hashCode();
    }
}
