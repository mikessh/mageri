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
    private int migSizeMin = 5, migSizeMax = 100;
    private ErrorModel errorModel = new ErrorModel();
    private MutationGenerator read = MutationGenerator.NO_INDEL,
            pcr = MutationGenerator.NO_INDEL_SKEWED,
            pcrHotSpot = pcr.multiply(errorModel.getPropagateProb()),
            somatic = MutationGenerator.SOMATIC;

    private final NucleotideSequence reference;
    private final Set<Integer> hotSpotPositions = new HashSet<>();
    private final Map<Integer, Integer> somaticMutationCounters = new HashMap<>(),
            hotSpotMutationCounters = new HashMap<>();


    public ModelMigGenerator(NucleotideSequence reference) {
        this.reference = reference;
    }

    public void generateHotSpots() {
        int[] mutations = pcr.multiply(1000).nextMutations(reference);

        for (int mutation : mutations) {
            hotSpotPositions.add(Mutations.getPosition(mutation));
        }
    }

    public SMig nextMig() {
        int[] somaticMutations = somatic.nextMutations(reference);
        Integer counter;
        for (int mutation : somaticMutations) {
            somaticMutationCounters.put(mutation,
                    (((counter = somaticMutationCounters.get(mutation)) == null) ? 0 : counter) + 1
            );
        }

        NucleotideSequence sequence1 = Mutations.mutate(reference, somaticMutations);

        //

        int[] hotSpotMutations = pcrHotSpot.nextMutations(reference),
                hotSpotMutationsFiltered = new int[hotSpotMutations.length];
        int k = 0;
        for (int mutation : hotSpotMutations) {
            if (hotSpotPositions.contains(Mutations.getPosition(mutation))) {
                hotSpotMutationCounters.put(mutation,
                        (((counter = hotSpotMutationCounters.get(mutation)) == null) ? 0 : counter) + 1
                );
                hotSpotMutationsFiltered[k++] = mutation;
            }
        }
        hotSpotMutations = Arrays.copyOf(hotSpotMutationsFiltered, k);

        NucleotideSequence sequence2 = Mutations.mutate(sequence1, hotSpotMutations);

        //

        int nReads = RandomUtil.nextFromRange(migSizeMin, migSizeMax);

        List<Read> reads = new ArrayList<>();

        for (int i = 0; i < nReads; i++) {
            int[] pcrMutations = pcr.nextMutations(sequence2);
            NucleotideSequence sequence3 = Mutations.mutate(sequence2, pcrMutations);

            int[] readMutations = read.nextMutations(sequence3);
            NucleotideSequence sequence4 = Mutations.mutate(sequence3, readMutations);
            byte[] quality = new byte[sequence4.size()];
            Arrays.fill(quality, (byte) 40);

            for (int mutation : readMutations) {
                int pos = Mutations.getPosition(mutation),
                        from = Mutations.getFrom(mutation),
                        to = Mutations.getTo(mutation);

                byte qual = (byte) Math.max(2, Math.min(40, -10 * Math.log10(read.getSubstitutionModel().getValue(from, to))));
                quality[pos] = qual;
            }

            reads.add(new Read(new NucleotideSQPair(sequence4, new SequenceQualityPhred(quality))));
        }

        return new SMig(Sample.create("dummy", false), randomSequence(12), reads);
    }
}
