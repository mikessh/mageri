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

package com.milaboratory.oncomigec.pipeline.analysis;

import com.milaboratory.oncomigec.core.PipelineBlock;
import com.milaboratory.oncomigec.core.assemble.AssemblerParameters;
import com.milaboratory.oncomigec.core.assemble.PAssemblerFactory;
import com.milaboratory.oncomigec.core.assemble.SAssemblerFactory;
import com.milaboratory.oncomigec.core.assemble.Assembler;
import com.milaboratory.oncomigec.core.input.PreprocessorParameters;

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
