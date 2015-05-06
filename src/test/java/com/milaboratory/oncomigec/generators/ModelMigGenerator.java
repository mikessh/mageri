/*
 * Copyright 2013-2015 Mikhail Shugay (mikhail.shugay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last modified on 1.5.2015 by mikesh
 */

package com.milaboratory.oncomigec.generators;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.quality.SequenceQualityPhred;
import com.milaboratory.oncomigec.core.input.SMig;
import com.milaboratory.oncomigec.core.input.index.Read;
import com.milaboratory.oncomigec.core.variant.ErrorModel;
import com.milaboratory.oncomigec.pipeline.analysis.Sample;

import java.util.*;

import static com.milaboratory.oncomigec.generators.RandomUtil.randomSequence;

public class ModelMigGenerator {
    private static final Random rnd = new Random(480011L);
    private double hotSpotPositionRatio = 0.1, pcrPositionRatio = 0.4,
            somaticMutationRatio = 0.1, somaticMutationFreq = 0.0005;
    private ErrorModel errorModel = new ErrorModel();
    private MutationGenerator readErrorGenerator = MutationGenerator.NO_INDEL,
            pcrErrorGenerator = MutationGenerator.NO_INDEL_SKEWED,
            pcrHotSpotErrorGenerator = MutationGenerator.NO_INDEL_SKEWED.multiply(errorModel.getPropagateProb());

    private int[] somaticMutations;

    private final NucleotideSequence reference;
    private final Set<Integer> hotSpotPositions = new HashSet<>(),
            pcrPositions = new HashSet<>();
    private final Map<Integer, Integer> somaticMutationCounters = new HashMap<>(),
            hotSpotMutationCounters = new HashMap<>(),
            totalMutationCounters = new HashMap<>();

    public ModelMigGenerator(NucleotideSequence reference) {
        this.reference = reference;
        generateHotSpots();

        int[] somaticMutations = new int[reference.size()];
        int j = 0;

        for (int i = 0; i < reference.size(); i++) {
            double p = rnd.nextDouble();
            if (p < somaticMutationRatio) {
                int from = reference.codeAt(i);
                int to;
                do {
                    to = rnd.nextInt(3);
                } while (to == from);
                somaticMutations[j++] = Mutations.createSubstitution(i, from, to);
            }
        }

        this.somaticMutations = Arrays.copyOf(somaticMutations, j);
    }

    private static int[] generateAndFilterMutations(MutationGenerator mutationGenerator,
                                                    NucleotideSequence sequence,
                                                    Set<Integer> positions) {
        int l = 0;
        int[] mutations = mutationGenerator.nextMutations(sequence);
        for (int j = 0; j < mutations.length; j++) {
            int code = mutations[j];
            if (positions.contains(Mutations.getPosition(code))) {
                mutations[l++] = code;
            }
        }
        return Arrays.copyOf(mutations, l);
    }

    private static int[] selectMutations(int[] mutations, double p) {
        int l = 0;
        int[] _mutations = new int[mutations.length];
        for (int code : mutations) {
            if (rnd.nextDouble() < p) {
                _mutations[l++] = code;
            }
        }
        return Arrays.copyOf(_mutations, l);
    }

    private void generateHotSpots() {
        for (int i = 0; i < reference.size(); i++) {
            double p = rnd.nextDouble();
            if (p < hotSpotPositionRatio) {
                hotSpotPositions.add(i);
            }
            if (p < pcrPositionRatio) {
                pcrPositions.add(i);
            }
        }
    }

    public SMig nextMig() {
        int[] somaticMutations = selectMutations(this.somaticMutations, somaticMutationFreq);

        Integer counter;
        for (int code : somaticMutations) {
            somaticMutationCounters.put(code,
                    (((counter = somaticMutationCounters.get(code)) == null) ? 0 : counter) + 1
            );
            totalMutationCounters.put(code,
                    (((counter = totalMutationCounters.get(code)) == null) ? 0 : counter) + 1
            );
        }

        NucleotideSequence sequence1 = Mutations.mutate(reference, somaticMutations);

        //

        int[] hotSpotMutations = generateAndFilterMutations(pcrHotSpotErrorGenerator,
                reference, hotSpotPositions);

        for (int code : hotSpotMutations) {
            hotSpotMutationCounters.put(code,
                    (((counter = hotSpotMutationCounters.get(code)) == null) ? 0 : counter) + 1
            );
            totalMutationCounters.put(code,
                    (((counter = totalMutationCounters.get(code)) == null) ? 0 : counter) + 1
            );
        }

        NucleotideSequence sequence2 = Mutations.mutate(sequence1, hotSpotMutations);

        //

        List<Read> reads = new ArrayList<>();

        for (int i = 0; i < Math.pow(errorModel.getCycles(), 0.5 + 1.5 * rnd.nextDouble()); i++) {
            int[] pcrMutations = generateAndFilterMutations(pcrErrorGenerator,
                    sequence2, pcrPositions);
            NucleotideSequence sequence3 = Mutations.mutate(sequence2, pcrMutations);

            int[] readMutations = readErrorGenerator.nextMutations(sequence3);
            NucleotideSequence sequence4 = Mutations.mutate(sequence3, readMutations);
            byte[] quality = new byte[sequence4.size()];
            Arrays.fill(quality, (byte) 40);

            for (int mutation : readMutations) {
                int pos = Mutations.getPosition(mutation),
                        from = Mutations.getFrom(mutation),
                        to = Mutations.getTo(mutation);

                byte qual = (byte) Math.max(2, Math.min(40,
                        -10 * Math.log10(readErrorGenerator.getSubstitutionModel().getValue(from, to)
                        )));
                quality[pos] = qual;
            }

            reads.add(new Read(new NucleotideSQPair(sequence4, new SequenceQualityPhred(quality))));
        }

        return new SMig(Sample.create("dummy", false), randomSequence(12), reads);
    }

    public int getSomaticCount(int mutationCode) {
        Integer count = somaticMutationCounters.get(mutationCode);
        return count != null ? count : 0;
    }

    public int getHotSpotCount(int mutationCode) {
        Integer count = hotSpotMutationCounters.get(mutationCode);
        return count != null ? count : 0;
    }

    public int getVariantCount(int mutationCode) {
        Integer count = totalMutationCounters.get(mutationCode);
        return count != null ? count : 0;
    }

    public int somaticSize() {
        return somaticMutationCounters.size();
    }

    public int hotSpotSize() {
        return hotSpotMutationCounters.size();
    }

    public int totalSize() {
        return totalMutationCounters.size();
    }

    public int totalCount() {
        int totalCount = 0;
        for (int count : totalMutationCounters.values()) {
            totalCount += count;
        }
        return totalCount;
    }
}
