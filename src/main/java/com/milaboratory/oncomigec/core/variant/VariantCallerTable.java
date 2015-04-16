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
 * Last modified on 9.4.2015 by mikesh
 */

package com.milaboratory.oncomigec.core.variant;

import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequenceBuilder;
import com.milaboratory.oncomigec.core.align.AlignerTable;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.mutations.Mutation;
import com.milaboratory.oncomigec.core.mutations.Substitution;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class VariantCallerTable {
    private final Reference reference;
    private final Map<Mutation, Variant> variantMap;

    public VariantCallerTable(VariantCaller variantCaller,
                              AlignerTable alignerTable,
                              ErrorModel errorModel) {
        this.reference = alignerTable.getReference();
        this.variantMap = new HashMap<>();

        for (Mutation mutation : alignerTable.getMutations()) {
            if (mutation instanceof Substitution) {
                int code = ((Substitution) mutation).getCode(),
                        pos = Mutations.getPosition(code),
                        base = Mutations.getTo(code);

                int majorCount = alignerTable.getMajorMigCount(pos, base);

                assert majorCount > 0;

                int coverage = alignerTable.getMigCoverage(pos);

                double score = errorModel.getLog10PValue(
                        majorCount,
                        alignerTable.getMinorMigCount(pos, base),
                        coverage);

                NucleotideSequenceBuilder nsb = new NucleotideSequenceBuilder(1);
                nsb.setCode(0, alignerTable.getAncestralBase(pos));

                Variant variant = new Variant(reference,
                        mutation, majorCount,
                        alignerTable.getMigCoverage(pos),
                        majorCount / (double) coverage, score,
                        nsb.create(), alignerTable.hasReferenceBase(pos));

                variant.filter(variantCaller);

                variantMap.put(mutation, variant);
            } else {
                // TODO: IMPORTANT: INDELS
            }
        }
    }

    public Variant getVariant(Mutation mutation) {
        return variantMap.get(mutation);
    }

    public Collection<Variant> getVariants() {
        return Collections.unmodifiableCollection(variantMap.values());
    }

    public Reference getReference() {
        return reference;
    }
}
