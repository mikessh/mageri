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

package com.antigenomics.mageri.core.assemble;

import com.antigenomics.mageri.core.input.PMig;
import com.antigenomics.mageri.core.input.PreprocessorParameters;

public final class PAssembler extends Assembler<PConsensus, PMig> {
    private final SAssembler assembler1, assembler2;

    public PAssembler() {
        this(new SAssembler(), new SAssembler());
    }

    public PAssembler(PreprocessorParameters preprocessorParameters,
                      AssemblerParameters assemblerParameters) {
        this.assembler1 = new SAssembler(preprocessorParameters, assemblerParameters);
        this.assembler2 = new SAssembler(preprocessorParameters, assemblerParameters);
        assembler1.storeConsensuses = false;
        assembler2.storeConsensuses = false;
    }

    public PAssembler(SAssembler assembler1, SAssembler assembler2) {
        assembler1.storeConsensuses = false;
        assembler2.storeConsensuses = false;
        this.assembler1 = assembler1;
        this.assembler2 = assembler2;
    }

    @Override
    public PConsensus assemble(PMig pMig) {
        SConsensus result1 = assembler1.assemble(pMig.getMig1()),
                result2 = assembler2.assemble(pMig.getMig2());

        readsTotal.addAndGet(pMig.size());
        migsTotal.incrementAndGet();
        if (result1 == null || result2 == null) {
            //System.out.println(pMig.getMig1().getReads().get(0).getData().getSequence().toString() + "\t" +
            //        pMig.getMig2().getReads().get(0).getData().getSequence().toString());
            return null;
        } else {
            PConsensus consensus = new PConsensus(result1, result2);
            readsAssembled.addAndGet(consensus.getAssembledSize());
            migsAssembled.incrementAndGet();
            consensusList.add(consensus);
            return consensus;
        }
    }

    @Override
    public long getReadsRescuedR1() {
        return assembler1.getReadsRescuedR1();
    }

    @Override
    public long getReadsDroppedShortR1() {
        return assembler1.getReadsDroppedShortR1();
    }

    @Override
    public long getReadsDroppedErrorR1() {
        return assembler1.getReadsDroppedErrorR1();
    }

    @Override
    public long getReadsRescuedR2() {
        return assembler1.getReadsRescuedR2();
    }

    @Override
    public long getReadsDroppedShortR2() {
        return assembler2.getReadsDroppedShortR1();
    }

    @Override
    public long getReadsDroppedErrorR2() {
        return assembler2.getReadsDroppedErrorR1();
    }

    @Override
    public MinorCaller getMinorCaller() {
        return assembler1.getMinorCaller().combine(assembler2.getMinorCaller());
    }

    @Override
    public boolean isPairedEnd() {
        return true;
    }
}
