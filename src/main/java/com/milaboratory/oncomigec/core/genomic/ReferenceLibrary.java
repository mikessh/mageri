/*
 * Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)
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
package com.milaboratory.oncomigec.core.genomic;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.io.fasta.FastaReader;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.oncomigec.pipeline.input.InputStreamWrapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class ReferenceLibrary implements Serializable {
    private final Map<Contig, Contig> contigs = new HashMap<>();
    private final List<Reference> references = new ArrayList<>();
    private final Map<String, Integer> nameToId = new HashMap<>();
    private final GenomicInfoProvider genomicInfoProvider;
    private final String path;

    public static ReferenceLibrary fromInput(InputStreamWrapper input,
                                             GenomicInfoProvider genomicInfoProvider) throws IOException {
        FastaReader reader = new FastaReader(input.getInputStream(), false);
        List<SSequencingRead> records = new LinkedList<>();
        SSequencingRead record;
        while ((record = reader.take()) != null) {
            records.add(record);
        }

        return new ReferenceLibrary(records, genomicInfoProvider, input.getFullPath());
    }

    public ReferenceLibrary() {
        this(new BasicGenomicInfoProvider());
    }

    public ReferenceLibrary(GenomicInfoProvider genomicInfoProvider) {
        this.genomicInfoProvider = genomicInfoProvider;
        this.path = "NA";
    }

    public ReferenceLibrary(Collection<SSequencingRead> fastaRecords) {
        this(fastaRecords, new BasicGenomicInfoProvider(), "NA");
    }

    public ReferenceLibrary(Collection<SSequencingRead> fastaRecords,
                            GenomicInfoProvider genomicInfoProvider,
                            String path) {
        this.genomicInfoProvider = genomicInfoProvider;
        this.path = path;
        for (SSequencingRead record : fastaRecords) {
            NucleotideSequence sequence = record.getData().getSequence();
            String[] descriptionFields = record.getDescription().split("[ \t]");
            addReference(descriptionFields[0],
                    sequence);
        }
    }

    public synchronized void addReference(String name, NucleotideSequence sequence) {
        int index = references.size();

        if (nameToId.containsKey(name)) {
            throw new RuntimeException("Duplicate sequence names are not allowed. " + name);
        }

        GenomicInfo genomicInfo = genomicInfoProvider.get(name, sequence);

        if (genomicInfo == null) {
            System.out.println("[WARNING] No genomic info for " + name +
                    ", skipping reference.");
            return;
        }

        if (!genomicInfo.positiveStrand()) {
            // Only work with + strand
            sequence = sequence.getReverseComplement();
        }

        Reference reference = new Reference(this, index, name, sequence, genomicInfo);

        nameToId.put(name, index);
        references.add(reference);
    }

    public Reference getAt(int index) {
        if (index < 0 || index >= references.size())
            throw new IndexOutOfBoundsException();
        return references.get(index);
    }

    public Reference getByName(String name) {
        return getAt(nameToId.get(name));
    }

    public List<Reference> getReferences() {
        return Collections.unmodifiableList(references);
    }

    public GenomicInfoProvider getGenomicInfoProvider() {
        return genomicInfoProvider;
    }

    public int size() {
        return references.size();
    }

    public String getPath() {
        return path;
    }
}
