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
 * Last modified on 1.5.2015 by mikesh
 */

package com.milaboratory.oncomigec.generators;

import com.milaboratory.core.sequence.SequenceBuilder;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;

import java.util.Random;

public class RandomUtil {
    private static final Random random = new Random(480011L);

    public static int nextFromRange(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    public static int nextIndex(int n) {
        return random.nextInt(n);
    }

    public static NucleotideSequence randomSequence(int length) {
        SequenceBuilder<NucleotideSequence> builder = NucleotideAlphabet.INSTANCE.getBuilderFactory().create(length);
        for (int i = 0; i < length; ++i) {
            builder.setCode(i, (byte) random.nextInt(4));
        }
        return builder.create();
    }
}
