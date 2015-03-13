/*
 * Copyright 2013-2014 Mikhail Shugay (mikhail.shugay@gmail.com)
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
 * Last modified on 1.12.2014 by mikesh
 */

package com.milaboratory.oncomigec.model.variant;

import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.core.consalign.entity.AlignerReferenceLibrary;

import java.util.HashMap;
import java.util.Map;

public class VariantLibrary {
    private final Map<Reference, VariantContainer> variantContainerMap = new HashMap<>();
    private final ReferenceLibrary referenceLibrary;

    public VariantLibrary(AlignerReferenceLibrary alignerReferenceLibrary) {
        this.referenceLibrary = alignerReferenceLibrary.getReferenceLibrary();
        for (Reference reference : referenceLibrary.getReferences()) {
            variantContainerMap.put(reference,
                    new VariantContainer(alignerReferenceLibrary.getMutationsAndCoverage(reference)));
        }
    }

    public VariantContainer getVariantContainer(Reference reference) {
        return variantContainerMap.get(reference);
    }

    public ReferenceLibrary getReferenceLibrary() {
        return referenceLibrary;
    }

    /**
     * Gets background substitution frequency, computed in MIG units.
     *
     * @param reference reference nucleotide
     * @param from      reference nucleotide
     * @param to        variant nucleotide
     * @param symmetric symmetrize resulting matrix
     * @return probability of substitution
     */
    public double getBgFreqMig(Reference reference, byte from, byte to, boolean symmetric) {
        return variantContainerMap.get(reference).getBgFreqMig(from, to, symmetric);
    }

    /**
     * Gets background substitution frequency, computed in MIG units.
     *
     * @param reference reference nucleotide
     * @param from      reference nucleotide
     * @param to        variant nucleotide
     * @return probability of substitution
     */
    public double getBgFreqMig(Reference reference, byte from, byte to) {
        return variantContainerMap.get(reference).getBgFreqMig(from, to, false);
    }

    /**
     * Gets background substitution frequency, computed in READ units.
     *
     * @param reference reference nucleotide
     * @param from      reference nucleotide code
     * @param to        variant nucleotide code
     * @param symmetric symmetrize resulting matrix
     * @return probability of substitution
     */
    public double getBgFreqRead(Reference reference, byte from, byte to, boolean symmetric) {
        return variantContainerMap.get(reference).getBgFreqRead(from, to, symmetric);
    }

    /**
     * Gets background substitution frequency, computed in READ units.
     *
     * @param reference reference nucleotide
     * @param from      reference nucleotide code
     * @param to        variant nucleotide code
     * @return probability of substitution
     */
    public double getBgFreqRead(Reference reference, byte from, byte to) {
        return variantContainerMap.get(reference).getBgFreqRead(from, to, false);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("#" + super.toString());

        for (Reference reference : referenceLibrary.getReferences()) {
            stringBuilder
                    .append("\n").append(reference.toString())
                    .append("\n").append(variantContainerMap.get(reference).toString());
        }

        return stringBuilder.toString();
    }
}
