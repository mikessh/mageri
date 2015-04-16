package com.milaboratory.oncomigec.core.align.kmer;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.util.IntArrayList;

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

        final double[] informationVector = new double[referenceCount];
        final double N = (double) referenceCount;
        double maxInformationValue = Double.MIN_VALUE,
                nextMaxInformationValue = Double.MIN_VALUE;
        int maxInformationId = 0;

        final long[] kmers = kmerUtils.extractKmers(sequence);

        for (long kmer : kmers) {
            KmerMap.KmerData kmerData = kmerMap.getData(kmer);
            if (kmerData != null) {
                // Note that we auto-correct repetitive k-mers by incrementing their count for the same reference
                double information = kmerData.getCounter() < referenceCount ?
                        -Math.log(kmerData.getCounter() / N) : 0.0;

                IntArrayList parentIds = kmerData.getParentSequenceIds();

                for (int j = 0; j < parentIds.size(); j++) {
                    int parentId = parentIds.get(j);
                    double parentInformation = informationVector[parentId] + information;
                    informationVector[parentId] = parentInformation;

                    if (parentInformation > maxInformationValue) {
                        nextMaxInformationValue = maxInformationValue;
                        maxInformationValue = parentInformation;
                        maxInformationId = parentId;
                    }
                }
            }
        }

        if (maxInformationId == 0) {
            // no hit
            return null;
        }

        boolean rc = maxInformationId < 0; // RC reference sequences are stored as -(index+1)

        return new KMerFinderResult(maxInformationValue,
                maxInformationValue - Math.max(0, nextMaxInformationValue),
                referenceLibrary.getAt((rc ? -maxInformationId : maxInformationId) - 1), // hash index is 1-based
                rc);
    }

    public ReferenceLibrary getReferenceLibrary() {
        return referenceLibrary;
    }
}
