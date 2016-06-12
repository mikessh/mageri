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

package com.antigenomics.mageri.core.mutations;

import com.antigenomics.mageri.core.genomic.Reference;
import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.quality.SequenceQualityPhred;

import java.util.HashSet;
import java.util.Set;

import static com.milaboratory.core.sequence.mutations.Mutations.*;
import static com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet.getComplement;

public final class MutationsExtractor {
    private final LocalAlignment consensusAlignment;
    private final NucleotideSQPair consensus;
    private final Reference reference;
    private final Set<Integer> minors;
    private final int[] consensusMutations, invertedConsensusMutations;
    private final boolean rc;

    public MutationsExtractor(LocalAlignment consensusAlignment,
                              Reference reference,
                              NucleotideSQPair consensus,
                              Set<Integer> minors,
                              byte consQualThreshold,
                              boolean rc) {
        this.consensusAlignment = consensusAlignment;
        this.reference = reference;
        this.minors = minors;
        this.rc = rc;
        this.consensus = consensus;

        // Extract ref->cons mutations (MAJORS)
        // Filter possible spurious mutations that arose due to inexact alignment
        // Then filter by CQS quality threshold
        int[] mutations = computeMajorMutations(rc ? consensus.getQuality().reverse() : consensus.getQuality(),
                computeMutations(reference.getSequence(), consensusAlignment),
                consensusAlignment,
                consQualThreshold);

        // Move ref->cons mutations (MAJORS) to absolute reference coordinates
        this.consensusMutations = Mutations.move(mutations, consensusAlignment.getSequence1Range().getFrom());

        // For converting read mutations to reference coordinates
        this.invertedConsensusMutations = Mutations.invertMutations(consensusMutations);
    }

    public MutationArray computeMajorMutations() {
        // Does all the stuff
        return new MutationArray(reference, consensusMutations);
    }

    public Set<Integer> recomputeMinorMutations() {
        Set<Integer> minors = new HashSet<>();

        for (int code : this.minors) {
            // Get absolute position in consensus
            int pos = Mutations.getPosition(code);

            if (pos >= consensus.size()) {
                // can happen when an overlap between mate consensuses is read-through
                // this will cause an exception when calling rc
                continue;
            }

            // Todo: not fully tested for Indel minors
            if (rc) {
                code = rc(code, pos, consensus.size());
                pos = Mutations.getPosition(code);
            }

            // Compute position in consensus<->reference frame

            // Check if overlaps with ref<->cons frame, filter otherwise
            if (consensusAlignment.getSequence2Range().contains(pos)) {
                // Position in consensus in ref<->cons frame
                int positionInReferenceConsensusFrame = pos -
                        consensusAlignment.getSequence2Range().getFrom(),
                        // position in reference in ref<->cons frame
                        relativeReferencePosition = Mutations.convertPosition(invertedConsensusMutations,
                                positionInReferenceConsensusFrame);

                // Absolute position in reference
                int referencePosition = relativeReferencePosition +
                        consensusAlignment.getSequence1Range().getFrom();

                // Check that minor doesn't fall out of ref<->cons frame
                if (consensusAlignment.getSequence1Range().contains(referencePosition)) {
                    minors.add(Mutations.move(code, referencePosition - pos));
                }
            }
        }

        return minors;
    }

    static int rc(int code, int pos, int len) {
        pos = len - 1 - pos;
        if (isSubstitution(code)) {
            return createSubstitution(pos,
                    getComplement((byte) getFrom(code)),
                    getComplement((byte) getTo(code)));
        } else if (isInsertion(code)) {
            return createInsertion(pos,
                    getComplement((byte) getTo(code)));
        } else {
            return createInsertion(pos,
                    getComplement((byte) getFrom(code)));
        }
    }

    static int[] computeMajorMutations(SequenceQualityPhred queryQuality,
                                       int[] mutationCodes,
                                       LocalAlignment alignment,
                                       byte qualityThreshold) {
        final boolean[] filter = new boolean[mutationCodes.length];
        int nFiltered = 0;
        for (int i = 0; i < mutationCodes.length; i++) {
            int code = mutationCodes[i];
            if (isSubstitution(code)) {
                // This tells us precise position in sequence2,
                // that is, consensus for ref->cons alignment and
                // read for cons->read alignment
                int position = alignment.getSequence2Range().getFrom() + Mutations.convertPosition(mutationCodes,
                        Mutations.getPosition(code));
                byte qual = queryQuality.value(position);
                if (qual <= qualityThreshold) {
                    filter[i] = true;
                    nFiltered++;
                }
            }
        }

        final int[] filteredMutationCodes = new int[mutationCodes.length - nFiltered];
        for (int i = 0, j = 0; i < mutationCodes.length; i++) {
            if (!filter[i]) {
                filteredMutationCodes[j] = mutationCodes[i];
                j++;
            }
        }

        return filteredMutationCodes;
    }

    static int[] computeMutations(NucleotideSequence reference, LocalAlignment alignment) {
        int[] mutations = alignment.getMutations();
        NucleotideSequence subSequence = reference.getRange(alignment.getSequence1Range());
        Mutations.shiftIndelsAtHomopolymers(subSequence, mutations);
        mutations = Mutations.filterMutations(subSequence, mutations);
        return mutations;
    }
}
