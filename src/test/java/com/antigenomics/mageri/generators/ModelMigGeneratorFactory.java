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

import com.antigenomics.mageri.core.variant.VariantCallerParameters;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.antigenomics.mageri.core.variant.model.MinorBasedErrorModel;

public class ModelMigGeneratorFactory {
    private double hotSpotPositionRatio = 0.1, pcrPositionRatio = 0.4,
            somaticMutationRatio = 0.1, somaticMutationFreq = 0.0005;
    private VariantCallerParameters variantCallerParameters = VariantCallerParameters.DEFAULT;
    private MutationGenerator readErrorGenerator = MutationGenerator.NO_INDEL,
            pcrErrorGenerator = MutationGenerator.NO_INDEL_SKEWED;

    public ModelMigGenerator create(NucleotideSequence reference) {
        return new ModelMigGenerator(hotSpotPositionRatio, pcrPositionRatio, somaticMutationRatio,
                somaticMutationFreq, variantCallerParameters, readErrorGenerator,
                pcrErrorGenerator, reference);
    }

    public double getHotSpotPositionRatio() {
        return hotSpotPositionRatio;
    }

    public double getPcrPositionRatio() {
        return pcrPositionRatio;
    }

    public double getSomaticMutationRatio() {
        return somaticMutationRatio;
    }

    public double getSomaticMutationFreq() {
        return somaticMutationFreq;
    }

    public VariantCallerParameters getVariantCallerParameters() {
        return variantCallerParameters;
    }

    public MutationGenerator getReadErrorGenerator() {
        return readErrorGenerator;
    }

    public MutationGenerator getPcrErrorGenerator() {
        return pcrErrorGenerator;
    }

    public void setHotSpotPositionRatio(double hotSpotPositionRatio) {
        this.hotSpotPositionRatio = hotSpotPositionRatio;
    }

    public void setPcrPositionRatio(double pcrPositionRatio) {
        this.pcrPositionRatio = pcrPositionRatio;
    }

    public void setSomaticMutationRatio(double somaticMutationRatio) {
        this.somaticMutationRatio = somaticMutationRatio;
    }

    public void setSomaticMutationFreq(double somaticMutationFreq) {
        this.somaticMutationFreq = somaticMutationFreq;
    }

    public void setReadErrorGenerator(MutationGenerator readErrorGenerator) {
        this.readErrorGenerator = readErrorGenerator;
    }

    public void setPcrErrorGenerator(MutationGenerator pcrErrorGenerator) {
        this.pcrErrorGenerator = pcrErrorGenerator;
    }
}
