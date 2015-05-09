/*
 * Copyright 2013-2015 Mikhail Shugay (mikhail.shugay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last modified on 17.3.2015 by mikesh
 */

package com.milaboratory.oncomigec.core.genomic;

import java.util.*;

public class BasicGenomicInfoProvider implements GenomicInfoProvider {
    private final Map<Contig, Contig> contigs = new HashMap<>();

    @Override
    public void annotate(Reference reference) {
        Contig contig = new Contig(reference.getName(),
                "user", reference.getSequence().size());

        Contig existing = contigs.get(contig);
        if (existing == null) {
            contigs.put(contig, contig);
            existing = contig;
        }

        reference.setGenomicInfo(new GenomicInfo(existing, 0, // make 0-based to be consistent with BED format
                reference.getSequence().size() - 1));
    }

    @Override
    public List<Contig> getContigs() {
        List<Contig> contigs = new ArrayList<>();
        contigs.addAll(this.contigs.values());
        Collections.sort(contigs);
        return contigs;
    }
}
