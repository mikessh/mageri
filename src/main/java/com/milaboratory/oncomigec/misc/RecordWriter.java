/*
 * Copyright 2013-2015 Mikhail Shugay (mikhail.shugay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last modified on 20.4.2015 by mikesh
 */

package com.milaboratory.oncomigec.misc;

import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.pipeline.analysis.Sample;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class RecordWriter<RecordType extends Record> implements AutoCloseable {
    protected final ReferenceLibrary referenceLibrary;
    protected final Sample sample;
    protected final PrintWriter writer;

    public RecordWriter(Sample sample, File outputFile, ReferenceLibrary referenceLibrary) throws IOException {
        this.sample = sample;
        this.writer = new PrintWriter(outputFile);
        this.referenceLibrary = referenceLibrary;
        
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
}
