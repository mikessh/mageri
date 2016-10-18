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

    private final MutationGenerator seqMutationGenerator, somaticMutationGenerator,
            pcrMutationGenerator;
    private final double[][] pcrErrorRates;
    private final VariantCallerParameters variantCallerParameters;
    private final Reference reference;
    private final int migSize;
    private final Map<Integer, Integer> somaticCounts = new HashMap<>();
    private int total = 0;

    public ModelMigGenerator2(VariantCallerParameters variantCallerParameters,
                              Reference reference, int migSize) {
        this(variantCallerParameters, reference, migSize,
                MutationGenerator.getUniform(1e-2),
                MutationGenerator.SEQ_Q30,
                MutationGenerator.NO_INDEL_SKEWED);
    }

    public ModelMigGenerator2(VariantCallerParameters variantCallerParameters,
                              Reference reference, int migSize,
                              MutationGenerator somaticMutationGenerator,
                              MutationGenerator seqMutationGenerator,
                              MutationGenerator pcrMutationGenerator) {
        this.seqMutationGenerator = seqMutationGenerator;
        this.pcrMutationGenerator = pcrMutationGenerator;
        this.pcrErrorRates = getPcrErrorRates(pcrMutationGenerator);
        this.variantCallerParameters = variantCallerParameters;
        this.somaticMutationGenerator = somaticMutationGenerator;
        this.reference = reference;
        this.migSize = migSize;
    }

    private double[][] getPcrErrorRates(MutationGenerator pcrMutationGenerator) {
        double[][] errorRates = new double[4][4];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                errorRates[i][j] = pcrMutationGenerator.getSubstitutionModel().getValue(i, j);
            }
        }

        return errorRates;
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
            double errFreqSum = 0;
            for (int j = 0; j < 4; j++) {
                if (j != originalBase) {
                    double errorFrequency = 0;
                    double pcrErrorRate = pcrErrorRates[originalBase][j];

                    for (int k = 1; k <= cycles; k++) {
                        errorFrequency += pcrErrorRate;

                        int nTemplates = (int) Math.pow(1.0 + lambda, k);

                        if (rnd.nextDouble() < pcrErrorRate * nTemplates) {
                            errorFrequency += 1.0 / nTemplates;
                        }
                    }
                    errorFrequency = errorFrequency > 1.0 ? 1.0 : errorFrequency;
                    pcrErrorRatesByPosAndBase[i][j] = errorFrequency;
                    errFreqSum += errorFrequency;
                }
            }
            pcrErrorRatesByPosAndBase[i][originalBase] = 1.0 - errFreqSum;
        }

        List<Read> reads = new ArrayList<>();

        for (int i = 0; i < migSize; i++) {
            Bit2Array seqBits = new Bit2Array(sequence.size());

            for (int j = 0; j < sequence.size(); j++) {
                seqBits.set(j, getBase(pcrErrorRatesByPosAndBase[j]));
            }

            NucleotideSequence newSeq = new NucleotideSequence(seqBits);

            newSeq = seqMutationGenerator.nextMutatedSequence(newSeq);

            reads.add(new MaskedRead(new NucleotideSQPair(newSeq)));
        }

        return new SMig(null, null, reads);
    }

    private int getBase(double[] pcrErrorRates) {
        double rand = rnd.nextDouble();

        double errFreqSum = pcrErrorRates[0];

        for (int i = 1; i < 4; i++) {
            if (rand <= errFreqSum) {
                return i - 1;
            }
            errFreqSum += pcrErrorRates[i];
        }

        return 3;
    }

    public float getSomaticFreq(int code) {
        Integer count = somaticCounts.get(code);

        return count == null ? 0.0f : (count / (float) total);
    }

    public MutationGenerator getSeqMutationGenerator() {
        return seqMutationGenerator;
    }

    public MutationGenerator getSomaticMutationGenerator() {
        return somaticMutationGenerator;
    }

    public MutationGenerator getPcrMutationGenerator() {
        return pcrMutationGenerator;
    }

    public VariantCallerParameters getVariantCallerParameters() {
        return variantCallerParameters;
    }
}
