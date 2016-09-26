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

import com.antigenomics.mageri.core.input.raw.SomewhatRawReadProperlyWrapped;

import java.util.HashSet;

public class DummyPAssembler extends Assembler<PConsensus, SomewhatRawReadProperlyWrapped> {
    public DummyPAssembler() {
        storeConsensuses = false;
    }

    @Override
    public PConsensus assemble(SomewhatRawReadProperlyWrapped mig) {
        readsTotal.incrementAndGet();
        migsTotal.incrementAndGet();
        readsAssembled.incrementAndGet();
        migsAssembled.incrementAndGet();

        return new PConsensus(
                new SConsensus(mig.getSample(), mig.getUmi(),
                        mig.getReadContainer().getFirst().toNucleotideSQPair(), new HashSet<Integer>(), 1, 1),
                new SConsensus(mig.getSample(), mig.getUmi(),
                        mig.getReadContainer().getSecond().toNucleotideSQPair(), new HashSet<Integer>(), 1, 1));
    }

    @Override
    public long getReadsRescuedR1() {
        return 0;
    }

    @Override
    public long getReadsRescuedR2() {
        return 0;
    }

    @Override
    public long getReadsDroppedShortR1() {
        return 0;
    }

    @Override
    public long getReadsDroppedErrorR1() {
        return 0;
    }

    @Override
    public long getReadsDroppedShortR2() {
        return 0;
    }

    @Override
    public long getReadsDroppedErrorR2() {
        return 0;
    }

    @Override
    public boolean isPairedEnd() {
        return true;
    }
}
