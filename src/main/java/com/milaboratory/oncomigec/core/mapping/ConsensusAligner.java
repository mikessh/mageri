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
        String header = "reference\tpos\tcoverage",
                major = "", minor = "", cqs = "";

        for (byte i = 0; i < 4; i++) {
            char symbol = NucleotideAlphabet.INSTANCE.symbolFromCode(i);
            major += "\t" + symbol + ".major";
            minor += "\t" + symbol + ".minor";
            cqs += "\t" + symbol + ".cqs";
        }

        return header + major + minor + cqs;
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
                            append(mutationsTable.getMigCoverage(i));

                    StringBuilder major = new StringBuilder(), minor = new StringBuilder(),
                            cqs = new StringBuilder();

                    for (byte j = 0; j < 4; j++) {
                        major.append("\t").append(mutationsTable.getMajorMigCount(i, j));
                        minor.append("\t").append(mutationsTable.getMinorMigCount(i, j));
                        cqs.append("\t").append(mutationsTable.getMeanCqs(i, j));
                    }

                    stringBuilder.append(major).append(minor).append(cqs).append("\n");
                }
            }
        }
        return stringBuilder.toString();
    }
}
