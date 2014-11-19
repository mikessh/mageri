/**
 * Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)
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
 */

package com.milaboratory.migec2.benchmark;

import com.milaboratory.migec2.core.haplotype.Haplotype;
import com.milaboratory.migec2.datasim.MigGeneratorHistory;

import java.util.Collection;

public class BenchmarkStatistics {
    private final int numberOfMigs, numberOfAssembledMigs, numberOfAlignedMigs;
    private final MigGeneratorHistory migGeneratorHistory;
    private final Collection<Haplotype> migecResults;

    public BenchmarkStatistics(int numberOfMigs, int numberOfAssembledMigs, int numberOfAlignedMigs,
                               MigGeneratorHistory migGeneratorHistory, Collection<Haplotype> migecResults) {
        this.numberOfMigs = numberOfMigs;
        this.numberOfAssembledMigs = numberOfAssembledMigs;
        this.numberOfAlignedMigs = numberOfAlignedMigs;
        this.migGeneratorHistory = migGeneratorHistory;
        this.migecResults = migecResults;
    }
}
