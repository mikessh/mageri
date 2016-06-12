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

import com.milaboratory.core.sequencing.io.fastq.PFastqWriter;
import com.milaboratory.core.sequencing.read.PSequencingRead;

import java.io.IOException;

public class PFastqWriterWrapper implements FastqWriter<PSequencingRead> {
    private final PFastqWriter writer;


    public PFastqWriterWrapper(PFastqWriter writer) {
        this.writer = writer;
    }


    @Override
    public void write(PSequencingRead read) throws IOException {
        writer.write(read);
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
