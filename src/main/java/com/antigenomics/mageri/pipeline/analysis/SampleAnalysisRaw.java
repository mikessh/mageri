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

package com.antigenomics.mageri.pipeline.analysis;

import cc.redberry.pipe.OutputPort;
import cc.redberry.pipe.blocks.Merger;
import cc.redberry.pipe.blocks.ParallelProcessor;
import cc.redberry.pipe.util.CountingOutputPort;
import com.antigenomics.mageri.core.assemble.Assembler;
import com.antigenomics.mageri.core.assemble.Consensus;
import com.antigenomics.mageri.core.assemble.DummyMinorCaller;
import com.antigenomics.mageri.core.input.MigSizeDistribution;
import com.antigenomics.mageri.core.mapping.AlignedConsensus;
import com.antigenomics.mageri.core.mapping.ConsensusAligner;
import com.antigenomics.mageri.core.variant.VariantCaller;
import com.antigenomics.mageri.misc.ProcessorResultWrapper;

public class SampleAnalysisRaw extends SampleAnalysis {
    protected SampleAnalysisRaw(ProjectAnalysis parent, Sample sample, MigSizeDistribution migSizeDistribution,
                                OutputPort reader, Assembler assembler, ConsensusAligner consensusAligner,
                                boolean isPairedEnd) {
        super(parent, sample, migSizeDistribution, reader, assembler, consensusAligner, isPairedEnd);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() throws Exception {
        if (ran) {
            return;
        }

        String outputPrefix = getOutputPrefix();

        final Merger<ProcessorResultWrapper<Consensus>> input = new Merger<>(524288);
        input.merge(reader);
        input.start();

        final CountingOutputPort<ProcessorResultWrapper<Consensus>> countingInput = new CountingOutputPort<>(input);

        Thread reporter = new Thread(new Runnable() {
            long prevCount = -1;

            @Override
            public void run() {
                try {
                    while (!countingInput.isClosed()) {
                        long count = countingInput.getCount();
                        if (prevCount != count) {
                            sout("Aligning reads, " + count + " processed so far..", 2);
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

        // Align in parallel
        final OutputPort<ProcessorResultWrapper<AlignedConsensus>> alignerResults =
                new ParallelProcessor<>(countingInput, consensusAligner, parent.getRuntimeParameters().getNumberOfThreads());

        ProcessorResultWrapper<AlignedConsensus> alignmentDataWrapped;
        while ((alignmentDataWrapped = alignerResults.take()) != null) {
            if (alignmentDataWrapped.hasResult()) {
                alignmentDataList.add(alignmentDataWrapped.getResult());
            }
        }

        // Write consensus aligner output now, as it will be cleared upon creation of VariantCaller
        if (outputPrefix != null) {
            consensusAligner.writePlainText(outputPrefix);
        }

        sout("Finished, " + countingInput.getCount() + " reads processed in total.", 1);

        sout("Calling variants.", 1);

        this.variantCaller = new VariantCaller(consensusAligner, DummyMinorCaller.INSTANCE,
                parent.getPresets().getVariantCallerParameters());

        if (outputPrefix != null) {
            variantCaller.writePlainText(outputPrefix);
        }

        sout("Finished", 1);

        ran = true;
    }
}
