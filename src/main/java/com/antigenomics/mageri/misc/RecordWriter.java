/*
 * Copyright 2014-2016 Mikhail Shugay
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

package com.antigenomics.mageri.misc;

import com.antigenomics.mageri.core.PipelineBlock;
import com.antigenomics.mageri.core.genomic.ReferenceLibrary;
import com.antigenomics.mageri.pipeline.Platform;
import com.antigenomics.mageri.pipeline.analysis.Sample;

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
