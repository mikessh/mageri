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

package com.antigenomics.mageri.core.genomic;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;

import java.util.*;

public class BasicGenomicInfoProvider implements GenomicInfoProvider {
    private final Map<String, Contig> contigs = new HashMap<>();

    @Override
    public GenomicInfo get(String name) {
        return new GenomicInfo(new Contig(name,
                "user", -1, false), -1, -1, true);
    }

    @Override
    public GenomicInfo create(String name, NucleotideSequence sequence) {
        Contig contig = new Contig(name,
                "user", sequence.size(),
                false);

        contigs.put(name, contig);

        return new GenomicInfo(contig, 0, sequence.size(), true);
    }

    @Override
    public GenomicInfo createPartitioned(String name, NucleotideSequence sequence, int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset should be greater or equal to zero.");
        }

        Contig contig = contigs.get(name);
        if (contig == null) {
            if (offset != 0) {
                throw new IllegalArgumentException("Creating new contig with non-zero offset.");
            }
            contig = new Contig(name,
                    "user", sequence.size(),
                    false);
        } else {
            contig = new Contig(name,
                    "user", offset + sequence.size(),
                    false);
        }
        contigs.put(name, contig);

        return new GenomicInfo(contig, offset, // make 0-based to be consistent with BED format
                offset + sequence.size(), true); // sequence end is exclusive
    }

    @Override
    public List<Contig> getContigs() {
        List<Contig> contigs = new ArrayList<>();
        contigs.addAll(this.contigs.values());
        Collections.sort(contigs);
        return contigs;
    }

    @Override
    public int size() {
        return contigs.size();
    }
}
