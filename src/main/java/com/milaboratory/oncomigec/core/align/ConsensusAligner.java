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
package com.milaboratory.oncomigec.core.align;

import cc.redberry.pipe.Processor;
import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import com.milaboratory.oncomigec.misc.ReadSpecific;
import com.milaboratory.oncomigec.core.PipelineBlock;
import com.milaboratory.oncomigec.core.align.sequence.Aligner;
import com.milaboratory.oncomigec.core.align.sequence.AlignmentResult;
import com.milaboratory.oncomigec.core.assemble.Consensus;
import com.milaboratory.oncomigec.core.assemble.SConsensus;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.core.mutations.MutationArray;
import com.milaboratory.oncomigec.core.mutations.MutationsExtractor;
import com.milaboratory.oncomigec.misc.ProcessorResultWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ConsensusAligner<ConsensusType extends Consensus> extends PipelineBlock
        implements Processor<ProcessorResultWrapper<ConsensusType>, ProcessorResultWrapper<AlignedConsensus>>,
        ReadSpecific {
    protected final AtomicInteger alignedMigs = new AtomicInteger(), failedMigs = new AtomicInteger(),
            skippedMigs = new AtomicInteger(),
            chimericMigs = new AtomicInteger(),
            overlapped = new AtomicInteger(),
            failedOverlapped = new AtomicInteger(),
            failedR1 = new AtomicInteger(), failedR2 = new AtomicInteger();
    protected final Map<Reference, AlignerTable> alignerTableByReference = new HashMap<>();
    protected transient final Aligner aligner;
    protected final ReferenceLibrary referenceLibrary;
    protected final ConsensusAlignerParameters parameters;

    protected ConsensusAligner(Aligner aligner, ConsensusAlignerParameters parameters) {
        super("aligner");
        this.aligner = aligner;
        this.referenceLibrary = aligner.getReferenceLibrary();
        this.parameters = parameters;
        for (Reference reference : referenceLibrary.getReferences()) {
            alignerTableByReference.put(reference, new AlignerTable(reference));
        }
    }

    @SuppressWarnings("unchecked")
    public ProcessorResultWrapper<AlignedConsensus> process(ProcessorResultWrapper<ConsensusType> assemblerResult) {
        if (assemblerResult.hasResult()) {
            ConsensusType consensus = assemblerResult.getResult();

            AlignedConsensus alignmentData = align(consensus);

            if (alignmentData == null) {
                failedMigs.incrementAndGet();
                return ProcessorResultWrapper.BLANK;
            } else {
                alignedMigs.incrementAndGet();
                return new ProcessorResultWrapper<>(alignmentData);
            }
        }

        skippedMigs.incrementAndGet();
        return ProcessorResultWrapper.BLANK;
    }

    protected MutationArray update(AlignmentResult result,
                                   SConsensus consensus) {
        return update(result, consensus.getConsensusSQPair(), consensus.getMinors());
    }

    protected MutationArray update(AlignmentResult result,
                                   NucleotideSQPair consensus,
                                   Set<Integer> minors) {
        Reference reference = result.getReference();
        LocalAlignment alignment = result.getAlignment();
        boolean rc = result.isReverseComplement();

        MutationsExtractor mutationsExtractor = new MutationsExtractor(
                alignment, reference, consensus,
                minors,
                parameters.getConsensusQualityThreshold(),
                rc);

        MutationArray majorMutations = mutationsExtractor.computeMajorMutations();
        Set<Integer> minorMutations = mutationsExtractor.recomputeMinorMutations();

        alignerTableByReference.get(reference).append(alignment,
                consensus.getQuality(),
                majorMutations, minorMutations);

        return majorMutations;
    }

    public abstract AlignedConsensus align(ConsensusType consensus);

    public int getAlignedMigs() {
        return alignedMigs.get();
    }

    public int getSkippedMigs() {
        return skippedMigs.get();
    }

    public int getFailedMigs() {
        return failedMigs.get();
    }

    public int getFailedR1() {
        return failedR1.get();
    }

    public int getFailedR2() {
        return failedR2.get();
    }

    public int getOverlapped() {
        return overlapped.get();
    }

    public int getFailedOverlapped() {
        return failedOverlapped.get();
    }

    public int getChimericMigs() {
        return chimericMigs.get();
    }

    public ReferenceLibrary getReferenceLibrary() {
        return referenceLibrary;
    }

    public AlignerTable getAlignerTable(Reference reference) {
        return alignerTableByReference.get(reference);
    }

    public void clear() {
        alignerTableByReference.clear();
    }

    @Override
    public String getHeader() {
        String header = "reference\tpos\tcoverage\tcqs.sum",
                major = "", minor = "";

        for (byte i = 0; i < 4; i++) {
            char symbol = NucleotideAlphabet.INSTANCE.symbolFromCode(i);
            major += "\t" + symbol + ".major";
            minor += "\t" + symbol + ".minor";
        }

        return header + major + minor;
    }

    @Override
    public String getBody() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Reference reference : referenceLibrary.getReferences()) {
            AlignerTable alignerTable = alignerTableByReference.get(reference);

            if (alignerTable.wasUpdated()) {
                for (int i = 0; i < reference.getSequence().size(); i++) {
                    stringBuilder.append(reference.getName()).append("\t").
                            append(i + 1).append("\t").
                            append(alignerTable.getMigCoverage(i)).append("\t").
                            append(alignerTable.getCqsSumCoverage(i));

                    StringBuilder major = new StringBuilder(), minor = new StringBuilder();

                    for (byte j = 0; j < 4; j++) {
                        major.append("\t").append(alignerTable.getMajorMigCount(i, j));
                        minor.append("\t").append(alignerTable.getMinorMigCount(i, j));
                    }

                    stringBuilder.append(major).append(minor).append("\n");
                }
            }
        }
        return stringBuilder.toString();
    }
}
