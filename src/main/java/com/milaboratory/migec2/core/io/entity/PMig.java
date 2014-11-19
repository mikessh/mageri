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
package com.milaboratory.migec2.core.io.entity;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;

public class PMig extends Mig{
    private final SMig mig1, mig2;

    public PMig(SMig mig1, SMig mig2) {
        this.mig1 = mig1;
        this.mig2 = mig2;
        if (!mig1.getUmi().equals(mig2.getUmi()))
            throw new IllegalArgumentException("UMIs of MIGs don't match");
        if (mig1.size() != mig2.size())
            throw new IllegalArgumentException("Size of MIGs with same UMI don't match");
    }

    @Override
    public NucleotideSequence getUmi() {
        return mig1.getUmi();
    }

    @Override
    public int size() {
        return mig1.getReads().size();
    }

    public SMig getMig1() {
        return mig1;
    }

    public SMig getMig2() {
        return mig2;
    }

    @Override
    public String toString() {
        String formattedString = "@" + getUmi().toString() + ":" + size();
        for (int i = 0; i < size(); i++) {
            formattedString += "\n" + mig1.getReads().get(i).getData().getSequence() + "\t" +
                    mig2.getReads().get(i).getData().getSequence() + "\n" +
                    mig1.getReads().get(i).getData().getQuality() + "\t" +
                    mig2.getReads().get(i).getData().getQuality();
        }
        return formattedString;
    }
}
