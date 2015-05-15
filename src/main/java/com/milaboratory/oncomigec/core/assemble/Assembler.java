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

package com.milaboratory.oncomigec.core.assemble;

import cc.redberry.pipe.Processor;
import com.milaboratory.core.sequence.quality.QualityFormat;
import com.milaboratory.core.sequencing.io.fastq.PFastqWriter;
import com.milaboratory.core.sequencing.io.fastq.SFastqWriter;
import com.milaboratory.oncomigec.core.Mig;
import com.milaboratory.oncomigec.core.PipelineBlock;
import com.milaboratory.oncomigec.core.ReadSpecific;
import com.milaboratory.oncomigec.misc.FastqWriter;
import com.milaboratory.oncomigec.misc.PFastqFastqWriterWrapper;
import com.milaboratory.oncomigec.misc.ProcessorResultWrapper;
import com.milaboratory.oncomigec.misc.SFastqFastqWriterWrapper;
import com.milaboratory.oncomigec.pipeline.Speaker;
import com.milaboratory.util.CompressionType;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Assembler<ConsensusType extends Consensus, MigType extends Mig> extends PipelineBlock
        implements Processor<MigType, ProcessorResultWrapper<ConsensusType>>, ReadSpecific {
    protected final AtomicLong readsTotal = new AtomicLong(), readsAssembled = new AtomicLong();
    protected final AtomicInteger migsTotal = new AtomicInteger(), migsAssembled = new AtomicInteger();
    protected final List<ConsensusType> consensusList = Collections.synchronizedList(new LinkedList<ConsensusType>());
    protected boolean storeConsensuses = true, cleared = false;

    protected Assembler() {
        super("assemble");
    }

    @Override
    @SuppressWarnings("unchecked")
    public ProcessorResultWrapper<ConsensusType> process(MigType mig) {
        ConsensusType consensus = assemble(mig);
        if (consensus == null) {
            return ProcessorResultWrapper.BLANK;
        } else {
            return new ProcessorResultWrapper<>(consensus);
        }
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

    public void clear() {
        consensusList.clear();
        cleared = true;
    }

    @Override
    public String getHeader() {
        return "umi\treads.assembled\treads.total";
    }

    @Override
    public String getBody() {
        StringBuilder stringBuilder = new StringBuilder();

        for (Consensus consensus : consensusList)
            stringBuilder.append(consensus.getUmi()).
                    append('\t').append(consensus.getAssembledSize()).
                    append('\t').append(consensus.getTrueSize()).
                    append('\n');

        return stringBuilder.toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writePlainText(String pathPrefix) throws IOException {
        super.writePlainText(pathPrefix);

        if (cleared) {
            Speaker.INSTANCE.sout("WARNING: Calling output for Assembler that was cleared", 1);
            return;
        }

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
