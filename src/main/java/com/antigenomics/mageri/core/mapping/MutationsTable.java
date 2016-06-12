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
package com.antigenomics.mageri.core.mapping;

import com.milaboratory.core.sequence.Range;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.quality.SequenceQualityPhred;
import com.antigenomics.mageri.core.genomic.Reference;
import com.antigenomics.mageri.core.mutations.Mutation;
import com.antigenomics.mageri.core.mutations.MutationArray;
import com.antigenomics.mageri.core.mutations.Substitution;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public final class MutationsTable implements Serializable {
    private final Reference reference;
    private final AtomicInteger migCount;
    private final NucleotideMatrix majorMigs, minorMigs;
    private final QualitySumMatrix qualitySum;
    private final Set<Mutation> mutations;

    public MutationsTable(Reference reference) {
        this.reference = reference;
        int l = reference.getSequence().size();
        this.majorMigs = new NucleotideMatrix(l);
        this.minorMigs = new NucleotideMatrix(l);
        this.qualitySum = new QualitySumMatrix(l);
        this.migCount = new AtomicInteger();
        this.mutations = new HashSet<>();
    }

    public void append(LocalAlignment alignment, SequenceQualityPhred qual,
                       MutationArray majorMutations,
                       Set<Integer> minorMutations) {
        migCount.incrementAndGet();

        // Thread-safe
        Range coveredRange = alignment.getSequence1Range();

        // Virtual reference coverage
        for (int pos = coveredRange.getFrom(); pos < coveredRange.getTo(); pos++) {
            byte nt = reference.getSequence().codeAt(pos);

            int posInCons = alignment.convertPosition(pos);
            if (posInCons >= 0) {
                // MIG coverage
                qualitySum.increaseAt(pos,
                        nt, qual.value(posInCons));
            }

            // Counters
            majorMigs.incrementAt(pos, nt);
        }

        // Update with real reference (a set of major mutations)
        for (Mutation mutation : majorMutations.getMutations()) {
            if (mutation instanceof Substitution) {
                int code = ((Substitution) mutation).getCode();
                int pos = Mutations.getPosition(code),
                        to = Mutations.getTo(code),
                        from = Mutations.getFrom(code),
                        posInCons = alignment.convertPosition(pos);

                // Increment major counters and read accumulation
                majorMigs.incrementAt(pos, to);
                qualitySum.increaseAt(pos,
                        to, qual.value(posInCons));

                // Balance the reference
                majorMigs.decrementAt(pos, from);
                qualitySum.decreaseAt(pos,
                        from, qual.value(posInCons));

                mutations.add(mutation);
            } else {
                // TODO: IMPORTANT, INDELS
            }
        }

        // Update minor counters
        for (int code : minorMutations) {
            int pos = Mutations.getPosition(code), to = Mutations.getTo(code);
            minorMigs.incrementAt(pos, to);
        }
    }

    public Reference getReference() {
        return reference;
    }

    public byte getAncestralBase(int pos) {
        byte maxBase = 0;
        int maxCount = 0;
        for (byte base = 0; base < 4; base++) {
            int count = majorMigs.getAt(pos, base);
            if (count > maxCount) {
                maxCount = count;
                maxBase = base;
            }
        }
        return maxBase;
    }

    public int getMigCoverage(int pos) {
        int coverage = 0;

        for (int i = 0; i < 4; i++) {
            coverage += getMajorMigCount(pos, i);
        }

        return coverage;
    }

    public float getMeanCqs(int pos, int letterCode) {
        return (float) qualitySum.getAt(pos, letterCode) / getMajorMigCount(pos, letterCode);
    }

    public boolean hasReferenceBase(int pos) {
        return getMajorMigCount(pos, reference.getSequence().codeAt(pos)) > 0;
    }

    public int getMajorMigCount(int position, int letterCode) {
        return majorMigs.getAt(position, letterCode);
    }

    public int getMinorMigCount(int position, int letterCode) {
        return minorMigs.getAt(position, letterCode);
    }

    public int getMigCount() {
        return migCount.get();
    }

    public boolean wasUpdated() {
        return getMigCount() > 0;
    }

    public Set<Mutation> getMutations() {
        return Collections.unmodifiableSet(mutations);
    }

    public int length() {
        return reference.getSequence().size();
    }
}
