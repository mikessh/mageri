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

import cc.redberry.pipe.OutputPort;
import cc.redberry.pipe.blocks.Merger;
import cc.redberry.pipe.blocks.ParallelProcessor;
import cc.redberry.pipe.util.CountingOutputPort;
import com.milaboratory.oncomigec.core.Mig;
import com.milaboratory.oncomigec.core.ReadSpecific;
import com.milaboratory.oncomigec.core.assemble.Assembler;
import com.milaboratory.oncomigec.core.assemble.Consensus;
import com.milaboratory.oncomigec.core.input.MigOutputPort;
import com.milaboratory.oncomigec.core.input.MigSizeDistribution;
import com.milaboratory.oncomigec.core.mapping.AlignedConsensus;
import com.milaboratory.oncomigec.core.mapping.ConsensusAligner;
import com.milaboratory.oncomigec.core.variant.VariantCaller;
import com.milaboratory.oncomigec.misc.ProcessorResultWrapper;
import com.milaboratory.oncomigec.pipeline.Speaker;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class SampleAnalysis implements ReadSpecific, Serializable {
    protected final boolean paired;

    protected final ProjectAnalysis parent;
    protected final Sample sample;

    protected transient final MigOutputPort reader;

    protected final MigSizeDistribution migSizeDistribution;
    protected final Assembler assembler;
    protected final ConsensusAligner consensusAligner;
    protected VariantCaller variantCaller;

    protected boolean ran = false;

    private final List<AlignedConsensus> alignmentDataList = new LinkedList<>();

    @SuppressWarnings("unchecked")
    protected SampleAnalysis(ProjectAnalysis parent,
                             Sample sample,
                             MigSizeDistribution migSizeDistribution,
                             MigOutputPort reader,
                             Assembler assembler,
                             ConsensusAligner consensusAligner) {
        this.parent = parent;
        this.migSizeDistribution = migSizeDistribution;
        this.sample = sample;
        this.reader = reader;
        this.paired = reader.isPairedEnd();

        if (assembler.isPairedEnd() != paired ||
                consensusAligner.isPairedEnd() != paired)
            throw new RuntimeException("All read-specific pipeline steps should have the same paired-end property.");

        this.assembler = assembler;
        this.consensusAligner = consensusAligner;
    }

    private void sout(String message, int verbosityLevel) {
        Speaker.INSTANCE.sout("[" + sample.getFullName() + "] " +
                message, verbosityLevel);
    }

    @SuppressWarnings("unchecked")
    public void run() throws Exception {
        if (ran) {
            return;
        }

        String outputPrefix = getOutputPrefix();

        OutputPort<Mig> input = reader;

        final Merger<Mig> bufferedInput = new Merger<>(524288);
        bufferedInput.merge(input);
        bufferedInput.start();
        input = bufferedInput;

        final CountingOutputPort<Mig> countingInput = new CountingOutputPort<>(input);

        Thread reporter = new Thread(new Runnable() {
            long prevCount = -1;

            @Override
            public void run() {
                try {
                    while (!countingInput.isClosed()) {
                        long count = countingInput.getCount();
                        if (prevCount != count) {
                            sout("Assembling & aligning consensuses, " + count + " MIGs processed..", 2);
                            prevCount = count;
                        }
                        Thread.sleep(10000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        reporter.setDaemon(true);
        reporter.start();

        // Assemble & align in parallel
        final OutputPort<ProcessorResultWrapper<Consensus>> assemblyResults =
                new ParallelProcessor<>(countingInput, assembler, parent.getRuntimeParameters().getNumberOfThreads());

        final OutputPort<ProcessorResultWrapper<AlignedConsensus>> alignerResults =
                new ParallelProcessor<>(assemblyResults, consensusAligner, parent.getRuntimeParameters().getNumberOfThreads());

        ProcessorResultWrapper<AlignedConsensus> alignmentDataWrapped;
        while ((alignmentDataWrapped = alignerResults.take()) != null) {
            if (alignmentDataWrapped.hasResult()) {
                alignmentDataList.add(alignmentDataWrapped.getResult());
            }
        }

        // Write plain-text and consensus FASTQ files
        // Write consensus aligner output now, as it will be cleared upon creation of VariantCaller
        if (outputPrefix != null) {
            migSizeDistribution.writePlainText(outputPrefix);
            assembler.writePlainText(outputPrefix);
            consensusAligner.writePlainText(outputPrefix);
        }

        assembler.clear();

        sout("Finished, " + countingInput.getCount() + " MIGs processed in total.", 1);

        sout("Calling variants.", 1);

        this.variantCaller = new VariantCaller(consensusAligner,
                parent.getPresets().getVariantCallerParameters());

        if (outputPrefix != null) {
            variantCaller.writePlainText(outputPrefix);
        }

        sout("Finished", 1);

        ran = true;
    }

    protected String getOutputPrefix() {
        String outputPath = parent.outputPath;

        if (outputPath == null) {
            return null;
        } else {
            return outputPath + sample.getFullName();
        }
    }

    public MigOutputPort getReader() {
        return reader;
    }

    public MigSizeDistribution getMigSizeDistribution() {
        return migSizeDistribution;
    }

    public Assembler getAssembler() {
        return assembler;
    }

    public ConsensusAligner getConsensusAligner() {
        return consensusAligner;
    }

    public VariantCaller getVariantCaller() {
        return variantCaller;
    }

    public List<AlignedConsensus> getAlignmentDataList() {
        return alignmentDataList;
    }

    public Sample getSample() {
        return sample;
    }

    public ProjectAnalysis getParent() {
        return parent;
    }

    public boolean wasRan() {
        return ran;
    }

    @Override
    public boolean isPairedEnd() {
        return paired;
    }
}
