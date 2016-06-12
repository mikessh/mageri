/*
 * Copyright (c) 2014-2015, Bolotin Dmitry, Chudakov Dmitry, Shugay Mikhail
 * (here and after addressed as Inventors)
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact the Inventors using one of the following
 * email addresses: chudakovdm@mail.ru, chudakovdm@gmail.com
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */
package com.milaboratory.mageri.core.genomic;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.io.fasta.FastaReader;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.mageri.pipeline.input.InputStreamWrapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class ReferenceLibrary implements Serializable {
    private final static String EMPTY_PATH = "NA";
    private final Set<Contig> contigs = new HashSet<>();
    private final List<Reference> references = new ArrayList<>();
    private final Map<String, Integer> nameToId = new HashMap<>();
    private final GenomicInfoProvider genomicInfoProvider;
    private final String path;
    private int warningCount = 0;
    private static final int MAX_WARNINGS = 10;

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
        this.path = EMPTY_PATH;
    }

    public ReferenceLibrary(Collection<SSequencingRead> fastaRecords) {
        this(fastaRecords, new BasicGenomicInfoProvider(), EMPTY_PATH);
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
        if (references.isEmpty()) {
            throw new RuntimeException("No references were loaded from provided FASTA records.");
        }
    }

    public synchronized void addReference(String name, NucleotideSequence sequence) {
        int index = references.size();

        if (nameToId.containsKey(name)) {
            throw new RuntimeException("Duplicate sequence names are not allowed. " + name);
        }

        GenomicInfo genomicInfo = genomicInfoProvider.get(name, sequence);

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
