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

package com.milaboratory.mageri.generators;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.core.sequencing.read.SSequencingReadImpl;
import com.milaboratory.mageri.core.genomic.Reference;
import com.milaboratory.mageri.core.genomic.ReferenceLibrary;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.milaboratory.mageri.generators.RandomUtil.randomSequence;

public class RandomReferenceGenerator implements RandomSequenceGenerator {
    protected int referenceSizeMin = 75, referenceSizeMax = 300;
    protected MutationGenerator mutationGenerator = MutationGenerator.DEFAULT;
    protected final AtomicInteger referenceIndex = new AtomicInteger();

    public NucleotideSequence nextSequence() {
        return randomSequence(RandomUtil.nextFromRange(referenceSizeMin, referenceSizeMax));
    }

    public ReferenceParentChildPair nextParentChildPair() {
        NucleotideSequence parent = nextSequence();
        int[] mutations = mutationGenerator.nextMutations(parent);
        NucleotideSequence child = Mutations.mutate(parent, mutations);
        return new ReferenceParentChildPair(mutations, null, parent, child);
    }

    public ReferenceParentChildPair nextParentChildPair(ReferenceLibrary referenceLibrary) {
        Reference parentReference = nextReference(referenceLibrary);
        NucleotideSequence parentSequence = parentReference.getSequence();
        int[] mutations = mutationGenerator.nextMutations(parentSequence);
        Mutations.shiftIndelsAtHomopolymers(parentSequence, mutations);
        NucleotideSequence childSequence = Mutations.mutate(parentSequence, mutations);
        return new ReferenceParentChildPair(mutations, parentReference, parentSequence, childSequence);
    }

    public Reference nextReference() {
        int id = referenceIndex.incrementAndGet();
        return new Reference(null, id, Integer.toString(id), nextSequence(), null);
    }

    public NucleotideSequence nextReferenceSequence() {
        return nextReference().getSequence();
    }

    public NucleotideSequence nextMutatedReferenceSequence() {
        return mutationGenerator.nextMutatedSequence(nextReference());
    }

    public Reference nextReference(ReferenceLibrary library) {
        return library.getReferences().get(RandomUtil.nextIndex(library.getReferences().size()));
    }

    public NucleotideSequence nextReferenceSequence(ReferenceLibrary library) {
        return nextReference(library).getSequence();
    }

    public NucleotideSequence nextMutatedReferenceSequence(ReferenceLibrary library) {
        return mutationGenerator.nextMutatedSequence(nextReference(library));
    }

    public ReferenceLibrary nextReferenceLibrary(int size) {
        List<SSequencingRead> references = new LinkedList<>();
        for (int i = 0; i < size; i++)
            references.add(new SSequencingReadImpl(i + " random_reference",
                    new NucleotideSQPair(nextSequence()),
                    i));
        return new ReferenceLibrary(references);
    }

    public ReferenceLibrary nextHomologousReferenceLibrary(int size) {
        NucleotideSequence core = nextSequence();
        Set<NucleotideSequence> referenceSequences = new HashSet<>();
        for (int i = 0; i < size; i++)
            referenceSequences.add(mutationGenerator.nextMutatedSequence(core));

        List<SSequencingRead> references = new LinkedList<>();
        int i = 0;
        for (NucleotideSequence referenceSequence : referenceSequences)
            references.add(new SSequencingReadImpl(i + " random_reference",
                    new NucleotideSQPair(referenceSequence),
                    i++));

        return new ReferenceLibrary(references);
    }

    public int getReferenceSizeMin() {
        return referenceSizeMin;
    }

    public int getReferenceSizeMax() {
        return referenceSizeMax;
    }

    public void setReferenceSizeMin(int referenceSizeMin) {
        this.referenceSizeMin = referenceSizeMin;
    }

    public void setReferenceSizeMax(int referenceSizeMax) {
        this.referenceSizeMax = referenceSizeMax;
    }

    public MutationGenerator getMutationGenerator() {
        return mutationGenerator;
    }

    public void setMutationGenerator(MutationGenerator mutationGenerator) {
        this.mutationGenerator = mutationGenerator;
    }
}
