/**
 * Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)
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
 */

package com.milaboratory.migec2.datasim.hotspot;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.mutations.GenericNucleotideMutationModel;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.mutations.NucleotideMutationModel;
import com.milaboratory.core.sequence.mutations.SubstitutionModels;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.core.sequencing.read.SSequencingReadImpl;
import com.milaboratory.migec2.core.align.reference.ReferenceLibrary;
import com.milaboratory.migec2.core.io.entity.SMig;
import com.milaboratory.migec2.datasim.MigGeneratorHistory;
import com.milaboratory.migec2.datasim.SMigGenerator;
import com.milaboratory.migec2.util.Util;
import org.apache.commons.math.MathException;
import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.random.Well19937c;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class HotSpotMigGenerator implements SMigGenerator {
    private final double baseHotSpotFrequency, pcrEfficiency, baseErrorFrequency,
            zipfExponent, variantMutationrRate;
    private final int numberOfVariants, numberOfHotspots, sequenceLength,
            minMigSize, meanMigSize, stdMigSize, umiLength, numberOfCycles;
    private final RandomDataImpl randomData;
    private final int[] hotSpotPositions, hotSpotCodes;
    private final VariantSet variantSet;
    private final double hotSpotFirstCycleFreq;
    private final MigGeneratorHistory migGeneratorHistory;

    private final AtomicLong regHotSpotCount = new AtomicLong(),
            fcHotSpotCount = new AtomicLong(),
            noiseCount = new AtomicLong();

    public HotSpotMigGenerator() throws MathException {
        this(100, 1.0, 10,
                100,
                5, 10, 5,
                12,
                30,
                1e-2, 0.85,
                1e-4, 0.01,
                51102L);
    }

    public HotSpotMigGenerator(int numberOfVariants, double zipfExponent, int numberOfHotspots,
                               int sequenceLength, int minMigSize, int meanMigSize, int stdMigSize, int umiLength,
                               int numberOfCycles,
                               double baseHotSpotFrequency, double pcrEfficiency,
                               double baseErrorFrequency, double variantMutationRate,
                               long seed) throws MathException {
        this.baseHotSpotFrequency = baseHotSpotFrequency;
        this.pcrEfficiency = pcrEfficiency;
        this.baseErrorFrequency = baseErrorFrequency;
        this.zipfExponent = zipfExponent;
        this.numberOfHotspots = numberOfHotspots;
        this.numberOfVariants = numberOfVariants;
        this.sequenceLength = sequenceLength;
        this.variantMutationrRate = variantMutationRate;
        this.minMigSize = minMigSize;
        this.meanMigSize = meanMigSize;
        this.stdMigSize = stdMigSize;
        this.umiLength = umiLength;
        this.numberOfCycles = numberOfCycles;
        this.hotSpotFirstCycleFreq = baseHotSpotFrequency * (1.0 - pcrEfficiency);
        this.randomData = new RandomDataImpl(new Well19937c(seed));

        // Generate hot-spot positions and 'to' codes

        List<Integer> positions = new ArrayList<>();

        for (int i = 0; i < sequenceLength; i++) {
            positions.add(i);
        }

        Collections.shuffle(positions);

        positions = positions.subList(0, numberOfHotspots);
        Collections.sort(positions);

        NucleotideSequence baseSequence = Util.randomSequence(sequenceLength);

        hotSpotPositions = new int[numberOfHotspots];
        hotSpotCodes = new int[numberOfHotspots];

        for (int i = 0; i < numberOfHotspots; i++) {
            hotSpotPositions[i] = positions.get(i);

            // Ensure real substitutions
            while (true) {
                hotSpotCodes[i] = randomData.nextInt(0, 3);
                if (hotSpotCodes[i] != baseSequence.codeAt(hotSpotPositions[i]))
                    break;
            }
        }

        variantSet = new VariantSet(baseSequence);

        this.migGeneratorHistory = new MigGeneratorHistory(variantSet.referenceLibrary);
    }

    @Override
    public SMig take() throws Exception {
        int migSize = (int) Math.max(minMigSize,
                randomData.nextGaussian(meanMigSize, stdMigSize));

        double factor = (double) numberOfCycles / (double) migSize;

        double correctedHotSpotFrequency = baseHotSpotFrequency * factor,
                correctedErrorFrequency = baseErrorFrequency * factor;

        NucleotideMutationModel randomMutationModel = new GenericNucleotideMutationModel(
                SubstitutionModels.getEmpiricalNucleotideSubstitutionModel(),
                0, correctedErrorFrequency, randomData.nextLong(Long.MIN_VALUE, Long.MAX_VALUE));

        NucleotideSequence umi = Util.randomSequence(umiLength);

        VariantData variantData = variantSet.sample();

        // Decide which hot-spots will happen at first cycle

        boolean[] hotSpotMask = new boolean[numberOfHotspots];
        int nubmerOfFirstCycleHotSpots = 0;

        for (int i = 0; i < numberOfHotspots; i++) {
            if (randomData.nextUniform(0, 1) < hotSpotFirstCycleFreq) {
                hotSpotMask[i] = true;
                nubmerOfFirstCycleHotSpots++;
            }
        }

        int[] firstCycleHotSpots = new int[nubmerOfFirstCycleHotSpots],
                regularHotSpots = new int[numberOfHotspots - nubmerOfFirstCycleHotSpots];

        for (int i = 0, j = 0, k = 0; i < numberOfHotspots; i++) {
            if (hotSpotMask[i]) {
                firstCycleHotSpots[j++] = variantData.hotSpotMutations[i];
            } else {
                regularHotSpots[k++] = variantData.hotSpotMutations[i];
            }
        }

        // Apply first cycle hot-spots

        if (firstCycleHotSpots.length > 0)
            fcHotSpotCount.incrementAndGet();

        NucleotideSequence sequence1 = Mutations.mutate(variantData.sequence, firstCycleHotSpots);

        // Generate reads

        List<SSequencingRead> reads = new ArrayList<>();

        for (int i = 0; i < migSize; i++) {
            // Apply remaining hot-spots

            boolean[] regularHotSpotMask = new boolean[regularHotSpots.length];
            int regularHotSpotsThisCycleCount = 0;

            for (int j = 0; j < regularHotSpots.length; j++) {
                if (randomData.nextUniform(0, 1) < correctedHotSpotFrequency) {
                    regularHotSpotMask[j] = true;
                    regularHotSpotsThisCycleCount++;
                }
            }

            int[] regularHotSpotsThisCycle = new int[regularHotSpotsThisCycleCount];
            for (int j = 0, k = 0; j < regularHotSpots.length; j++)
                if (regularHotSpotMask[j])
                    regularHotSpotsThisCycle[k++] = regularHotSpots[j];

            if (regularHotSpotsThisCycleCount > 0)
                regHotSpotCount.incrementAndGet();

            NucleotideSequence sequence2 = Mutations.mutate(sequence1, regularHotSpotsThisCycle);


            // Add in some random errors

            int[] randomMutations = Mutations.generateMutations(sequence2, randomMutationModel);

            if (randomMutations.length > 0)
                noiseCount.incrementAndGet();

            sequence2 = Mutations.mutate(sequence2, randomMutations);

            reads.add(new SSequencingReadImpl(new NucleotideSQPair(sequence2)));

            migGeneratorHistory.append(variantData.sequence, sequence2);
        }

        return new SMig(reads, umi);
    }

    @Override
    public ReferenceLibrary getReferenceLibrary() {
        return variantSet.getReferenceLibrary();
    }

    @Override
    public MigGeneratorHistory getMigGeneratorHistory() {
        return migGeneratorHistory;
    }

    private class VariantSet {
        private final List<VariantData> variants = new ArrayList<>();
        private final ReferenceLibrary referenceLibrary;

        public VariantSet(NucleotideSequence baseSequence) throws MathException {
            NucleotideMutationModel variantMutationModel = new GenericNucleotideMutationModel(
                    SubstitutionModels.getEmpiricalNucleotideSubstitutionModel(),
                    0, variantMutationrRate,
                    51102L);

            List<SSequencingRead> records = new LinkedList<>();
            Set<NucleotideSequence> previousSequences = new HashSet<>();

            for (int i = 0; i < numberOfVariants; i++) {
                NucleotideSequence sequence;

                // Generate unique variant
                while (true) {
                    int[] mutations = Mutations.generateMutations(baseSequence, variantMutationModel);
                    sequence = Mutations.mutate(baseSequence, mutations);

                    if (!previousSequences.contains(sequence)) {
                        previousSequences.add(sequence);
                        break;
                    }
                }

                records.add(new SSequencingReadImpl(
                        "hotspot_variant_" + i + "\tsynthetic",
                        new NucleotideSQPair(sequence),
                        (long) i));

                // Generate hot-spot mutations

                int[] hotSpotMutations = new int[numberOfHotspots];

                for (int j = 0; j < numberOfHotspots; j++) {
                    int pos = hotSpotPositions[j], from = sequence.codeAt(pos),
                            to = hotSpotCodes[j];

                    hotSpotMutations[j] = Mutations.createSubstitution(pos, from, to);
                }

                // Calculate frequency based on Zipf distribution

                int variantFrequency = randomData.nextZipf(numberOfVariants, zipfExponent);

                VariantData variant = new VariantData(sequence, hotSpotMutations);

                for (int j = 0; j < variantFrequency; j++) {
                    variants.add(variant);
                }
            }

            referenceLibrary = new ReferenceLibrary(records);
        }

        public VariantData sample() {
            return variants.get(randomData.nextInt(0, variants.size() - 1));
        }

        public ReferenceLibrary getReferenceLibrary() {
            return referenceLibrary;
        }
    }

    private class VariantData {
        public final NucleotideSequence sequence;
        public final int[] hotSpotMutations;

        public VariantData(NucleotideSequence sequence, int[] hotSpotMutations) {
            this.sequence = sequence;
            this.hotSpotMutations = hotSpotMutations;
        }

        @Override
        public String toString() {
            return sequence.toString() + "\n" + Mutations.toString(NucleotideAlphabet.INSTANCE, hotSpotMutations);
        }
    }

    public VariantSet getVariantSet() {
        return variantSet;
    }

    @Override
    public String toString() {
        return "First cycle hotspots = " + fcHotSpotCount.get() +
                "\nRegular hotspots = " + regHotSpotCount.get() +
                "\nOther mtuations = " + noiseCount.get();
    }
}
