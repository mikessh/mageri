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

package com.antigenomics.mageri.generators;

import com.antigenomics.mageri.core.genomic.Reference;
import com.antigenomics.mageri.core.genomic.ReferenceLibrary;
import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.core.sequencing.read.SSequencingReadImpl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.antigenomics.mageri.generators.RandomUtil.randomSequence;

public class RandomReferenceGenerator implements RandomSequenceGenerator {
    protected int referenceSizeMin = 75, referenceSizeMax = 300;
    protected MutationGenerator mutationGenerator = MutationGenerator.DEFAULT;
    protected final AtomicInteger referenceIndex = new AtomicInteger();

    public NucleotideSequence nextSequence() {
        return RandomUtil.randomSequence(RandomUtil.nextFromRange(referenceSizeMin, referenceSizeMax));
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
