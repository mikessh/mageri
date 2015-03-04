/*
 * Copyright 2013-2015 Mikhail Shugay (mikhail.shugay@gmail.com)
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
 *
 * Last modified on 20.2.2015 by mikesh
 */

package com.milaboratory.oncomigec.core.io.misc;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequencing.read.SequencingRead;

public class MigecReadPair implements MigecRead {
    private final NucleotideSQPair first, second;

    public MigecReadPair(SequencingRead sequencingRead) {
        this(sequencingRead.getData(0), sequencingRead.getData(1));
    }

    public MigecReadPair(NucleotideSQPair first, NucleotideSQPair second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public NucleotideSQPair first() {
        return first;
    }

    @Override
    public NucleotideSQPair second() {
        return second;
    }
}
