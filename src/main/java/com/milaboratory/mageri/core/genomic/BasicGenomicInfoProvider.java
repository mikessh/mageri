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

import java.util.*;

public class BasicGenomicInfoProvider implements GenomicInfoProvider {
    private final Map<Contig, Contig> contigs = new HashMap<>();

    @Override
    public GenomicInfo get(String name, NucleotideSequence sequence) {
        Contig contig = new Contig(name,
                "user", sequence.size(),
                false);

        Contig existing = contigs.get(contig);
        if (existing == null) {
            contigs.put(contig, contig);
            existing = contig;
        }

        return new GenomicInfo(existing, 0, // make 0-based to be consistent with BED format
                sequence.size() - 1, true);
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
