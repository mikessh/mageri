package com.milaboratory.oncomigec.util.testing.generators;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.quality.SequenceQualityPhred;
import com.milaboratory.oncomigec.core.align.reference.Reference;
import com.milaboratory.oncomigec.core.io.entity.SMig;

import java.util.*;

import static com.milaboratory.oncomigec.util.Util.randomSequence;

public class RandomMigGenerator {
    private int migSizeMin, migSizeMax;
    private static final double INDEL_HEAVY_THERSHOLD = 0.7, PCR_FACTOR = 0.01;
    private static final int UMI_SIZE = 12;
    private final GeneratorMutationModel generatorMutationModel, pcrGeneratorMutationModel;

    public RandomMigGenerator() {
        this(GeneratorMutationModel.DEFAULT, 30, 300);
    }

    public RandomMigGenerator(GeneratorMutationModel generatorMutationModel) {
        this(generatorMutationModel, 30, 300);
    }

    public RandomMigGenerator(GeneratorMutationModel generatorMutationModel, int migSizeMin, int migSizeMax) {
        this.generatorMutationModel = generatorMutationModel;
        this.pcrGeneratorMutationModel = new GeneratorMutationModel(generatorMutationModel, PCR_FACTOR);
        this.migSizeMin = migSizeMin;
        this.migSizeMax = migSizeMax;
    }

    public RandomMigGeneratorResult nextMig(RandomReferenceGenerator referenceGenerator) {
        return nextMig(referenceGenerator.nextSequence());
    }

    public RandomMigGeneratorResult nextMigPCR(Reference reference) {
        return nextMigPCR(reference.getSequence());
    }

    public RandomMigGeneratorResult nextMigPCR(NucleotideSequence reference) {
        int[] pcrMutations = generatorMutationModel.nextMutations(reference);
        return nextMig(reference, pcrMutations);
    }

    public RandomMigGeneratorResult nextMig(Reference reference) {
        return nextMig(reference.getSequence());
    }

    public RandomMigGeneratorResult nextMig(NucleotideSequence sequence) {
        return nextMig(sequence, new int[0]);
    }

    private RandomMigGeneratorResult nextMig(NucleotideSequence sequence, int[] pcrMutations) {
        sequence = Mutations.mutate(sequence, pcrMutations);

        List<NucleotideSQPair> reads = new ArrayList<>();
        int migSize = generatorMutationModel.nextFromRange(migSizeMin, migSizeMax);
        int readsWithIndels = 0;
        Map<Integer, Integer> minorMutationCounts = new HashMap<>();
        for (int j = 0; j < migSize; j++) {
            int[] mutations = generatorMutationModel.nextMutations(sequence);
            Mutations.shiftIndelsAtHomopolymers(sequence, mutations);

            for (int k = 0; k < mutations.length; k++) {
                int code = mutations[k];
                Integer count = minorMutationCounts.get(code);
                minorMutationCounts.put(code, count == null ? 1 : (count + 1));

                if (!Mutations.isSubstitution(mutations[k])) {
                    readsWithIndels++;
                    break;
                }
            }

            NucleotideSequence seq = Mutations.mutate(sequence,
                    mutations);

            byte[] qual = new byte[seq.size()];
            Arrays.fill(qual, (byte) 40);
            for (int k = 0; k < mutations.length; k++) {
                int code = mutations[k];
                if (Mutations.isSubstitution(code))
                    qual[Mutations.convertPosition(mutations, Mutations.getPosition(code))] = (byte) 0;
            }

            reads.add(new NucleotideSQPair(seq, new SequenceQualityPhred(qual)));
        }

        boolean indelHeavy = (readsWithIndels / (double) migSize) > INDEL_HEAVY_THERSHOLD;
        SMig sMig = new SMig(reads, randomSequence(UMI_SIZE));

        return new RandomMigGeneratorResult(sMig, indelHeavy, minorMutationCounts, pcrMutations);
    }

    public int getMigSizeMin() {
        return migSizeMin;
    }

    public int getMigSizeMax() {
        return migSizeMax;
    }

    public void setMigSizeMin(int migSizeMin) {
        this.migSizeMin = migSizeMin;
    }

    public void setMigSizeMax(int migSizeMax) {
        this.migSizeMax = migSizeMax;
    }

    public class RandomMigGeneratorResult {
        private final SMig mig;
        private final boolean indelHeavy;
        private final int[] pcrMutations;
        private final Map<Integer, Integer> minorMutationCounts;

        public RandomMigGeneratorResult(SMig mig, boolean indelHeavy, Map<Integer, Integer> minorMutationCounts,
                                        int[] pcrMutations) {
            this.mig = mig;
            this.pcrMutations = pcrMutations;
            this.indelHeavy = indelHeavy;
            this.minorMutationCounts = minorMutationCounts;
        }

        public SMig getMig() {
            return mig;
        }

        public boolean indelHeavy() {
            return indelHeavy;
        }

        public Map<Integer, Integer> getMinorMutationCounts() {
            return minorMutationCounts;
        }

        public int[] getPcrMutations() {
            return pcrMutations;
        }
    }
}
