/*
 * Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)
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
package com.milaboratory.migec2.core.haplotype;

public final class HaplotypeCounters {
    private int count = 0, readCount = 0;
    private double pValue = 0;

    public HaplotypeCounters() {

    }

    public void incrementCount() {
        count++;
    }

    public void incrementReadCount(int migSize) {
        readCount += migSize;
    }

    public int getCount() {
        return count;
    }

    public int getReadCount() {
        return readCount;
    }

    public void updatepValue(double pValue) {
        this.pValue = Math.max(this.pValue, pValue);
    }

    public double getpValue() {
        return pValue;
    }
}
