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
import com.milaboratory.mageri.pipeline.input.InputStreamWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

public class BedGenomicInfoProvider implements GenomicInfoProvider {
    private static final String UNUSED_IN_SAM_BAM = "PRIVATE";
    
    private final static Pattern correctBedLine = Pattern.compile("^\\S+[\t ]+\\d+[\t ]+\\d+[\t ]+\\S+[\t ]+\\d+[\t ]+[+-]");
    private final Map<String, GenomicInfo> records = new HashMap<>();
    private final List<Contig> contigs;

    public BedGenomicInfoProvider(InputStreamWrapper bedRecords, InputStreamWrapper contigRecords) throws IOException {
        InputStream inputStream = contigRecords.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String[] splitLine;

        Map<String, Contig> contigByName = new HashMap<>();

        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("#")) {
                splitLine = line.split("[\t ]+");
                Contig contig = new Contig(splitLine[0], splitLine[1], Integer.parseInt(splitLine[2]),
                        splitLine[1].toUpperCase().equals(UNUSED_IN_SAM_BAM));
                contigByName.put(contig.getID(), contig);
            }
        }

        this.contigs = new ArrayList<>(contigByName.values());
        
        Collections.sort(contigs);

        inputStream = bedRecords.getInputStream();
        reader = new BufferedReader(new InputStreamReader(inputStream));

        while ((line = reader.readLine()) != null) {
            if (correctBedLine.matcher(line).find()) {
                splitLine = line.split("[\t ]+");
                GenomicInfo genomicInfo = new GenomicInfo(
                        contigByName.get(splitLine[0]),
                        Integer.parseInt(splitLine[1]),
                        Integer.parseInt(splitLine[2]),
                        splitLine[5].equals("+"));
                records.put(splitLine[3], genomicInfo);
            }
        }
    }

    @Override
    public GenomicInfo get(String name, NucleotideSequence sequence) {
        return records.get(name);
    }

    @Override
    public List<Contig> getContigs() {
        return Collections.unmodifiableList(contigs);
    }
}
