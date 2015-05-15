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

package com.milaboratory.oncomigec.pipeline.analysis;

import com.milaboratory.oncomigec.core.PipelineBlock;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.core.mapping.ConsensusAligner;
import com.milaboratory.oncomigec.core.mapping.ConsensusAlignerParameters;
import com.milaboratory.oncomigec.core.mapping.PConsensusAlignerFactory;
import com.milaboratory.oncomigec.core.mapping.SConsensusAlignerFactory;
import com.milaboratory.oncomigec.core.mapping.alignment.AlignerFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class PipelineConsensusAlignerFactory extends PipelineBlock {
    private final Map<Sample, ConsensusAligner> alignersBySample = new HashMap<>();
    private final List<Reference> references;
    private final PConsensusAlignerFactory pairedFactory;
    private final SConsensusAlignerFactory singleFactory;

    public PipelineConsensusAlignerFactory(AlignerFactory alignerFactory,
                                           ConsensusAlignerParameters consensusAlignerParameters) {
        super("mapping");
        this.pairedFactory = new PConsensusAlignerFactory(alignerFactory, consensusAlignerParameters);
        this.singleFactory = new SConsensusAlignerFactory(alignerFactory, consensusAlignerParameters);
        ReferenceLibrary referenceLibrary = alignerFactory.getReferenceLibrary();
        this.references = new ArrayList<>(referenceLibrary.getReferences());

        Collections.sort(references, new Comparator<Reference>() {
            @Override
            public int compare(Reference o1, Reference o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    public ConsensusAligner create(Sample sample) {
        ConsensusAligner aligner = sample.isPairedEnd() ? pairedFactory.create() : singleFactory.create();
        alignersBySample.put(sample, aligner);
        return aligner;
    }

    @Override
    public String getHeader() {
        List<String> referenceNames = new ArrayList<>();

        for (Reference reference : references)
            referenceNames.add(reference.getName());

        return "sample.group\tsample\t" +
                "migs.good.alignment\tmigs.aligned\t" +
                "migs.chimeric\tmigs.skipped\tmigs.total";// +
                //StringUtils.join(referenceNames, "\t");
    }

    @Override
    public String getBody() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Sample sample : alignersBySample.keySet()) {
            ConsensusAligner aligner = alignersBySample.get(sample);
            stringBuilder.append(sample.getParent().getName()).append("\t").
                    append(sample.getName()).append("\t").
                    append(aligner.getGoodAlignmentMigs()).append("\t").
                    append(aligner.getAlignedMigs()).append("\t").
                    append(aligner.getChimericMigs()).append("\t").
                    append(aligner.getSkippedMigs()).append("\t").
                    append(aligner.getTotalMigs());

            /*for (Reference reference : references) {
                MutationsTable mutationsTable = aligner.getAlignerTable(reference);
                stringBuilder.append("\t").append(mutationsTable.getMigCount());
            }*/

            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
