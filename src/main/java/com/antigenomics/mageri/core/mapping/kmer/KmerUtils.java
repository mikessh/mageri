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
    private final int k, mid;
    private final boolean spacedSeeds;

    public KmerUtils(int k, boolean spacedSeeds) {
        if (k < 3 || k > 31)
            throw new IllegalArgumentException("K-mer length should be in [3, 31] (64bit)");
        this.k = k;
        this.mid = k / 2;
        this.spacedSeeds = spacedSeeds;
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
        if (spacedSeeds) {
            for (int i = 0; i < n; ++i) {
                kmers[i] = getKmerSpaced(sequence, i);
            }
        } else {
            for (int i = 0; i < n; ++i) {
                kmers[i] = getKmer(sequence, i);
            }
        }
        return kmers;
    }

    public void countKmers(Reference reference, KmerMap kmerMap, boolean rc) {
        if (rc) {
            NucleotideSequence sequence = reference.getSequence().getReverseComplement();

            int index = -reference.getIndex() - 1;

            int nMaskedBases = reference.size() - reference.getnMaskedBases() - k;

            long[] kmers = extractKmers(sequence);
            for (int i = 0; i < kmers.length; i++) {
                long kmer = kmers[i];
                kmerMap.increment(kmer, index, i <= nMaskedBases);
                // Do not increment count for masked bases
                // we don't lower information of a given K-mer,
                // still it points towards its parent reference
            }
        } else {
            NucleotideSequence sequence = reference.getSequence();

            int index = reference.getIndex() + 1;

            int nMaskedBases = reference.getnMaskedBases();

            long[] kmers = extractKmers(sequence);
            for (int i = 0; i < kmers.length; i++) {
                long kmer = kmers[i];
                kmerMap.increment(kmer, index, i >= nMaskedBases);
            }
        }
    }

    protected long getKmer(NucleotideSequence sequence, int pos) {
        long kmer = 0;
        for (int j = pos; j < pos + k; ++j) {
            kmer = kmer << 2 | sequence.codeAt(j);
        }
        return kmer;
    }

    protected long getKmerSpaced(NucleotideSequence sequence, int pos) {
        long kmer = 0;
        int j = pos;
        for (; j < pos + mid; ++j) {
            kmer = kmer << 2 | sequence.codeAt(j);
        }
        ++j;
        for (; j < pos + k; ++j) {
            kmer = kmer << 2 | sequence.codeAt(j);
        }
        return kmer;
    }

    public KmerMap buildKmerMap(ReferenceLibrary referenceLibrary) {
        final KmerMap kmerMap = new KmerMap();
        for (Reference reference : referenceLibrary.getReferences()) {
            countKmers(reference, kmerMap, true);
            countKmers(reference, kmerMap, false);
        }
        return kmerMap;
    }

    public int getK() {
        return k;
    }
}
