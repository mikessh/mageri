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
package com.milaboratory.oncomigec.core.assemble.processor;

import cc.redberry.pipe.Processor;
import com.milaboratory.oncomigec.core.ReadSpecific;
import com.milaboratory.oncomigec.core.assemble.entity.Consensus;
import com.milaboratory.oncomigec.core.io.entity.Mig;
import com.milaboratory.oncomigec.util.ProcessorResultWrapper;
import com.milaboratory.oncomigec.util.QualityHistogram;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Assembler<ConsensusType extends Consensus, MigType extends Mig> implements Processor<MigType, ProcessorResultWrapper<ConsensusType>>, ReadSpecific {
    protected final AtomicLong readsTotal = new AtomicLong(), readsAssembled = new AtomicLong();
    protected final AtomicInteger migsTotal = new AtomicInteger(), migsAssembled = new AtomicInteger();
    protected final List<ConsensusType> consensusList = Collections.synchronizedList(new LinkedList<ConsensusType>());
    protected boolean storeConsensuses = true;

    @Override
    @SuppressWarnings("unchecked")
    public ProcessorResultWrapper<ConsensusType> process(MigType mig) {
        ConsensusType consensus = assemble(mig);
        if (consensus == null)
            return ProcessorResultWrapper.BLANK;
        else
            return new ProcessorResultWrapper<>(consensus);
    }

    public abstract ConsensusType assemble(MigType mig);

    public abstract long getReadsDroppedShortR1();

    public abstract long getReadsDroppedErrorR1();

    public abstract long getReadsDroppedShortR2();

    public abstract long getReadsDroppedErrorR2();

    public abstract String formattedSequenceHeader();

    public List<ConsensusType> getConsensusList() {
        return Collections.synchronizedList(consensusList);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("#").append(super.toString()).
                append("\nreadsTotal\t").append(readsTotal.get()).
                append("\nreadsAssembled\t").append(readsAssembled.get()).
                append("\nreadsDroppedShortR1\t").append(getReadsDroppedShortR1()).
                append("\nreadsDroppedErrorR1\t").append(getReadsDroppedErrorR1()).
                append("\nreadsDroppedShortR2\t").append(getReadsDroppedShortR2()).
                append("\nreadsDroppedErrorR2\t").append(getReadsDroppedErrorR2()).
                append("\nmigsTotal\t").append(migsTotal.get()).
                append("\nmigsAssembled\t").append(migsAssembled.get());

        // Quality histogram
        QualityHistogram qualityHistogram = new QualityHistogram();
        for (Consensus consensus : consensusList)
            qualityHistogram.append(consensus.getQualityHistogram());

        sb.append("\nqualityHistogram:\n").append(qualityHistogram.toString());

        sb.append("\nconsensuses:");

        // Consensus list

        sb.append("\nUMI\tAssembledReads\tTotalReads\t").append(formattedSequenceHeader());
        for (Consensus consensus : consensusList)
            sb.append('\n').append(consensus.getUmi()).
                    append('\t').append(consensus.size()).
                    append('\t').append(consensus.fullSize()).
                    append('\t').append(consensus.formattedSequence());

        return sb.toString();
    }
}
