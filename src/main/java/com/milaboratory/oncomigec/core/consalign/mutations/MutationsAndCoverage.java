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
package com.milaboratory.oncomigec.core.consalign.mutations;

import com.milaboratory.core.sequence.Range;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.quality.SequenceQualityPhred;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.mutations.MigecMutation;
import com.milaboratory.oncomigec.core.mutations.MigecMutationsCollection;
import com.milaboratory.oncomigec.util.Basics;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLongArray;

public final class MutationsAndCoverage implements Serializable {
    private final Reference reference;
    private final int referenceLength;
    private final ConcurrentHashMap<Integer, AtomicInteger> majorIndelMigCountMap, minorIndelMigCountMap,
            majorIndelReadCountMap, minorIndelReadCountMap;
    private final AtomicIntegerArray referenceUmiCoverage;
    private final AtomicLongArray referenceReadCoverage, referenceQualitySumCoverage;
    private final NucleotideCoverage majorLetterMigCounts, minorLetterMigCounts,
            majorLetterReadCounts, minorLetterReadCounts, accumLetterReadCounts;
    private AtomicBoolean updated = new AtomicBoolean(false);
    private int migCount = -1;
    private long readCount = -1;

    public MutationsAndCoverage(Reference reference) {
        this.reference = reference;
        this.referenceLength = reference.getSequence().size();
        this.referenceUmiCoverage = new AtomicIntegerArray(referenceLength);
        this.referenceReadCoverage = new AtomicLongArray(referenceLength);
        this.referenceQualitySumCoverage = new AtomicLongArray(referenceLength);
        this.majorLetterMigCounts = new NucleotideCoverage(referenceLength);
        this.minorLetterMigCounts = new NucleotideCoverage(referenceLength);
        this.majorLetterReadCounts = new NucleotideCoverage(referenceLength);
        this.minorLetterReadCounts = new NucleotideCoverage(referenceLength);
        this.accumLetterReadCounts = new NucleotideCoverage(referenceLength);
        this.majorIndelMigCountMap = new ConcurrentHashMap<>();
        this.minorIndelMigCountMap = new ConcurrentHashMap<>();
        this.majorIndelReadCountMap = new ConcurrentHashMap<>();
        this.minorIndelReadCountMap = new ConcurrentHashMap<>();
    }

    public void append(LocalAlignment alignment, SequenceQualityPhred qual,
                       MigecMutationsCollection majorMutations, MinorMutationData minorMutations,
                       boolean appendReference) {
        // Thread-safe
        updated.compareAndSet(false, true);
        int migSize = minorMutations.getMigSize();
        Range coveredRange = alignment.getSequence1Range();
        
        for (int i = coveredRange.getFrom(); i < coveredRange.getTo(); i++) {
            int posInCons = alignment.convertPosition(i);
            if (posInCons >= 0) {
                referenceUmiCoverage.incrementAndGet(i);
                referenceReadCoverage.addAndGet(i, migSize);
                referenceQualitySumCoverage.addAndGet(i, qual.value(posInCons));

                byte nt = reference.getSequence().codeAt(i);
                majorLetterMigCounts.incrementCoverage(i, nt);
                majorLetterReadCounts.incrementCoverage(i, nt, migSize);
                accumLetterReadCounts.incrementCoverage(i, nt, minorMutations.getGainedReadCount(i));
            }
        }

        for (MigecMutation mutation : majorMutations) {
            if (mutation.isSubstitution()) {
                final int pos = mutation.pos(), to = mutation.to(),
                        from = mutation.from();
                // Increment major counters and read accumulation
                majorLetterMigCounts.incrementCoverage(pos, to);
                majorLetterReadCounts.incrementCoverage(pos, to, migSize);
                int gain = minorMutations.getGainedReadCount(pos);
                accumLetterReadCounts.incrementCoverage(pos, to, gain);

                // Balance the reference
                majorLetterMigCounts.decrementCoverage(pos, from);
                majorLetterReadCounts.decrementCoverage(pos, from, migSize);
                accumLetterReadCounts.decrementCoverage(pos, from, gain);
            } else {
                Basics.incrementAICounter(majorIndelReadCountMap, mutation.code(), migSize);
                Basics.incrementAICounter(majorIndelMigCountMap, mutation.code());
            }
        }
        for (int code : minorMutations.getCodes()) {
            if (Mutations.isSubstitution(code)) {
                int pos = Mutations.getPosition(code), to = Mutations.getTo(code);
                minorLetterReadCounts.incrementCoverage(pos, to, minorMutations.getLostReadCount(code));
                minorLetterMigCounts.incrementCoverage(pos, to);
            } else {
                Basics.incrementAICounter(minorIndelReadCountMap, code, minorMutations.getLostReadCount(code));
                Basics.incrementAICounter(minorIndelMigCountMap, code);
            }
        }
    }

    public Reference getReference() {
        return reference;
    }

    public int referenceLength() {
        return referenceLength;
    }

    public int getMigCount() {
        if (migCount < 0) {
            // account for paired
            migCount = 0;
            for (int i = 0; i < referenceLength; i++)
                migCount = Math.max(migCount, referenceUmiCoverage.get(i));
        }
        return migCount;
    }

    public long getReadCount() {
        if (readCount < 0) {
            // account for paired
            readCount = 0;
            for (int i = 0; i < referenceLength; i++)
                readCount = Math.max(readCount, referenceReadCoverage.get(i));
        }
        return readCount;
    }

    public long getReferenceUmiCoverage(int pos) {
        return referenceUmiCoverage.get(pos);
    }

    public long getReferenceReadCoverage(int pos) {
        return referenceReadCoverage.get(pos);
    }

    public long getReferenceQualitySumCoverage(int pos) {
        return referenceQualitySumCoverage.get(pos);
    }

    public Set<Integer> getAllIndelCodes() {
        Set<Integer> indels = new HashSet<>(majorIndelMigCountMap.keySet());
        indels.addAll(minorIndelMigCountMap.keySet());
        return indels;
    }

    public Set<Integer> getMajorIndelCodes() {
        return new HashSet<>(majorIndelMigCountMap.keySet());
    }

    public int getMajorNucleotideMigCount(int position, int letterCode) {
        return majorLetterMigCounts.getCoverage(position, letterCode);
    }

    public int getMinorNucleotideMigCount(int position, int letterCode) {
        return minorLetterMigCounts.getCoverage(position, letterCode);
    }

    public int getMajorNucleotideReadCount(int position, int letterCode) {
        return majorLetterReadCounts.getCoverage(position, letterCode);
    }

    public int getMinorNucleotideReadCount(int position, int letterCode) {
        return minorLetterReadCounts.getCoverage(position, letterCode);
    }

    public int getGainedNucleotideReadCount(int position, int letterCode) {
        return accumLetterReadCounts.getCoverage(position, letterCode);
    }

    public int getMajorIndelMigCount(int indelCode) {
        return Basics.nullToZero(majorIndelMigCountMap.get(indelCode));
    }

    public int getMinorIndelMigCount(int indelCode) {
        return Basics.nullToZero(minorIndelMigCountMap.get(indelCode));
    }

    public int getMajorIndelReadCount(int indelCode) {
        return Basics.nullToZero(majorIndelReadCountMap.get(indelCode));
    }

    public int getMinorIndelReadCount(int indelCode) {
        return Basics.nullToZero(minorIndelReadCountMap.get(indelCode));
    }

    public boolean wasUpdated() {
        return updated.get();
    }
}
