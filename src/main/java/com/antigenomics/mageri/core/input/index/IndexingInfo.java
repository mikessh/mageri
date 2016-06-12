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

package com.antigenomics.mageri.core.input.index;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;

public class IndexingInfo {
    private final ReadInfo readInfo;
    private final String sampleName; // TODO: IMPORTANT, change from string to ref or int
    private final NucleotideSequence umi;

    public IndexingInfo(ReadInfo readInfo, String sampleName, NucleotideSequence umi) {
        this.readInfo = readInfo;
        this.sampleName = sampleName;
        this.umi = umi;
    }

    public ReadInfo getReadInfo() {
        return readInfo;
    }

    public String getSampleName() {
        return sampleName;
    }

    public NucleotideSequence getUmi() {
        return umi;
    }
}
