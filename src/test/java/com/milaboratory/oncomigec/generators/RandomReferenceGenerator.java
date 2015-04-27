package com.milaboratory.oncomigec.generators;

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
    protected int referenceSizeMin = 75, referenceSizeMax = 300;
    protected GeneratorMutationModel generatorMutationModel = GeneratorMutationModel.DEFAULT;
    protected final AtomicInteger referenceIndex = new AtomicInteger();

    public NucleotideSequence nextSequence() {
        return randomSequence(generatorMutationModel.nextFromRange(referenceSizeMin, referenceSizeMax));
    }

    public ReferenceParentChildPair nextParentChildPair() {
        NucleotideSequence parent = nextSequence();
        int[] mutations = generatorMutationModel.nextMutations(parent);
        NucleotideSequence child = Mutations.mutate(parent, mutations);
        return new ReferenceParentChildPair(mutations, null, parent, child);
    }

    public ReferenceParentChildPair nextParentChildPair(ReferenceLibrary referenceLibrary) {
        Reference parentReference = nextReference(referenceLibrary);
        NucleotideSequence parentSequence = parentReference.getSequence();
        int[] mutations = generatorMutationModel.nextMutations(parentSequence);
        Mutations.shiftIndelsAtHomopolymers(parentSequence, mutations);
        NucleotideSequence childSequence = Mutations.mutate(parentSequence, mutations);
        return new ReferenceParentChildPair(mutations, parentReference, parentSequence, childSequence);
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

    public GeneratorMutationModel getGeneratorMutationModel() {
        return generatorMutationModel;
    }

    public void setGeneratorMutationModel(GeneratorMutationModel generatorMutationModel) {
        this.generatorMutationModel = generatorMutationModel;
    }
}
