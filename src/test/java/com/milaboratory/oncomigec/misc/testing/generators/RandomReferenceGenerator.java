package com.milaboratory.oncomigec.misc.testing.generators;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.core.sequencing.read.SSequencingReadImpl;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.milaboratory.oncomigec.misc.Util.randomSequence;

public class RandomReferenceGenerator {
    private int referenceSizeMin, referenceSizeMax;
    private final GeneratorMutationModel generatorMutationModel;
    private AtomicInteger referenceIndex = new AtomicInteger();


    public RandomReferenceGenerator() {
        this(GeneratorMutationModel.DEFAULT, 75, 300);
    }

    public RandomReferenceGenerator(GeneratorMutationModel generatorMutationModel) {
        this(generatorMutationModel, 75, 300);
    }

    public RandomReferenceGenerator(GeneratorMutationModel generatorMutationModel, int referenceSizeMin, int referenceSizeMax) {
        this.generatorMutationModel = generatorMutationModel;
        this.referenceSizeMin = referenceSizeMin;
        this.referenceSizeMax = referenceSizeMax;
    }

    public NucleotideSequence nextSequence() {
        return randomSequence(generatorMutationModel.nextFromRange(referenceSizeMin, referenceSizeMax));
    }

    public ParentChildPair nextParentChildPair() {
        NucleotideSequence parent = nextSequence();
        int[] mutations = generatorMutationModel.nextMutations(parent);
        NucleotideSequence child = Mutations.mutate(parent, mutations);
        return new ParentChildPair(mutations, null, parent, child);
    }

    public ParentChildPair nextParentChildPair(ReferenceLibrary referenceLibrary) {
        Reference parentReference = nextReference(referenceLibrary);
        NucleotideSequence parentSequence = parentReference.getSequence();
        int[] mutations = generatorMutationModel.nextMutations(parentSequence);
        Mutations.shiftIndelsAtHomopolymers(parentSequence, mutations);
        NucleotideSequence childSequence = Mutations.mutate(parentSequence, mutations);
        return new ParentChildPair(mutations, parentReference, parentSequence, childSequence);
    }

    public Reference nextReference() {
        int id = referenceIndex.incrementAndGet();
        return new Reference(null, id, Integer.toString(id), nextSequence());
    }

    public NucleotideSequence nextReferenceSequence() {
        return nextReference().getSequence();
    }

    public NucleotideSequence nextMutatedReferenceSequence() {
        return generatorMutationModel.nextMutatedSequence(nextReference());
    }

    public Reference nextReference(ReferenceLibrary library) {
        return library.getReferences().get(generatorMutationModel.nextIndex(library.getReferences().size()));
    }

    public NucleotideSequence nextReferenceSequence(ReferenceLibrary library) {
        return nextReference(library).getSequence();
    }

    public NucleotideSequence nextMutatedReferenceSequence(ReferenceLibrary library) {
        return generatorMutationModel.nextMutatedSequence(nextReference(library));
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
            referenceSequences.add(generatorMutationModel.nextMutatedSequence(core));


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

    public class ParentChildPair {
        private final int[] mutations;
        private final NucleotideSequence parentSequence, childSequence;
        private final Reference parentReference;

        public ParentChildPair(int[] mutations, Reference parentReference,
                               NucleotideSequence parentSequence, NucleotideSequence childSequence) {
            this.mutations = mutations;
            this.parentReference = parentReference;
            this.parentSequence = parentSequence;
            this.childSequence = childSequence;
        }

        public int[] getMutations() {
            return mutations;
        }

        public Reference getParentReference() {
            return parentReference;
        }

        public NucleotideSequence getParentSequence() {
            return parentSequence;
        }

        public NucleotideSequence getChildSequence() {
            return childSequence;
        }
    }
}
