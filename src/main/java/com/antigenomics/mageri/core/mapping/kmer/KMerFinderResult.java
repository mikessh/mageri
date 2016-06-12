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

package com.antigenomics.mageri.core.mapping.kmer;

import com.antigenomics.mageri.core.genomic.Reference;

public class KMerFinderResult {
    private final double information;
    private final byte score;
    private final boolean reverseComplement;
    private final Reference hit;

    public KMerFinderResult(double information, byte score,
                            Reference hit, boolean reverseComplement) {
        this.information = information;
        this.score = score;
        this.hit = hit;
        this.reverseComplement = reverseComplement;
    }

    public double getInformation() {
        return information;
    }

    public byte getScore() {
        return score;
    }

    public Reference getHit() {
        return hit;
    }

    public boolean isReverseComplement() {
        return reverseComplement;
    }
}