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

package com.milaboratory.migec2.datasim;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.migec2.core.consalign.mutations.NucleotideCoverage;

import java.util.concurrent.atomic.AtomicInteger;

public class MigGeneratorHistoryEntry {
    private final NucleotideSequence sequence;
    private final AtomicInteger frequency;
    private final NucleotideCoverage mutationProfile;

    public MigGeneratorHistoryEntry(NucleotideSequence sequence) {
        this.sequence = sequence;
        this.frequency = new AtomicInteger();
        this.mutationProfile = new NucleotideCoverage(sequence.size());
    }

    public void append(NucleotideSequence variant) {
        frequency.incrementAndGet();
        for (int i = 0; i < variant.size(); i++) {
            byte variantCode = variant.codeAt(i);
            if (variantCode != sequence.codeAt(i))
                mutationProfile.incrementCoverage(i, variantCode);
        }
    }

    public int getFrequency() {
        return frequency.get();
    }

    public int mutationFrequency(int pos, byte code) {
        return mutationProfile.getCoverage(pos, code);
    }

    @Override
    public String toString() {
        return "@F=" + frequency.get() + "\n" + sequence.toString() + "\n+Mutations" + mutationProfile;
    }
}
