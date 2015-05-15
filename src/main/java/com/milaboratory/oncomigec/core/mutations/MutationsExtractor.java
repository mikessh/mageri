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

package com.milaboratory.oncomigec.core.mutations;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.quality.SequenceQualityPhred;
import com.milaboratory.oncomigec.core.genomic.Reference;

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
