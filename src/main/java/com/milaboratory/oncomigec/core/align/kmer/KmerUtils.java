package com.milaboratory.oncomigec.core.align.kmer;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;

public class KmerUtils {
    private final int k;

    public KmerUtils(int k) {
        if (k < 0 || k > 31)
            throw new IllegalArgumentException("K-mer length should be in [0, 31] (64bit)");
        this.k = k;
    }

    private int nKmers(NucleotideSequence sequence) {
        int nKmers = sequence.size() - k + 1;
        if (nKmers < 0)
            throw new IllegalArgumentException("Sequence size less than k-mer size");
        return nKmers;
    }

    public long[] extractKmers(NucleotideSequence sequence) {
        final int n = nKmers(sequence);
        final long[] kmers = new long[n];
        for (int i = 0; i < n; ++i) {
            long kmer = 0;
            for (int j = i; j < i + k; ++j)
                kmer = kmer << 2 | sequence.codeAt(j);
            kmers[i] = kmer;
        }
        return kmers;
    }

    public void countKmers(Reference reference, KmerMap kmerMap) {
        final NucleotideSequence sequence = reference.getSequence();
        final int globalId = reference.getGlobalId();
        final int n = nKmers(sequence);
        for (int i = 0; i < n; ++i) {
            long kmer = 0;
            for (int j = i; j < i + k; ++j)
                kmer = kmer << 2 | sequence.codeAt(j);
            kmerMap.increment(kmer, globalId);
        }
    }

    public KmerMap buildKmerMap(ReferenceLibrary referenceLibrary) {
        final KmerMap kmerMap = new KmerMap();
        for (Reference reference : referenceLibrary.getReferences())
            countKmers(reference, kmerMap);
        return kmerMap;
    }

    public long kmerByPosition(NucleotideSequence sequence, int pos) {
        long kmer = 0;
        for (int i = pos; i < pos + k; ++i)
            kmer = kmer << 2 | sequence.codeAt(i);
        return kmer;
    }
}
