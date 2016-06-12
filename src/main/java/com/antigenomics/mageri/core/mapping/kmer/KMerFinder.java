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

package com.antigenomics.mageri.core.mapping.kmer;

import com.antigenomics.mageri.core.mapping.ConsensusAlignerParameters;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.antigenomics.mageri.core.genomic.ReferenceLibrary;
import com.milaboratory.util.IntArrayList;

import java.util.HashMap;
import java.util.Map;

public class KMerFinder {
    private final KmerUtils kmerUtils;
    private final KmerMap kmerMap;
    private final ReferenceLibrary referenceLibrary;
    private final double N;

    public KMerFinder(ReferenceLibrary referenceLibrary) {
        this(referenceLibrary, ConsensusAlignerParameters.DEFAULT);
    }

    public KMerFinder(ReferenceLibrary referenceLibrary, ConsensusAlignerParameters alignerParameters) {
        this.kmerUtils = new KmerUtils(alignerParameters.getK());
        this.kmerMap = kmerUtils.buildKmerMap(referenceLibrary);
        this.referenceLibrary = referenceLibrary;
        this.N = (double) kmerMap.getTotal();
    }

    // Thread-safe
    public KMerFinderResult find(NucleotideSequence sequence) {
        if (sequence.size() < kmerUtils.getK()) {
            // sequence too short
            return null;
        }

        final Map<Integer, Double> informationMap = new HashMap<>();
        double maxInformationValue = Double.MIN_VALUE,
                nextMaxInformationValue = Double.MIN_VALUE;
        int maxInformationId = 0;

        final long[] kmers = kmerUtils.extractKmers(sequence);

        for (long kmer : kmers) {
            KmerMap.KmerData kmerData = kmerMap.getData(kmer);
            if (kmerData != null) {
                // Note that we auto-correct repetitive k-mers by incrementing their count for the same reference
                double information = -Math.log(kmerData.getCounter() / N);

                IntArrayList parentIds = kmerData.getParentSequenceIds();

                for (int j = 0; j < parentIds.size(); j++) {
                    int parentId = parentIds.get(j);

                    Double parentInformation = informationMap.get(parentId);
                    parentInformation = parentInformation == null ? information : (parentInformation + information);

                    if (parentInformation > maxInformationValue) {
                        maxInformationValue = parentInformation;
                        if (maxInformationId != parentId) {
                            maxInformationId = parentId;
                            nextMaxInformationValue = maxInformationValue;
                        }
                    }

                    informationMap.put(parentId, parentInformation);
                }
            }
        }

        if (maxInformationId == 0) {
            // no hit
            return null;
        }

        maxInformationValue /= kmers.length;
        nextMaxInformationValue /= kmers.length;

        boolean rc = maxInformationId < 0; // RC reference sequences are stored as -(index+1)

        return new KMerFinderResult(maxInformationValue,
                (byte) Math.min(10 * (maxInformationValue - Math.max(0, nextMaxInformationValue)) / Math.log(10), Byte.MAX_VALUE),
                referenceLibrary.getAt((rc ? -maxInformationId : maxInformationId) - 1), // hash index is 1-based
                rc);
    }

    public ReferenceLibrary getReferenceLibrary() {
        return referenceLibrary;
    }
}
