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
 * Last modified on 12.3.2015 by mikesh
 */

package com.milaboratory.oncomigec.core.assemble.misc;

import com.milaboratory.core.sequence.quality.QualityFormat;
import com.milaboratory.core.sequencing.io.fastq.PFastqWriter;
import com.milaboratory.core.sequencing.io.fastq.SFastqWriter;
import com.milaboratory.oncomigec.core.PipelineBlockIO;
import com.milaboratory.oncomigec.core.assemble.entity.PConsensus;
import com.milaboratory.oncomigec.core.assemble.entity.SConsensus;
import com.milaboratory.oncomigec.core.assemble.processor.Assembler;

import java.io.IOException;

public class AssemblerIO extends PipelineBlockIO<Assembler> {
    public AssemblerIO() {
        super("assembler", 2);
    }

    @Override
    protected void writeToTabular(Assembler block, String outputPrefix) throws IOException {
        super.writeToTabular(block, outputPrefix);

        if (block.isPairedEnd()) {
            PFastqWriter writer = new PFastqWriter(outputPrefix + "_C1.fastq.gz",
                    outputPrefix + "_C2.fastq.gz", QualityFormat.Phred33);
            for (Object obj : block.getConsensusList()) {
                PConsensus consensus = (PConsensus) obj;
                writer.write(consensus.asRead());
            }
            writer.close();
        } else {
            SFastqWriter writer = new SFastqWriter(outputPrefix + "_C1.fastq.gz",
                    QualityFormat.Phred33);
            for (Object obj : block.getConsensusList()) {
                SConsensus consensus = (SConsensus) obj;
                writer.write(consensus.asRead());
            }
            writer.close();
        }
    }
}
