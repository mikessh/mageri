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

package com.antigenomics.mageri.core.mapping.alignment;

import com.antigenomics.mageri.core.genomic.ReferenceLibrary;
import com.antigenomics.mageri.core.mapping.ConsensusAlignerParameters;
import com.antigenomics.mageri.core.mapping.kmer.KMerFinder;

public class ExtendedKmerAlignerFactory implements AlignerFactory<ExtendedKmerAligner> {
    private final ConsensusAlignerParameters alignerParameters;
    private final KMerFinder kMerFinder;

    public ExtendedKmerAlignerFactory(ReferenceLibrary referenceLibrary) {
        this(referenceLibrary, ConsensusAlignerParameters.DEFAULT);
    }

    public ExtendedKmerAlignerFactory(ReferenceLibrary referenceLibrary, ConsensusAlignerParameters alignerParameters) {
        this.kMerFinder = new KMerFinder(referenceLibrary, alignerParameters);
        this.alignerParameters = alignerParameters;
    }

    @Override
    public ReferenceLibrary getReferenceLibrary() {
        return kMerFinder.getReferenceLibrary();
    }

    @Override
    public ExtendedKmerAligner create() {
        return new ExtendedKmerAligner(kMerFinder, alignerParameters);
    }

    public ConsensusAlignerParameters getAlignerParameters() {
        return alignerParameters;
    }
}
