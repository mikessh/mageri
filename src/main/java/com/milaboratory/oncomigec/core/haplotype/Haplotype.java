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
package com.milaboratory.oncomigec.core.haplotype;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.Range;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.core.sequencing.read.SSequencingReadImpl;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.mutations.MutationDifference;
import com.milaboratory.oncomigec.core.mutations.wrappers.MutationWrapper;
import com.milaboratory.oncomigec.core.mutations.wrappers.MutationWrapperCollection;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;

public final class Haplotype implements Serializable {
    protected final Reference reference;
    protected double worstPvalue;
    protected LinkedList<Range> ranges;
    protected Range span;
    protected final HaplotypeCounters haplotypeCounters;
    protected int[] mutations;
    protected final Set<Integer> coverageMask;

    public Haplotype(Reference reference,
                     int[] mutations,
                     int migSize, double worstPvalue,
                     Set<Integer> coverageMask,
                     List<Range> ranges) {
        this.reference = reference;
        this.haplotypeCounters = new HaplotypeCounters();
        this.mutations = mutations;
        haplotypeCounters.incrementCount();
        haplotypeCounters.incrementReadCount(migSize);
        this.worstPvalue = worstPvalue;
        this.coverageMask = coverageMask;
        this.ranges = new LinkedList<>(ranges);
        simplifyRanges();
    }

    private void simplifyRanges() {
        Collections.sort(ranges, RangeComparator.INSTANCE);

        LinkedList<Range> newRanges = new LinkedList<>();

        Range currentRange = null;

        for (Range range : ranges) {
            if (currentRange != null) {
                if (currentRange.intersectsWithOrTouches(range)) {
                    // append and continue sliding
                    currentRange = new Range(currentRange.getFrom(), range.getTo());
                } else {
                    // failed to overlap with previous one
                    newRanges.add(currentRange);
                    // start sliding with me
                    currentRange = range;
                }
            } else {
                // start sliding with me
                currentRange = range;
            }
        }

        // add last
        newRanges.add(currentRange);

        this.ranges = newRanges;

        computeSpan();
    }

    private void computeSpan() {
        Range firstRange = ranges.getFirst(), lastRange = ranges.getLast();

        span = new Range(firstRange.getFrom(), lastRange.getTo());
    }

    void merge(Haplotype haplotype, List<Range> intersectionRanges) {
        haplotypeCounters.incrementCount();
        haplotypeCounters.incrementReadCount(haplotype.haplotypeCounters.getReadCount());
        worstPvalue = Math.max(worstPvalue, haplotype.worstPvalue);
        ranges.addAll(haplotype.ranges);
        coverageMask.addAll(haplotype.coverageMask);
        mutations = combineMutations(mutations, haplotype.mutations, intersectionRanges);
        simplifyRanges();
    }

    private static int[] combineMutations(int[] mutations1, int[] mutations2, List<Range> intersectionRanges) {
        List<Integer> mutationList = new LinkedList<>();

        for (int i = 0; i < mutations2.length; i++) {
            int mutation = mutations2[i];
            int pos = Mutations.getPosition(mutation);
            boolean duplicate = false;
            for (Range range : intersectionRanges) {
                if (range.contains(pos)) {
                    duplicate = true;
                    break;
                }
            }
            if (!duplicate)
                mutationList.add(mutation);
        }

        int[] combinedMutations = new int[mutations1.length + mutationList.size()];

        int i = 0;
        for (; i < mutations1.length; i++) {
            combinedMutations[i] = mutations1[i];
        }

        for (Integer mutation : mutationList) {
            combinedMutations[i++] = mutation;
        }

        return combinedMutations;
    }

    private NucleotideSequence getMutatedSequence() {
        return Mutations.mutate(reference.getSequence(), mutations);
    }

    public String getMaskedSequence() {
        NucleotideSequence haplotypeSequence = getMutatedSequence();

        StringBuilder sb = new StringBuilder();

        // todo: mismatches, insertions - lower case, deletions '-'

        for (int i = 0; i < haplotypeSequence.size(); i++)
            if (coverageMask.contains(i))
                sb.append('N');
            else
                sb.append(NucleotideAlphabet.INSTANCE.symbolFromCode(haplotypeSequence.codeAt(i)));

        return sb.toString();
    }

    public MutationDifference getMutationDifference(Haplotype other, Range range) {
        int[] myMutations = Mutations.extractMutationsForRange(mutations, range),
                hisMutations = Mutations.extractMutationsForRange(other.mutations, range);

        return new MutationDifference(reference,
                myMutations,
                hisMutations);
    }

    public double getWorstPointPvalue() {
        return worstPvalue;
    }

    public MutationWrapperCollection getMutations() {
        return new MutationWrapperCollection(reference, mutations);
    }

    public SSequencingRead asFastaRecord(int id) {
        return new SSequencingReadImpl(">" + id +
                " reference=" + getReference().getFullName() +
                " mutations=" + getMutationSignature(),
                new NucleotideSQPair(getMaskedSequence()), id);
    }

    public String getMutationSignature() {
        List<String> mutationSignatures = new ArrayList<>();
        for (MutationWrapper mutation : getMutations().getMutations()) {
            mutationSignatures.add(mutation.toString());
        }
        Collections.sort(mutationSignatures);

        return StringUtils.join(mutationSignatures, ",");
    }

    public HaplotypeCounters getHaplotypeCounters() {
        return haplotypeCounters;
    }

    public Reference getReference() {
        return reference;
    }

    public Range getSpan() {
        return span;
    }
}
