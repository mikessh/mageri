package com.milaboratory.oncomigec.core.mapping.kmer;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.util.IntArrayList;

import java.util.HashMap;
import java.util.Map;

public class KMerFinder {
    public static final int DEFAULT_K = 11;

    private final KmerUtils kmerUtils;
    private final KmerMap kmerMap;
    private final ReferenceLibrary referenceLibrary;
    private final int referenceCount;

    public KMerFinder(ReferenceLibrary referenceLibrary) {
        this(referenceLibrary, DEFAULT_K);
    }

    public KMerFinder(ReferenceLibrary referenceLibrary, int k) {
        this.kmerUtils = new KmerUtils(k);
        this.kmerMap = kmerUtils.buildKmerMap(referenceLibrary);
        this.referenceLibrary = referenceLibrary;
        this.referenceCount = referenceLibrary.getReferences().size();
    }

    // Thread-safe
    public KMerFinderResult find(NucleotideSequence sequence) {
        if (sequence.size() < kmerUtils.getK()) {
            // sequence too short
            return null;
        }

        final Map<Integer, Double> informationMap = new HashMap<>();
        final double N = (double) kmerMap.getTotal();
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
