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
package com.milaboratory.oncomigec.core.consalign.processor;

import cc.redberry.pipe.Processor;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import com.milaboratory.oncomigec.ReadSpecific;
import com.milaboratory.oncomigec.core.PipelineBlock;
import com.milaboratory.oncomigec.core.align.processor.Aligner;
import com.milaboratory.oncomigec.core.assemble.entity.Consensus;
import com.milaboratory.oncomigec.core.consalign.entity.AlignedConsensus;
import com.milaboratory.oncomigec.core.consalign.entity.AlignerReferenceLibrary;
import com.milaboratory.oncomigec.core.consalign.misc.ConsensusAlignerParameters;
import com.milaboratory.oncomigec.core.consalign.mutations.MutationsAndCoverage;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.util.ProcessorResultWrapper;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class ConsensusAligner<T extends Consensus> extends PipelineBlock
        implements Processor<ProcessorResultWrapper<T>, ProcessorResultWrapper<AlignedConsensus>>,
        ReadSpecific {
    protected final AtomicInteger alignedMigs = new AtomicInteger(), badMigs = new AtomicInteger(),
            chimericMigs = new AtomicInteger(), skippedMigs = new AtomicInteger();
    protected transient final Aligner aligner;
    protected final AlignerReferenceLibrary alignerReferenceLibrary;
    protected final ConsensusAlignerParameters parameters;

    protected ConsensusAligner(Aligner aligner, ConsensusAlignerParameters parameters) {
        super("aligner");
        this.aligner = aligner;
        this.alignerReferenceLibrary = new AlignerReferenceLibrary(aligner.getReferenceLibrary());
        this.parameters = parameters;
    }

    @SuppressWarnings("unchecked")
    public ProcessorResultWrapper<AlignedConsensus> process(ProcessorResultWrapper<T> consensus) {
        if (consensus.hasResult()) {
            AlignedConsensus alignmentData = align(consensus.getResult());

            if (alignmentData == null) {
                badMigs.incrementAndGet();
                return ProcessorResultWrapper.BLANK;
            } else {
                for (int i = 1; i < alignmentData.getNumberOfReferences(); i++) {
                    if (alignmentData.getReference(i - 1) != alignmentData.getReference(i)) {
                        chimericMigs.incrementAndGet();
                        return ProcessorResultWrapper.BLANK;
                    }
                }
                alignedMigs.incrementAndGet();
                return new ProcessorResultWrapper<>(alignmentData);
            }
        }

        skippedMigs.incrementAndGet();
        return ProcessorResultWrapper.BLANK;
    }

    public abstract AlignedConsensus align(T consensus);

    public AlignerReferenceLibrary getAlignerReferenceLibrary() {
        return alignerReferenceLibrary;
    }

    public int getAlignedMigs() {
        return alignedMigs.get();
    }

    public int getSkippedMigs() {
        return skippedMigs.get();
    }

    public int getBadMigs() {
        return badMigs.get();
    }

    public int getChimericMigs() {
        return chimericMigs.get();
    }

    @Override
    public String getHeader() {
        String header = "reference\tpos", subst = "", ins = "", del = "";

        for (byte i = 0; i < 4; i++) {
            char symbol = NucleotideAlphabet.INSTANCE.symbolFromCode(i);
            subst += "\t" + symbol;
            ins += "\tI:" + symbol;
            del += "\tD:" + symbol;
        }

        return header + subst + ins + del;
    }

    @Override
    public String getBody() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Reference reference : alignerReferenceLibrary.getReferenceLibrary().getReferences()) {
            MutationsAndCoverage mutationsAndCoverage = alignerReferenceLibrary.getMutationsAndCoverage(reference);

            if (mutationsAndCoverage.wasUpdated()) {
                for (int i = 0; i < reference.getSequence().size(); i++) {
                    stringBuilder.append(reference.getFullName()).append("\t").
                            append(i + 1);
                    StringBuilder subst = new StringBuilder(), ins = new StringBuilder(), del = new StringBuilder();
                    for (byte j = 0; j < 4; j++) {
                        int insCode = Mutations.createInsertion(i, j), delCode = Mutations.createDeletion(i, j);

                        subst.append("\t").append(mutationsAndCoverage.getMajorNucleotideMigCount(i, j));
                        ins.append("\t").append(mutationsAndCoverage.getMajorIndelMigCount(insCode));
                        del.append("\t").append(mutationsAndCoverage.getMajorIndelMigCount(delCode));
                    }
                    stringBuilder.append(subst).append(ins).append(del).append("\n");
                }
            }
        }
        return stringBuilder.toString();
    }
}
