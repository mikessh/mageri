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

import com.antigenomics.mageri.core.genomic.Reference;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.antigenomics.mageri.core.genomic.ReferenceLibrary;

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

    public void countKmers(Reference reference, KmerMap kmerMap, boolean rc) {
        final NucleotideSequence sequence = rc ?
                reference.getSequence().getReverseComplement() :
                reference.getSequence();
        final int index = rc ?
                -(reference.getIndex() + 1) :
                (reference.getIndex() + 1);
        final int n = nKmers(sequence);
        for (int i = 0; i < n; ++i) {
            long kmer = 0;
            for (int j = i; j < i + k; ++j)
                kmer = kmer << 2 | sequence.codeAt(j);
            kmerMap.increment(kmer, index);
        }
    }

    public KmerMap buildKmerMap(ReferenceLibrary referenceLibrary) {
        final KmerMap kmerMap = new KmerMap();
        for (Reference reference : referenceLibrary.getReferences()) {
            countKmers(reference, kmerMap, true);
            countKmers(reference, kmerMap, false);
        }
        return kmerMap;
    }

    public long kmerByPosition(NucleotideSequence sequence, int pos) {
        long kmer = 0;
        for (int i = pos; i < pos + k; ++i)
            kmer = kmer << 2 | sequence.codeAt(i);
        return kmer;
    }

    public int getK() {
        return k;
    }
}