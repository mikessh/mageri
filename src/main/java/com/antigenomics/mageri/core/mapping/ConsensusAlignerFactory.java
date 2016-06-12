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

package com.antigenomics.mageri.core.mapping;

import com.antigenomics.mageri.core.assemble.Consensus;
import com.antigenomics.mageri.core.mapping.alignment.AlignerFactory;
import com.antigenomics.mageri.core.ReadSpecific;

public abstract class ConsensusAlignerFactory<T extends Consensus, Y extends AlignedConsensus> implements ReadSpecific {
    protected final AlignerFactory alignerFactory;
    protected final ConsensusAlignerParameters parameters;

    protected ConsensusAlignerFactory(AlignerFactory alignerFactory, ConsensusAlignerParameters parameters) {
        this.alignerFactory = alignerFactory;
        this.parameters = parameters;
    }

    public abstract ConsensusAligner<T, Y> create();

    public ConsensusAlignerParameters getParameters() {
        return parameters;
    }
}
