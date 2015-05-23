/*
 * Copyright (c) 2014-2015, Bolotin Dmitry, Chudakov Dmitry, Shugay Mikhail
 * (here and after addressed as Inventors)
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact the Inventors using one of the following
 * email addresses: chudakovdm@mail.ru, chudakovdm@gmail.com
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */
package com.milaboratory.oncomigec.core.mapping;

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

public final class MutationsTable implements Serializable {
    private final Reference reference;
    private final AtomicInteger migCount;
    private final NucleotideMatrix majorMigs, minorMigs;
    private final AtomicIntegerArray migCoverage;
    private final AtomicLongArray cqsSumCoverage;
    private final CoverageAndQuality coverageAndQuality;
    private final Set<Mutation> mutations;

    public MutationsTable(Reference reference) {
        this.reference = reference;
        int l = reference.getSequence().size();
        this.majorMigs = new NucleotideMatrix(l);
        this.minorMigs = new NucleotideMatrix(l);
        this.coverageAndQuality = new CoverageAndQuality(l);
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
        for (int pos = coveredRange.getFrom(); pos < coveredRange.getTo(); pos++) {
            byte nt = reference.getSequence().codeAt(pos);
            //migCoverage.incrementAndGet(i);

            int posInCons = alignment.convertPosition(pos);
            if (posInCons >= 0) {
                // MIG coverage
                //cqsSumCoverage.addAndGet(i, qual.value(posInCons));
                coverageAndQuality.increaseQualityAndCoverage(pos,
                        nt, qual.value(posInCons));
            }

            // Counters
            majorMigs.incrementCount(pos, nt);
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
                majorMigs.incrementCount(pos, to);
                coverageAndQuality.increaseQualityAndCoverage(pos,
                        to, qual.value(posInCons));

                // Balance the reference
                majorMigs.decrementCount(pos, from);
                coverageAndQuality.decreaseQualityAndCoverage(pos,
                        from, qual.value(posInCons));

                mutations.add(mutation);
            } else {
                // TODO: IMPORTANT, INDELS
            }
        }

        // Update minor counters
        for (int code : minorMutations) {
            int pos = Mutations.getPosition(code), to = Mutations.getTo(code);
            minorMigs.incrementCount(pos, to);
        }
    }

    public Reference getReference() {
        return reference;
    }

    public byte getAncestralBase(int pos) {
        byte maxBase = 0;
        int maxCount = 0;
        for (byte base = 0; base < 4; base++) {
            int count = majorMigs.getCount(pos, base);
            if (count > maxCount) {
                maxCount = count;
                maxBase = base;
            }
        }
        return maxBase;
    }

    public int getMigCoverage(int pos) {
        //return migCoverage.get(pos);
        return coverageAndQuality.getCoverage(pos);
    }

    public double getMeanCqs(int pos, int letterCode) {
        //return cqsSumCoverage.get(pos);
        return coverageAndQuality.getAverageQuality(pos, letterCode);
    }

    public boolean hasReferenceBase(int pos) {
        return getMajorMigCount(pos, reference.getSequence().codeAt(pos)) > 0;
    }

    public int getMajorMigCount(int position, int letterCode) {
        return majorMigs.getCount(position, letterCode);
    }

    public int getMinorMigCount(int position, int letterCode) {
        return minorMigs.getCount(position, letterCode);
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
