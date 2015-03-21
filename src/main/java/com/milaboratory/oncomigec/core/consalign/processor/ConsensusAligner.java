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
import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import com.milaboratory.oncomigec.ReadSpecific;
import com.milaboratory.oncomigec.core.PipelineBlock;
import com.milaboratory.oncomigec.core.align.entity.SAlignmentResult;
import com.milaboratory.oncomigec.core.align.processor.Aligner;
import com.milaboratory.oncomigec.core.assemble.entity.Consensus;
import com.milaboratory.oncomigec.core.assemble.entity.SConsensus;
import com.milaboratory.oncomigec.core.consalign.entity.AlignedConsensus;
import com.milaboratory.oncomigec.core.consalign.entity.AlignerReferenceLibrary;
import com.milaboratory.oncomigec.core.consalign.misc.ConsensusAlignerParameters;
import com.milaboratory.oncomigec.core.consalign.mutations.MinorMutationData;
import com.milaboratory.oncomigec.core.consalign.mutations.MutationsAndCoverage;
import com.milaboratory.oncomigec.core.consalign.mutations.MutationsExtractor;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.mutations.MigecMutationsCollection;
import com.milaboratory.oncomigec.util.ProcessorResultWrapper;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class ConsensusAligner<ConsensusType extends Consensus> extends PipelineBlock
        implements Processor<ProcessorResultWrapper<ConsensusType>, ProcessorResultWrapper<AlignedConsensus>>,
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
    public ProcessorResultWrapper<AlignedConsensus> process(ProcessorResultWrapper<ConsensusType> assemblerResult) {
        if (assemblerResult.hasResult()) {
            ConsensusType consensus = assemblerResult.getResult();
            
            AlignedConsensus alignmentData = align(consensus);

            consensus.empty();

            if (alignmentData == null) {
                badMigs.incrementAndGet();
                return ProcessorResultWrapper.BLANK;
            } else {
                alignedMigs.incrementAndGet();
                return new ProcessorResultWrapper<>(alignmentData);
            }
        }

        skippedMigs.incrementAndGet();
        return ProcessorResultWrapper.BLANK;
    }

    protected MigecMutationsCollection update(SAlignmentResult result,
                                              SConsensus consensus) {
        return update(result, consensus, consensus.getConsensusSQPair(), true);
    }

    protected MigecMutationsCollection update(SAlignmentResult result,
                                              SConsensus consensus,
                                              NucleotideSQPair trimmedConsensus,
                                              boolean appendReference) {
        Reference reference = result.getReference();
        LocalAlignment alignment = result.getAlignment();

        MutationsExtractor mutationsExtractor = new MutationsExtractor(alignment,
                reference, consensus, trimmedConsensus, parameters);

        MigecMutationsCollection majorMutations = mutationsExtractor.calculateMajorMutations();
        MinorMutationData minorMutations = mutationsExtractor.calculateMinorMutations();

        alignerReferenceLibrary.append(reference, alignment, trimmedConsensus,
                majorMutations, minorMutations,
                appendReference);

        return majorMutations;
    }

    public abstract AlignedConsensus align(ConsensusType consensus);

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
        String header = "reference\tpos",
                mig = "", corrected = "", lost = "", gained = "", raw = "";

        for (byte i = 0; i < 4; i++) {
            char symbol = NucleotideAlphabet.INSTANCE.symbolFromCode(i);
            mig += "\t" + symbol + ".mig";
            corrected += "\t" + symbol + ".read.corrected";
            lost += "\t" + symbol + ".read.lost";
            gained += "\t" + symbol + ".read.gained";
            raw += "\t" + symbol + ".read.raw";
        }

        return header + mig + corrected + lost + gained + raw;
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

                    StringBuilder mig = new StringBuilder(),
                            corrected = new StringBuilder(),
                            lost = new StringBuilder(),
                            gained = new StringBuilder(),
                            raw = new StringBuilder();

                    for (byte j = 0; j < 4; j++) {
                        int migCount = mutationsAndCoverage.getMajorNucleotideMigCount(i, j),
                                correctedCount = mutationsAndCoverage.getMajorNucleotideReadCount(i, j),
                                lostCount = mutationsAndCoverage.getMinorNucleotideReadCount(i, j),
                                gainedCount = mutationsAndCoverage.getGainedNucleotideReadCount(i, j),
                                rawCount = correctedCount - gainedCount + lostCount;


                        mig.append("\t").append(migCount);
                        corrected.append("\t").append(correctedCount);
                        lost.append("\t").append(lostCount);
                        gained.append("\t").append(gainedCount);
                        raw.append("\t").append(rawCount);
                    }
                    stringBuilder.append(mig).
                            append(corrected).
                            append(lost).
                            append(gained).
                            append(raw).
                            append("\n");
                }
            }
        }
        return stringBuilder.toString();
    }
}
