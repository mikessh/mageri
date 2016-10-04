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
import com.milaboratory.core.sequencing.io.fasta.FastaReader;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.antigenomics.mageri.pipeline.input.InputStreamWrapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class ReferenceLibrary implements Serializable {
    private final static String EMPTY_PATH = "NA";
    private final Set<Contig> contigs = new HashSet<>();
    private final List<Reference> references = new ArrayList<>();
    private final Map<String, Integer> nameToId = new HashMap<>();
    private final GenomicInfoProvider genomicInfoProvider;
    private final ReferenceLibraryParameters referenceLibraryParameters;
    private final String path;
    private int warningCount = 0;
    private static final int MAX_WARNINGS = 10;

    public static ReferenceLibrary fromInput(InputStreamWrapper input,
                                             GenomicInfoProvider genomicInfoProvider) throws IOException {
        return fromInput(input, genomicInfoProvider, ReferenceLibraryParameters.DEFAULT);
    }

    public static ReferenceLibrary fromInput(InputStreamWrapper input,
                                             GenomicInfoProvider genomicInfoProvider,
                                             ReferenceLibraryParameters referenceLibraryParameters) throws IOException {
        FastaReader reader = new FastaReader(input.getInputStream(), false);
        List<SSequencingRead> records = new LinkedList<>();
        SSequencingRead record;
        while ((record = reader.take()) != null) {
            records.add(record);
        }

        return new ReferenceLibrary(records, genomicInfoProvider, input.getFullPath(), referenceLibraryParameters);
    }

    public ReferenceLibrary() {
        this(new BasicGenomicInfoProvider());
    }

    public ReferenceLibrary(ReferenceLibraryParameters referenceLibraryParameters) {
        this(new BasicGenomicInfoProvider(), referenceLibraryParameters);
    }

    public ReferenceLibrary(GenomicInfoProvider genomicInfoProvider) {
        this(genomicInfoProvider, ReferenceLibraryParameters.DEFAULT);
    }


    public ReferenceLibrary(GenomicInfoProvider genomicInfoProvider,
                            ReferenceLibraryParameters referenceLibraryParameters) {
        this.genomicInfoProvider = genomicInfoProvider;
        this.path = EMPTY_PATH;
        this.referenceLibraryParameters = referenceLibraryParameters;
    }

    public ReferenceLibrary(Collection<SSequencingRead> fastaRecords) {
        this(fastaRecords, new BasicGenomicInfoProvider(), EMPTY_PATH);
    }

    public ReferenceLibrary(Collection<SSequencingRead> fastaRecords,
                            ReferenceLibraryParameters referenceLibraryParameters) {
        this(fastaRecords, new BasicGenomicInfoProvider(), EMPTY_PATH, referenceLibraryParameters);
    }

    public ReferenceLibrary(Collection<SSequencingRead> fastaRecords,
                            GenomicInfoProvider genomicInfoProvider, String path) {
        this(fastaRecords, genomicInfoProvider, path, ReferenceLibraryParameters.DEFAULT);
    }

    public ReferenceLibrary(Collection<SSequencingRead> fastaRecords,
                            GenomicInfoProvider genomicInfoProvider,
                            String path, ReferenceLibraryParameters referenceLibraryParameters) {
        this.genomicInfoProvider = genomicInfoProvider;
        this.path = path;
        this.referenceLibraryParameters = referenceLibraryParameters;
        for (SSequencingRead record : fastaRecords) {
            NucleotideSequence sequence = record.getData().getSequence();
            String[] descriptionFields = record.getDescription().split("[ \t]");
            addReference(descriptionFields[0],
                    sequence);
        }
        if (references.isEmpty()) {
            throw new RuntimeException("No references were loaded from provided FASTA records.");
        }
    }

    public synchronized void addReference(String name, NucleotideSequence sequence) {
        if (!referenceLibraryParameters.splitLargeReferences() || sequence.size() <= referenceLibraryParameters.getMaxReferenceLength()) {
            addReference(name, sequence, -1);
        } else {
            int offset = 0;
            int step = referenceLibraryParameters.getMaxReferenceLength() - referenceLibraryParameters.getReadLength();

            while (true) {
                int to = Math.min(offset + referenceLibraryParameters.getMaxReferenceLength(), sequence.size());

                if (sequence.size() - offset - step < referenceLibraryParameters.getReadLength()) {
                    // don't create short references
                    to = sequence.size();
                }

                addReference(name, sequence.getRange(offset, to), offset);

                if (to == sequence.size()) {
                    break;
                }

                offset += step;
            }
        }
    }

    public synchronized void addReference(String name, NucleotideSequence sequence, int offset) {
        int index = references.size();

        boolean noSuffix = offset < 0;
        offset = noSuffix ? 0 : offset;

        GenomicInfo genomicInfo = genomicInfoProvider.get(name, sequence, offset);

        if (!noSuffix) {
            name += "_" + offset;
        }

        if (nameToId.containsKey(name)) {
            throw new RuntimeException("Duplicate sequence names are not allowed. " + name);
        }

        if (genomicInfo == null) {
            if (++warningCount <= MAX_WARNINGS) {
                System.out.println("[WARNING] No genomic info for " + name +
                        ", skipping reference. (Showing first " + MAX_WARNINGS + " warnings)");
            }
            return;
        }

        if (genomicInfo.getContig() == null) {
            if (++warningCount <= MAX_WARNINGS) {
                System.out.println("[WARNING] No contig found for " + name +
                        ", skipping reference. (Showing first " + MAX_WARNINGS + " warnings)");
            }
            return;
        }

        contigs.add(genomicInfo.getContig());

        if (!genomicInfo.positiveStrand()) {
            // Only work with + strand
            sequence = sequence.getReverseComplement();
        }

        int maskedBases = offset <= 0 ? 0 : Math.min(sequence.size(), referenceLibraryParameters.getReadLength());

        Reference reference = new Reference(this, index, name, sequence, genomicInfo, maskedBases);

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

    public Set<Contig> getContigs() {
        return Collections.unmodifiableSet(contigs);
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(Reference.HEADER);
        for (Reference reference : references) {
            stringBuilder.append("\n").append(reference.toString());
        }
        return stringBuilder.toString();
    }
}
