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
package com.milaboratory.oncomigec.core.align;

import com.milaboratory.core.sequence.Range;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.quality.SequenceQualityPhred;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.mutations.Mutation;
import com.milaboratory.oncomigec.core.mutations.MutationArray;
import com.milaboratory.oncomigec.core.mutations.Substitution;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLongArray;

public final class AlignerTable implements Serializable {
    private final Reference reference;
    private final AtomicInteger migCount;
    private final NucleotideCoverage majorMigs, minorMigs;
    private final AtomicIntegerArray migCoverage;
    private final AtomicLongArray cqsSumCoverage;
    private final Set<Mutation> mutations;

    public AlignerTable(Reference reference) {
        this.reference = reference;
        int l = reference.getSequence().size();
        this.majorMigs = new NucleotideCoverage(l);
        this.minorMigs = new NucleotideCoverage(l);
        this.migCoverage = new AtomicIntegerArray(l);
        this.cqsSumCoverage = new AtomicLongArray(l);
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
        for (int i = coveredRange.getFrom(); i < coveredRange.getTo(); i++) {
            migCoverage.incrementAndGet(i);

            int posInCons = alignment.convertPosition(i);
            if (posInCons >= 0) {
                // MIG coverage
                cqsSumCoverage.addAndGet(i, qual.value(posInCons));
            }

            // Counters
            byte nt = reference.getSequence().codeAt(i);
            majorMigs.incrementCoverage(i, nt);
        }

        // Update with real reference (a set of major mutations)
        for (Mutation mutation : majorMutations.getMutations()) {
            if (mutation instanceof Substitution) {
                int code = ((Substitution) mutation).getCode();
                int pos = Mutations.getPosition(code),
                        to = Mutations.getTo(code),
                        from = Mutations.getFrom(code);

                // Increment major counters and read accumulation
                majorMigs.incrementCoverage(pos, to);

                // Balance the reference
                majorMigs.decrementCoverage(pos, from);

                mutations.add(mutation);
            } else {
                // TODO: IMPORTANT, INDELS
            }
        }

        // Update minor counters
        for (int code : minorMutations) {
            int pos = Mutations.getPosition(code), to = Mutations.getTo(code);
            minorMigs.incrementCoverage(pos, to);
        }
    }

    public Reference getReference() {
        return reference;
    }

    public byte getAncestralBase(int pos) {
        byte maxBase = 0;
        int maxCount = 0;
        for (byte base = 0; base < 4; base++) {
            int count = majorMigs.getCoverage(pos, base);
            if (count > maxCount) {
                maxCount = count;
                maxBase = base;
            }
        }
        return maxBase;
    }

    public int getMigCoverage(int pos) {
        return migCoverage.get(pos);
    }

    public long getCqsSumCoverage(int pos) {
        return cqsSumCoverage.get(pos);
    }

    public boolean hasReferenceBase(int pos) {
        return getMajorMigCount(pos, reference.getSequence().codeAt(pos)) > 0;
    }

    public int getMajorMigCount(int position, int letterCode) {
        return majorMigs.getCoverage(position, letterCode);
    }

    public int getMinorMigCount(int position, int letterCode) {
        return minorMigs.getCoverage(position, letterCode);
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
}
