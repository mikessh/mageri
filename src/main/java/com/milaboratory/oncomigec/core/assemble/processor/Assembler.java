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
import com.milaboratory.core.sequence.quality.QualityFormat;
import com.milaboratory.core.sequencing.io.fastq.PFastqWriter;
import com.milaboratory.core.sequencing.io.fastq.SFastqWriter;
import com.milaboratory.oncomigec.ReadSpecific;
import com.milaboratory.oncomigec.core.PipelineBlock;
import com.milaboratory.oncomigec.core.assemble.entity.Consensus;
import com.milaboratory.oncomigec.core.io.entity.Mig;
import com.milaboratory.oncomigec.util.FastqWriter;
import com.milaboratory.oncomigec.util.PFastqFastqWriterWrapper;
import com.milaboratory.oncomigec.util.ProcessorResultWrapper;
import com.milaboratory.oncomigec.util.SFastqFastqWriterWrapper;
import com.milaboratory.util.CompressionType;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

// pipeline block - qual histogram
public abstract class Assembler<ConsensusType extends Consensus, MigType extends Mig> extends PipelineBlock
        implements Processor<MigType, ProcessorResultWrapper<ConsensusType>>, ReadSpecific {
    protected final AtomicLong readsTotal = new AtomicLong(), readsAssembled = new AtomicLong();
    protected final AtomicInteger migsTotal = new AtomicInteger(), migsAssembled = new AtomicInteger();
    protected final List<ConsensusType> consensusList = Collections.synchronizedList(new LinkedList<ConsensusType>());
    protected boolean storeConsensuses = true;

    protected Assembler() {
        super("assemble");
    }

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

    public long getReadsTotal() {
        return readsTotal.get();
    }

    public long getReadsAssembled() {
        return readsAssembled.get();
    }

    public long getMigsTotal() {
        return migsTotal.get();
    }

    public long getMigsAssembled() {
        return migsAssembled.get();
    }

    public abstract long getReadsDroppedShortR1();

    public abstract long getReadsDroppedErrorR1();

    public abstract long getReadsDroppedShortR2();

    public abstract long getReadsDroppedErrorR2();

    public List<ConsensusType> getConsensusList() {
        return Collections.synchronizedList(consensusList);
    }

    @Override
    public String getHeader() {
        return "umi\treads.assembled\treads.total\tmin.qual\tavg.qual\tmax.qual";
    }

    @Override
    public String getBody() {
        StringBuilder stringBuilder = new StringBuilder();

        for (Consensus consensus : consensusList)
            stringBuilder.append(consensus.getUmi()).
                    append('\t').append(consensus.size()).
                    append('\t').append(consensus.fullSize()).
                    append('\t').append(consensus.getMinQual()).
                    append('\t').append(consensus.getAvgQual()).
                    append('\t').append(consensus.getMaxQual()).
                    append('\n');

        return stringBuilder.toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writePlainText(String pathPrefix) throws IOException {
        super.writePlainText(pathPrefix);
        FastqWriter writer = isPairedEnd() ?
                new PFastqFastqWriterWrapper(
                        new PFastqWriter(pathPrefix + ".assemble.R1.fastq.gz", pathPrefix + ".assemble.R2.fastq.gz",
                                QualityFormat.Phred33, CompressionType.GZIP)
                ) :
                new SFastqFastqWriterWrapper(
                        new SFastqWriter(pathPrefix + ".assemble.R1.fastq.gz", QualityFormat.Phred33, CompressionType.GZIP)
                );
        for (Consensus consensus : consensusList) {
            writer.write(consensus.asRead());
        }
        writer.close();
    }
}
