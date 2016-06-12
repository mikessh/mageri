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

package com.antigenomics.mageri.pipeline.analysis;

import com.antigenomics.mageri.core.PipelineBlock;
import com.antigenomics.mageri.core.assemble.Assembler;
import com.antigenomics.mageri.core.assemble.AssemblerParameters;
import com.antigenomics.mageri.core.assemble.PAssemblerFactory;
import com.antigenomics.mageri.core.assemble.SAssemblerFactory;
import com.antigenomics.mageri.core.input.PreprocessorParameters;

import java.util.HashMap;
import java.util.Map;

public class PipelineAssemblerFactory extends PipelineBlock {
    private final Map<Sample, Assembler> assemblersBySample = new HashMap<>();
    private final PAssemblerFactory pairedFactory;
    private final SAssemblerFactory singleFactory;

    public PipelineAssemblerFactory(PreprocessorParameters preprocessorParameters,
                                    AssemblerParameters assemblerParameters) {
        super("assemble");
        this.pairedFactory = new PAssemblerFactory(preprocessorParameters, assemblerParameters);
        this.singleFactory = new SAssemblerFactory(preprocessorParameters, assemblerParameters);
    }

    public Assembler create(Sample sample) {
        Assembler assembler = sample.isPairedEnd() ? pairedFactory.create() : singleFactory.create();
        assemblersBySample.put(sample, assembler);
        return assembler;
    }

    @Override
    public String getHeader() {
        return "sample.group\tsample\t" +
                "migs.total\tmigs.assembled\t" +
                "reads.total\treads.assembled\t" +
                "reads.short.r1\treads.short.r2\treads.mismatch.r1\treads.mismatch.r2";
    }

    @Override
    public String getBody() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Sample sample : assemblersBySample.keySet()) {
            Assembler assembler = assemblersBySample.get(sample);
            stringBuilder.append(sample.getParent().getName()).append("\t").
                    append(sample.getName()).append("\t").
                    append(assembler.getMigsTotal()).append("\t").
                    append(assembler.getMigsAssembled()).append("\t").
                    append(assembler.getReadsTotal()).append("\t").
                    append(assembler.getReadsAssembled()).append("\t").
                    append(assembler.getReadsDroppedShortR1()).append("\t").
                    append(assembler.getReadsDroppedShortR2()).append("\t").
                    append(assembler.getReadsDroppedErrorR1()).append("\t").
                    append(assembler.getReadsDroppedErrorR2()).append("\n");
        }
        return stringBuilder.toString();
    }
}
