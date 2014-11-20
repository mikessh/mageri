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
package com.milaboratory.migec2.core.consalign.mutations;

import com.milaboratory.core.sequence.Range;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import com.milaboratory.migec2.core.align.reference.Reference;
import com.milaboratory.migec2.core.assemble.entity.SConsensus;
import com.milaboratory.migec2.core.mutations.MigecMutation;
import com.milaboratory.migec2.core.mutations.MigecMutationsCollection;
import com.milaboratory.migec2.util.Basics;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLongArray;

public final class MutationsAndCoverage {
    private final Reference reference;
    private final int referenceLength;
    private final ConcurrentHashMap<Integer, AtomicInteger> majorIndelMigCountMap, minorIndelMigCountMap,
            majorIndelReadCountMap, minorIndelReadCountMap;
    private final AtomicIntegerArray referenceUmiCoverage;
    private final AtomicLongArray referenceReadCoverage, referenceQualitySumCoverage;
    private final NucleotideCoverage majorLetterMigCounts, minorLetterMigCounts,
            majorLetterReadCounts, minorLetterReadCounts;
    private AtomicBoolean updated = new AtomicBoolean(false);
    private int migCount = -1;

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
        this.majorIndelMigCountMap = new ConcurrentHashMap<>();
        this.minorIndelMigCountMap = new ConcurrentHashMap<>();
        this.majorIndelReadCountMap = new ConcurrentHashMap<>();
        this.minorIndelReadCountMap = new ConcurrentHashMap<>();
    }

    public void appendCoverage(LocalAlignment alignment, SConsensus consensus, int migSize) {
        // Thread-safe
        updated.compareAndSet(false, true);
        Range coveredRange = alignment.getSequence1Range();
        for (int i = coveredRange.getFrom(); i < coveredRange.getTo(); i++) {
            int posInCons = alignment.convertPosition(i);
            if (posInCons >= 0) {
                referenceUmiCoverage.incrementAndGet(i);
                referenceReadCoverage.addAndGet(i, migSize);
                referenceQualitySumCoverage.addAndGet(i, consensus.getConsensusSQPair().getQuality().value(posInCons));
            }
        }
    }

    public void appendMutations(MigecMutationsCollection majorMutations,
                                Map<Integer, Integer> minorMutations, int migSize) {
        updated.compareAndSet(false, true);

        // append reference to major by default
        for (int i = 0; i < referenceLength; i++) {
            majorLetterReadCounts.incrementCoverage(i, reference.getSequence().codeAt(i), migSize);
            majorLetterMigCounts.incrementCoverage(i, reference.getSequence().codeAt(i));
        }

        for (MigecMutation mutation : majorMutations) {
            if (mutation.isSubstitution()) {
                final int pos = mutation.pos(), to = mutation.to(),
                        from = mutation.from();
                majorLetterReadCounts.incrementCoverage(pos, to, migSize);
                majorLetterReadCounts.decrementCoverage(pos, from, migSize);
                // Also balance reference
                majorLetterMigCounts.incrementCoverage(pos, to);
                majorLetterMigCounts.decrementCoverage(pos, from);
            } else {
                Basics.incrementAICounter(majorIndelReadCountMap, mutation.code(), migSize);
                Basics.incrementAICounter(majorIndelMigCountMap, mutation.code());
            }
        }
        for (Map.Entry<Integer, Integer> mutationEntry : minorMutations.entrySet()) {
            int mutationCode = mutationEntry.getKey();
            if (Mutations.isSubstitution(mutationCode)) {
                minorLetterReadCounts.incrementCoverage(Mutations.getPosition(mutationCode),
                        Mutations.getTo(mutationCode), mutationEntry.getValue());
                minorLetterMigCounts.incrementCoverage(Mutations.getPosition(mutationCode),
                        Mutations.getTo(mutationCode));
            } else {
                Basics.incrementAICounter(minorIndelReadCountMap, mutationCode, mutationEntry.getValue());
                Basics.incrementAICounter(minorIndelMigCountMap, mutationCode);
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

    @Override
    public String toString() {
        StringBuilder formattedString = new StringBuilder("NumberOfMIGs=").append(getMigCount()).append("\n");
        formattedString.append("CoverageType\tCountType\tNucleotide");

        for (int i = 0; i < referenceLength; i++)
            formattedString.append("\t").append(i + 1);

        formattedString.append("\nBaseCoverage\tRead\t-");
        for (int i = 0; i < referenceLength; i++)
            formattedString.append("\t").append(referenceReadCoverage.get(i));

        formattedString.append("\nBaseCoverage\tUMI\t-");
        for (int i = 0; i < referenceLength; i++)
            formattedString.append("\t").append(referenceUmiCoverage.get(i));

        formattedString.append("\nCQS\t-\t-");
        for (int i = 0; i < referenceLength; i++)
            formattedString.append("\t").append(referenceQualitySumCoverage.get(i));

        // MAJOR - MIGS

        // substitutions - migs
        for (int j = 0; j < 4; j++) {
            formattedString.append("\nMAJOR\tMIG\t");
            formattedString.append(NucleotideAlphabet.INSTANCE.symbolFromCode((byte) j));
            for (int i = 0; i < referenceLength; i++)
                formattedString.append("\t").append(getMajorNucleotideMigCount(i, j));
        }

        // insertions - migs
        for (int j = 0; j < 4; j++) {
            formattedString.append("\nMAJOR\tMIG\tI:");
            formattedString.append(NucleotideAlphabet.INSTANCE.symbolFromCode((byte) j));
            for (int i = 0; i < referenceLength; i++) {
                int code = Mutations.createInsertion(i, j);
                formattedString.append("\t").append(getMajorIndelMigCount(code));
            }
        }

        // deletions - migs
        for (int j = 0; j < 4; j++) {
            formattedString.append("\nMAJOR\tMIG\tD:");
            formattedString.append(NucleotideAlphabet.INSTANCE.symbolFromCode((byte) j));
            for (int i = 0; i < referenceLength; i++) {
                int code = Mutations.createDeletion(i, j);
                formattedString.append("\t").append(getMajorIndelMigCount(code));
            }
        }

        // MINOR - MIGS

        // substitutions - migs
        for (int j = 0; j < 4; j++) {
            formattedString.append("\nMINOR\tMIG\t");
            formattedString.append(NucleotideAlphabet.INSTANCE.symbolFromCode((byte) j));
            for (int i = 0; i < referenceLength; i++)
                formattedString.append("\t").append(getMinorNucleotideMigCount(i, j));
        }

        // insertions - migs
        for (int j = 0; j < 4; j++) {
            formattedString.append("\nMINOR\tMIG\tI:");
            formattedString.append(NucleotideAlphabet.INSTANCE.symbolFromCode((byte) j));
            for (int i = 0; i < referenceLength; i++) {
                int code = Mutations.createInsertion(i, j);
                formattedString.append("\t").append(getMinorIndelMigCount(code));
            }
        }

        // deletions - migs
        for (int j = 0; j < 4; j++) {
            formattedString.append("\nMINOR\tMIG\tD:");
            formattedString.append(NucleotideAlphabet.INSTANCE.symbolFromCode((byte) j));
            for (int i = 0; i < referenceLength; i++) {
                int code = Mutations.createDeletion(i, j);
                formattedString.append("\t").append(getMinorIndelMigCount(code));
            }
        }

        // MAJOR - READS

        // substitutions - reads
        for (int j = 0; j < 4; j++) {
            formattedString.append("\nMAJOR\tREAD\t");
            formattedString.append(NucleotideAlphabet.INSTANCE.symbolFromCode((byte) j));
            for (int i = 0; i < referenceLength; i++)
                formattedString.append("\t").append(getMajorNucleotideReadCount(i, j));
        }

        // insertions - reads
        for (int j = 0; j < 4; j++) {
            formattedString.append("\nMAJOR\tREAD\tI:");
            formattedString.append(NucleotideAlphabet.INSTANCE.symbolFromCode((byte) j));
            for (int i = 0; i < referenceLength; i++) {
                int code = Mutations.createInsertion(i, j);
                formattedString.append("\t").append(getMajorIndelReadCount(code));
            }
        }

        // deletions - reads
        for (int j = 0; j < 4; j++) {
            formattedString.append("\nMAJOR\tREAD\tD:");
            formattedString.append(NucleotideAlphabet.INSTANCE.symbolFromCode((byte) j));
            for (int i = 0; i < referenceLength; i++) {
                int code = Mutations.createDeletion(i, j);
                formattedString.append("\t").append(getMajorIndelReadCount(code));
            }
        }

        // MINOR - READS

        // substitutions - reads
        for (int j = 0; j < 4; j++) {
            formattedString.append("\nMINOR\tREAD\t");
            formattedString.append(NucleotideAlphabet.INSTANCE.symbolFromCode((byte) j));
            for (int i = 0; i < referenceLength; i++)
                formattedString.append("\t").append(getMinorNucleotideReadCount(i, j));
        }

        // insertions - reads
        for (int j = 0; j < 4; j++) {
            formattedString.append("\nMINOR\tREAD\tI:");
            formattedString.append(NucleotideAlphabet.INSTANCE.symbolFromCode((byte) j));
            for (int i = 0; i < referenceLength; i++) {
                int code = Mutations.createInsertion(i, j);
                formattedString.append("\t").append(getMinorIndelReadCount(code));
            }
        }

        // deletions - reads
        for (int j = 0; j < 4; j++) {
            formattedString.append("\nMINOR\tREAD\tD:");
            formattedString.append(NucleotideAlphabet.INSTANCE.symbolFromCode((byte) j));
            for (int i = 0; i < referenceLength; i++) {
                int code = Mutations.createDeletion(i, j);
                formattedString.append("\t").append(getMinorIndelReadCount(code));
            }
        }



        return formattedString.toString();
    }
}
