package com.milaboratory.oncomigec.generators;

import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.input.SMig;
import com.milaboratory.oncomigec.core.input.index.Read;

import java.util.*;

import static com.milaboratory.oncomigec.misc.Util.randomSequence;

public class RandomMigGenerator {
    private static final Random rnd = new Random(123456);
    private boolean maskMinorSubstitutions = false;
    private int migSizeMin = 100, migSizeMax = 300;
    private double pcrErrorMultiplier = 0.1;
    private int umiSize = 12;
    private int maxRandomFlankSize = 0;
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

        List<Read> reads = new ArrayList<>();
        int migSize = generatorMutationModel.nextFromRange(migSizeMin, migSizeMax);

        Map<Integer, Integer> minorMutationCounts = new HashMap<>();
        for (int j = 0; j < migSize; j++) {

            int[] mutations = generatorMutationModel.nextMutations(sequence);
            Mutations.shiftIndelsAtHomopolymers(sequence, mutations);

            for (int code : mutations) {
                Integer count = minorMutationCounts.get(code);
                minorMutationCounts.put(code, count == null ? 1 : (count + 1));
            }

            NucleotideSequence sequence2 = Mutations.mutate(sequence,
                    mutations);

            int offset5 = rnd.nextInt(maxRandomFlankSize + 1),
                    offset3 = rnd.nextInt(maxRandomFlankSize + 1);

            NucleotideSequence flank5 = randomSequence(offset5),
                    flank3 = randomSequence(offset3),
                    sequence3 = flank5.concatenate(sequence2.concatenate(flank3));

            BitSet qualMask = new BitSet(sequence3.size());

            if (maskMinorSubstitutions) {
                for (int code : mutations) {
                    if (Mutations.isSubstitution(code)) {
                        qualMask.set(offset5 + Mutations.convertPosition(mutations, Mutations.getPosition(code)));
                    }
                }
            }

            reads.add(new Read(sequence3, qualMask));
        }

        SMig sMig = new SMig(null, randomSequence(umiSize), reads);

        return new RandomMigGeneratorResult(sequence, sMig, minorMutationCounts, pcrMutations);
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

    public boolean getMaskMinorSubstitutions() {
        return maskMinorSubstitutions;
    }

    public void setMaskMinorSubstitutions(boolean maskMinorSubstitutions) {
        this.maskMinorSubstitutions = maskMinorSubstitutions;
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

    public int getMaxRandomFlankSize() {
        return maxRandomFlankSize;
    }

    public void setMaxRandomFlankSize(int maxRandomFlankSize) {
        this.maxRandomFlankSize = maxRandomFlankSize;
    }

    public class RandomMigGeneratorResult {
        private final SMig mig;
        private final int[] pcrMutations;
        private final NucleotideSequence consensus;
        private final Map<Integer, Integer> minorMutationCounts;

        public RandomMigGeneratorResult(NucleotideSequence consensus,
                                        SMig mig, Map<Integer, Integer> minorMutationCounts,
                                        int[] pcrMutations) {
            this.consensus = consensus;
            this.mig = mig;
            this.pcrMutations = pcrMutations;
            this.minorMutationCounts = minorMutationCounts;
        }

        public NucleotideSequence getConsensus() {
            return consensus;
        }

        public SMig getMig() {
            return mig;
        }

        public Map<Integer, Integer> getMinorMutationCounts() {
            return minorMutationCounts;
        }

        public int[] getPcrMutations() {
            return pcrMutations;
        }
    }
}
