package com.milaboratory.oncomigec.util.testing.generators;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.quality.SequenceQualityPhred;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.io.entity.SMig;
import com.milaboratory.oncomigec.util.Util;

import java.util.*;

import static com.milaboratory.oncomigec.util.Util.randomSequence;

public class RandomMigGenerator {
    private boolean markMinorMutations = false;
    private int migSizeMin = 100, migSizeMax = 300;
    private double indelHeavyThreshold = 0.7, pcrErrorMultiplier = 0.01;
    private int umiSize = 12;
    private GeneratorMutationModel generatorMutationModel, pcrGeneratorMutationModel;

    public RandomMigGenerator() {
        setGeneratorMutationModel(GeneratorMutationModel.DEFAULT);
    }

    public RandomMigGeneratorResult nextMig(RandomReferenceGenerator referenceGenerator) {
        return nextMig(referenceGenerator.nextSequence());
    }

    public RandomMigGeneratorResult nextMigPCR(Reference reference) {
        return nextMigPCR(reference.getSequence());
    }

    public RandomMigGeneratorResult nextMigPCR(NucleotideSequence reference) {
        int[] pcrMutations = pcrGeneratorMutationModel.nextMutations(reference);
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
            Arrays.fill(qual, Util.PH33_MAX_QUAL);

            if (markMinorMutations) {
                for (int k = 0; k < mutations.length; k++) {
                    int code = mutations[k];
                    if (Mutations.isSubstitution(code))
                        qual[Mutations.convertPosition(mutations, Mutations.getPosition(code))] = Util.PH33_BAD_QUAL;
                }
            }

            reads.add(new NucleotideSQPair(seq, new SequenceQualityPhred(qual)));
        }

        boolean indelHeavy = (readsWithIndels / (double) migSize) > indelHeavyThreshold;
        SMig sMig = new SMig(reads, randomSequence(umiSize));

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

    public boolean getMarkMinorMutations() {
        return markMinorMutations;
    }

    public void setMarkMinorMutations(boolean markMinorMutations) {
        this.markMinorMutations = markMinorMutations;
    }

    public double getIndelHeavyThreshold() {
        return indelHeavyThreshold;
    }

    public void setIndelHeavyThreshold(double indelHeavyThreshold) {
        this.indelHeavyThreshold = indelHeavyThreshold;
    }

    public double getPcrErrorMultiplier() {
        return pcrErrorMultiplier;
    }

    public void setPcrErrorMultiplier(double pcrErrorMultiplier) {
        this.pcrErrorMultiplier = pcrErrorMultiplier;
        this.pcrGeneratorMutationModel = generatorMutationModel.multiply(pcrErrorMultiplier);
    }

    public GeneratorMutationModel getGeneratorMutationModel() {
        return generatorMutationModel;
    }

    public void setGeneratorMutationModel(GeneratorMutationModel generatorMutationModel) {
        this.generatorMutationModel = generatorMutationModel;
        this.pcrGeneratorMutationModel = generatorMutationModel.multiply(pcrErrorMultiplier);
    }

    public GeneratorMutationModel getPcrGeneratorMutationModel() {
        return pcrGeneratorMutationModel;
    }

    public void setPcrGeneratorMutationModel(GeneratorMutationModel pcrGeneratorMutationModel) {
        this.pcrGeneratorMutationModel = pcrGeneratorMutationModel;
    }

    public int getUmiSize() {
        return umiSize;
    }

    public void setUmiSize(int umiSize) {
        this.umiSize = umiSize;
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
