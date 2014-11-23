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
package com.milaboratory.migec2.core.consalign.processor;

import cc.redberry.pipe.Processor;
import com.milaboratory.migec2.core.align.processor.Aligner;
import com.milaboratory.migec2.core.assemble.entity.Consensus;
import com.milaboratory.migec2.core.consalign.entity.AlignedConsensus;
import com.milaboratory.migec2.core.consalign.entity.AlignerReferenceLibrary;
import com.milaboratory.migec2.core.consalign.misc.ConsensusAlignerParameters;
import com.milaboratory.migec2.util.ProcessorResultWrapper;

public abstract class ConsensusAligner<T extends Consensus> implements Processor<ProcessorResultWrapper<T>,
        ProcessorResultWrapper<AlignedConsensus>> {
    protected final Aligner aligner;
    protected final AlignerReferenceLibrary alignerReferenceLibrary;
    protected final ConsensusAlignerParameters parameters;

    protected ConsensusAligner(Aligner aligner, ConsensusAlignerParameters parameters) {
        this.aligner = aligner;
        this.alignerReferenceLibrary = new AlignerReferenceLibrary(aligner.getReferenceLibrary());
        this.parameters = parameters;
    }

    public ProcessorResultWrapper<AlignedConsensus> process(ProcessorResultWrapper<T> consensus) {
        if (consensus.hasResult()) {
            AlignedConsensus alignmentData = align(consensus.getResult());
            if (alignmentData == null)
                return ProcessorResultWrapper.BLANK;
            else
                return new ProcessorResultWrapper<>(alignmentData);
        } else
            return ProcessorResultWrapper.BLANK;
    }

    public abstract AlignedConsensus align(T consensus);

    public AlignerReferenceLibrary getAlignerReferenceLibrary() {
        return alignerReferenceLibrary;
    }
}
