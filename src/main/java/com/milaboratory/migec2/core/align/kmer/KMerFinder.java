package com.milaboratory.migec2.core.align.kmer;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.migec2.core.align.reference.ReferenceLibrary;
import com.milaboratory.util.IntArrayList;

public class KMerFinder {
    private final KmerUtils kmerUtils;
    private final KmerMap kmerMap;
    private final ReferenceLibrary referenceLibrary;
    private final int referenceCount;

    public KMerFinder(ReferenceLibrary referenceLibrary) {
        this(referenceLibrary, 11);
    }

    public KMerFinder(ReferenceLibrary referenceLibrary, int k) {
        this.kmerUtils = new KmerUtils(k);
        this.kmerMap = kmerUtils.buildKmerMap(referenceLibrary);
        this.referenceLibrary = referenceLibrary;
        this.referenceCount = referenceLibrary.getReferences().size();
    }

    // Thread-safe
    public KMerFinderResult find(NucleotideSequence sequence) {
        final double[] informationVector = new double[referenceCount];
        final double N = (double) referenceCount;
        double maxInformationValue = Double.MIN_VALUE;
        int maxInformationId = -1;

        final long[] kmers = kmerUtils.extractKmers(sequence);

        for (int i = 0; i < kmers.length; i++) {
            KmerMap.KmerData kmerData = kmerMap.getData(kmers[i]);
            if (kmerData != null) {
                // Assume that we auto-correct repetitive k-mers by incrementing their count for the same reference
                double information = kmerData.getCounter() < referenceCount ?
                        -Math.log(kmerData.getCounter() / N) : 0.0;

                IntArrayList parentIds = kmerData.getParentSequenceIds();

                for (int j = 0; j < parentIds.size(); j++) {
                    int parentId = parentIds.get(j);
                    double parentInformation = informationVector[parentId] + information;
                    informationVector[parentId] = parentInformation;

                    if (parentInformation > maxInformationValue) {
                        maxInformationValue = parentInformation;
                        maxInformationId = parentId;
                    }
                }
            }
        }

        // todo: filter by value

        if (maxInformationId < 0)
            return null;

        maxInformationValue /= Math.log(N) * kmers.length;

        return new KMerFinderResult(maxInformationValue, referenceLibrary.getByGlobalId(maxInformationId));
    }
}
