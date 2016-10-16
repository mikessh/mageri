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
import com.antigenomics.mageri.core.input.SMig;
import com.antigenomics.mageri.core.input.index.MaskedRead;
import com.antigenomics.mageri.core.input.index.Read;
import com.antigenomics.mageri.core.variant.VariantCallerParameters;
import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.util.Bit2Array;

import java.util.*;

public class ModelMigGenerator2 {
    private static final Random rnd = new Random(480011L);

    private final MutationGenerator seqMutationGenerator, somaticMutationGenerator;
    private final double[][] pcrErrorRates;
    private final VariantCallerParameters variantCallerParameters;
    private final Reference reference;
    private final int migSize;
    private final Map<Integer, Integer> somaticCounts = new HashMap<>();
    private int total = 0;

    public ModelMigGenerator2(VariantCallerParameters variantCallerParameters,
                              Reference reference, int migSize) {
        this(variantCallerParameters,reference,migSize,
                MutationGenerator.a)
    }

    public ModelMigGenerator2(VariantCallerParameters variantCallerParameters,
                              Reference reference, int migSize,
                              MutationGenerator somaticMutationGenerator,
                              MutationGenerator seqMutationGenerator,
                              double[][] pcrErrorRates) {
        this.seqMutationGenerator = seqMutationGenerator;
        this.pcrErrorRates = pcrErrorRates;
        this.variantCallerParameters = variantCallerParameters;
        this.somaticMutationGenerator = somaticMutationGenerator;
        this.reference = reference;
        this.migSize = migSize;
    }

    public SMig nextMig() {
        total++;

        int[] somaticMutations = somaticMutationGenerator.nextMutations(reference);

        for (int code : somaticMutations) {
            Integer val = somaticCounts.get(code);

            somaticCounts.put(code, val == null ? 1 : (val + 1));
        }

        NucleotideSequence sequence = Mutations.mutate(reference.getSequence(), somaticMutations);

        double[][] pcrErrorRatesByPosAndBase = new double[sequence.size()][4];

        int cycles = (int) variantCallerParameters.getModelCycles();
        double lambda = variantCallerParameters.getModelEfficiency() - 1.0;

        for (int i = 0; i < sequence.size(); i++) {
            int originalBase = sequence.codeAt(i);
            for (int j = 0; j < 4; j++) {
                if (j != originalBase) {
                    double errorFrequency = 0;
                    double pcrErrorRate = pcrErrorRates[originalBase][j];

                    for (int k = 0; k < cycles; k++) {
                        errorFrequency += pcrErrorRate;

                        int nTemplates = (int) Math.pow(1.0 + lambda, j);

                        if (rnd.nextDouble() < pcrErrorRate * nTemplates) {
                            errorFrequency += 1.0 / nTemplates;
                        }
                    }
                    pcrErrorRatesByPosAndBase[i][j] = errorFrequency;
                }
            }
        }

        List<Read> reads = new ArrayList<>();

        for (int i = 0; i < migSize; i++) {
            Bit2Array seqBits = new Bit2Array(sequence.size());

            for (int j = 0; j < sequence.size(); j++) {
                int origBase = sequence.codeAt(j), base = origBase;

                for (int k = 0; k < 4; k++) {
                    if (k != origBase) {
                        if (rnd.nextDouble() < pcrErrorRatesByPosAndBase[j][k]) {
                            base = k;
                        }
                    }
                }

                seqBits.set(j, base);
            }

            NucleotideSequence newSeq = new NucleotideSequence(seqBits);

            newSeq = seqMutationGenerator.nextMutatedSequence(newSeq);

            reads.add(new MaskedRead(new NucleotideSQPair(newSeq)));
        }

        return new SMig(null, null, reads);
    }

    public float getSomaticFreq(int code) {
        Integer count = somaticCounts.get(code);

        return count == null ? 0.0f : (count / (float) total);
    }
}
