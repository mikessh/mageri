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
package com.milaboratory.oncomigec.core.mapping;

import cc.redberry.pipe.Processor;
import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import com.milaboratory.oncomigec.core.PipelineBlock;
import com.milaboratory.oncomigec.core.ReadSpecific;
import com.milaboratory.oncomigec.core.assemble.Consensus;
import com.milaboratory.oncomigec.core.assemble.SConsensus;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.core.mapping.alignment.Aligner;
import com.milaboratory.oncomigec.core.mapping.alignment.AlignmentResult;
import com.milaboratory.oncomigec.core.mutations.MutationArray;
import com.milaboratory.oncomigec.core.mutations.MutationsExtractor;
import com.milaboratory.oncomigec.misc.ProcessorResultWrapper;
import com.milaboratory.oncomigec.pipeline.Speaker;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ConsensusAligner<ConsensusType extends Consensus> extends PipelineBlock
        implements Processor<ProcessorResultWrapper<ConsensusType>, ProcessorResultWrapper<AlignedConsensus>>,
        ReadSpecific {
    protected final Map<Reference, MutationsTable> alignerTableByReference = new HashMap<>();
    protected transient final Aligner aligner;
    protected final ReferenceLibrary referenceLibrary;
    protected final ConsensusAlignerParameters parameters;
    protected final AtomicInteger alignedMigs = new AtomicInteger(),
            goodAlignmentMigs = new AtomicInteger(),
            skippedMigs = new AtomicInteger(),
            chimericMigs = new AtomicInteger(),
            totalMigs = new AtomicInteger();
    protected boolean cleared = false;

    protected ConsensusAligner(Aligner aligner, ConsensusAlignerParameters parameters) {
        super("mapper");
        this.aligner = aligner;
        this.referenceLibrary = aligner.getReferenceLibrary();
        this.parameters = parameters;
        for (Reference reference : referenceLibrary.getReferences()) {
            alignerTableByReference.put(reference, new MutationsTable(reference));
        }
    }

    @SuppressWarnings("unchecked")
    public ProcessorResultWrapper<AlignedConsensus> process(ProcessorResultWrapper<ConsensusType> assemblerResult) {
        totalMigs.incrementAndGet();

        if (assemblerResult.hasResult()) {
            ConsensusType consensus = assemblerResult.getResult();

            AlignedConsensus alignedConsensus = align(consensus);

            if (alignedConsensus.isMapped()) {
                alignedMigs.incrementAndGet();
            }
            if (alignedConsensus.isAligned()) {
                goodAlignmentMigs.incrementAndGet();
            }
            if (alignedConsensus.isChimeric()) {
                chimericMigs.incrementAndGet();
            }

            return new ProcessorResultWrapper<>(alignedConsensus);
        }

        skippedMigs.incrementAndGet();
        return ProcessorResultWrapper.BLANK;
    }

    protected MutationArray extractMutations(AlignmentResult result,
                                             SConsensus consensus) {
        return extractMutations(result, consensus.getConsensusSQPair(), consensus.getMinors());
    }

    protected MutationArray extractMutations(AlignmentResult result,
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

        if (result.isGood()) {
            Set<Integer> minorMutations = mutationsExtractor.recomputeMinorMutations();

            alignerTableByReference.get(reference).append(alignment,
                    consensus.getQuality(),
                    majorMutations, minorMutations);
        }

        return majorMutations;
    }

    public abstract AlignedConsensus align(ConsensusType consensus);

    public int getAlignedMigs() {
        return alignedMigs.get();
    }

    public int getSkippedMigs() {
        return skippedMigs.get();
    }

    public int getGoodAlignmentMigs() {
        return goodAlignmentMigs.get();
    }

    public int getTotalMigs() {
        return totalMigs.get();
    }

    public int getChimericMigs() {
        return chimericMigs.get();
    }

    public ReferenceLibrary getReferenceLibrary() {
        return referenceLibrary;
    }

    public MutationsTable getAlignerTable(Reference reference) {
        return alignerTableByReference.get(reference);
    }

    public ConsensusAlignerParameters getParameters() {
        return parameters;
    }

    public void clear() {
        alignerTableByReference.clear();
        cleared = true;
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
        if (cleared) {
            Speaker.INSTANCE.sout("WARNING: Calling output for Aligner that was cleared", 1);
            return "Was cleared..";
        }

        StringBuilder stringBuilder = new StringBuilder();

        for (Reference reference : referenceLibrary.getReferences()) {
            MutationsTable mutationsTable = alignerTableByReference.get(reference);

            if (mutationsTable.wasUpdated()) {
                for (int i = 0; i < reference.getSequence().size(); i++) {
                    stringBuilder.append(reference.getName()).append("\t").
                            append(i + 1).append("\t").
                            append(mutationsTable.getMigCoverage(i)).append("\t").
                            append(mutationsTable.getCqsSumCoverage(i));

                    StringBuilder major = new StringBuilder(), minor = new StringBuilder();

                    for (byte j = 0; j < 4; j++) {
                        major.append("\t").append(mutationsTable.getMajorMigCount(i, j));
                        minor.append("\t").append(mutationsTable.getMinorMigCount(i, j));
                    }

                    stringBuilder.append(major).append(minor).append("\n");
                }
            }
        }
        return stringBuilder.toString();
    }
}
