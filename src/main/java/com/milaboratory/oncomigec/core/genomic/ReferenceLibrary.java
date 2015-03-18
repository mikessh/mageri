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
    private final Set<NucleotideSequence> referenceSequences = new HashSet<>();
    private final GenomicInfoProvider genomicInfoProvider;
    private final String path;
    private int globalId = 0, uniqueCount = 0;

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
            addReferenceAndRC(descriptionFields[0],
                    sequence);
        }
    }

    synchronized void addReference(String name, NucleotideSequence sequence, boolean rc) {
        Reference reference = new Reference(this, globalId, name, sequence, rc);

        sequence = rc ? sequence.getReverseComplement() : sequence;
        String fullName = reference.getFullName();

        if (referenceSequences.contains(sequence))
            throw new RuntimeException("Duplicate sequences not allowed in reference library. " +
                    sequence);
        if (nameToId.containsKey(reference.getFullName()))
            throw new RuntimeException("Duplicate sequence names (with respect to reverse complement flag, _RC) " +
                    "are not allowed. " + reference.getFullName());

        genomicInfoProvider.annotate(reference);
        referenceSequences.add(sequence);
        nameToId.put(fullName, globalId);
        references.add(reference);

        globalId++;
        uniqueCount++;
    }

    public void addReference(String name, NucleotideSequence sequence) {
        addReference(name, sequence, false);
    }

    public void addReferenceAndRC(String name, NucleotideSequence sequence) {
        addReference(name, sequence, false);
        addReference(name, sequence, true);
        uniqueCount--;
    }

    public Reference getByGlobalId(int globalId) {
        if (globalId < 0 || globalId >= references.size())
            throw new IndexOutOfBoundsException();
        return references.get(globalId);
    }

    public Reference getByName(String name, boolean rc) {
        return getByGlobalId(nameToId.get(name + (rc ? "_RC" : "")));
    }

    public Reference getByName(String name) {
        return getByName(name, false);
    }

    public List<Reference> getReferences() {
        return Collections.unmodifiableList(references);
    }

    public GenomicInfoProvider getGenomicInfoProvider() {
        return genomicInfoProvider;
    }

    public int size() {
        return globalId;
    }

    public int uniqueCount() {
        return uniqueCount;
    }

    public String getPath() {
        return path;
    }
}
