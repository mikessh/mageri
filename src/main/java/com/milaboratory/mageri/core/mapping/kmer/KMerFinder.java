/*
 * Copyright (c) 2014-2015, Bolotin Dmitry, Chudakov Dmitry, Shugay Mikhail
 * (here and after addressed as Inventors)
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact the Inventors using one of the following
 * email addresses: chudakovdm@mail.ru, chudakovdm@gmail.com
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */

package com.milaboratory.mageri.core.mapping.kmer;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.mageri.core.genomic.ReferenceLibrary;
import com.milaboratory.util.IntArrayList;

import java.util.HashMap;
import java.util.Map;

public class KMerFinder {
    public static final int DEFAULT_K = 11;

    private final KmerUtils kmerUtils;
    private final KmerMap kmerMap;
    private final ReferenceLibrary referenceLibrary;
    private final double N;

    public KMerFinder(ReferenceLibrary referenceLibrary) {
        this(referenceLibrary, DEFAULT_K);
    }

    public KMerFinder(ReferenceLibrary referenceLibrary, int k) {
        this.kmerUtils = new KmerUtils(k);
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
                10 * (maxInformationValue - Math.max(0, nextMaxInformationValue)) / Math.log(10),
                referenceLibrary.getAt((rc ? -maxInformationId : maxInformationId) - 1), // hash index is 1-based
                rc);
    }

    public ReferenceLibrary getReferenceLibrary() {
        return referenceLibrary;
    }
}
