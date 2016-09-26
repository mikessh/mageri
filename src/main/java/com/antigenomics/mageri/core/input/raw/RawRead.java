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

package com.antigenomics.mageri.core.input.raw;

import com.antigenomics.mageri.core.input.index.Read;
import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class RawRead implements Read {
    private final NucleotideSQPair nucleotideSQPair;

    public RawRead(NucleotideSQPair nucleotideSQPair) {
        this.nucleotideSQPair = nucleotideSQPair;
    }

    @Override
    public Read rc() {
        return new RawRead(nucleotideSQPair.getRC());
    }

    @Override
    public Read trim5Prime(int from) {
        return region(from, length());
    }

    @Override
    public Read trim3Prime(int to) {
        return region(0, to);
    }

    @Override
    public Read region(int from, int to) {
        return new RawRead(nucleotideSQPair.getRange(from, to));
    }

    public NucleotideSequence getSequence() {
        return nucleotideSQPair.getSequence();
    }

    @Override
    public NucleotideSQPair toNucleotideSQPair() {
        return nucleotideSQPair;
    }

    public boolean goodQuality(int pos) {
        throw new NotImplementedException();
    }

    public int length() {
        return nucleotideSQPair.size();
    }
}
