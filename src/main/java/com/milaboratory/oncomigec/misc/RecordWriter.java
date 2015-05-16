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

package com.milaboratory.oncomigec.misc;

import com.milaboratory.oncomigec.core.PipelineBlock;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.pipeline.Platform;
import com.milaboratory.oncomigec.pipeline.analysis.Sample;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public abstract class RecordWriter<RecordType extends Record, BlockType extends PipelineBlock> implements AutoCloseable {
    protected final ReferenceLibrary referenceLibrary;
    protected final Sample sample;
    protected final PrintWriter writer;
    protected final Platform platform;
    protected final BlockType pipelineBlock;

    public RecordWriter(Sample sample, OutputStream outputStream,
                        ReferenceLibrary referenceLibrary,
                        BlockType pipelineBlock,
                        Platform platform) throws IOException {
        this.sample = sample;
        this.writer = new PrintWriter(outputStream);
        this.referenceLibrary = referenceLibrary;
        this.pipelineBlock = pipelineBlock;
        this.platform = platform;

        writer.println(getHeader());
    }

    protected abstract String getHeader();

    public abstract void write(RecordType record) throws IOException;

    public void close() {
        writer.close();
    }

    public ReferenceLibrary getReferenceLibrary() {
        return referenceLibrary;
    }

    public Sample getSample() {
        return sample;
    }

    public BlockType getPipelineBlock() {
        return pipelineBlock;
    }
}
