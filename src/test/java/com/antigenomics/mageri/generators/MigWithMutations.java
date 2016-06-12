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

package com.antigenomics.mageri.generators;

import com.antigenomics.mageri.core.input.index.Read;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.antigenomics.mageri.core.input.PMig;
import com.antigenomics.mageri.core.input.SMig;
import com.antigenomics.mageri.pipeline.analysis.Sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MigWithMutations {
    private final SMig mig;
    private final int[] majorMutations;
    private final NucleotideSequence consensus;
    private final Map<Integer, Integer> minorMutationCounts;

    public MigWithMutations(NucleotideSequence consensus,
                            SMig mig, Map<Integer, Integer> minorMutationCounts,
                            int[] majorMutations) {
        this.consensus = consensus;
        this.mig = mig;
        this.majorMutations = majorMutations;
        this.minorMutationCounts = minorMutationCounts;
    }

    public NucleotideSequence getConsensus() {
        return consensus;
    }

    public SMig getSMig() {
        return mig;
    }

    public PMig getPMig() {
        return getPMig(-5, 5);
    }

    public PMig getPMig(int overlapHalfSzMin, int overlapHalfSzMax) {
        List<Read> reads1 = new ArrayList<>(), reads2 = new ArrayList<>();

        int overlap = RandomUtil.nextFromRange(overlapHalfSzMin, overlapHalfSzMax);

        for (Read read : mig.getReads()) {
            int mid = read.getSequence().size() / 2;

            Read read1 = read.region(0, mid + overlap),
                    read2 = read.region(mid - overlap, read.getSequence().size());

            reads1.add(read1);
            reads2.add(read2);
        }

        Sample sample = Sample.create("dummy", true);

        return new PMig(new SMig(sample, mig.getUmi(), reads1),
                new SMig(sample, mig.getUmi(), reads2));
    }

    public Map<Integer, Integer> getMinorMutationCounts() {
        return minorMutationCounts;
    }

    public int[] getMajorMutations() {
        return majorMutations;
    }
}