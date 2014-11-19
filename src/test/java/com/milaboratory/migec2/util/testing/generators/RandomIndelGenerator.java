package com.milaboratory.migec2.util.testing.generators;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.migec2.util.Util;

public class RandomIndelGenerator {
    private final int minIndelSize, maxIndelSize;
    private final boolean insertionMode;

    public static RandomIndelGenerator DEFAULT_INSERTION = new RandomIndelGenerator(5, 30, true),
            DEFAULT_DELETION = new RandomIndelGenerator(5, 30, false);

    public RandomIndelGenerator(int minIndelSize, int maxIndelSize, boolean insertionMode) {
        this.minIndelSize = minIndelSize;
        this.maxIndelSize = maxIndelSize;
        this.insertionMode = insertionMode;
    }


    public NucleotideSequence mutate(NucleotideSequence nucleotideSequence) {
        int len = nucleotideSequence.size();
        int size = Util.randomInRange(minIndelSize, maxIndelSize);
        if (insertionMode) {
            int pos = Util.randomWithBound(len);

            return nucleotideSequence.getRange(0, pos).
                    concatenate(Util.randomSequence(size)).
                    concatenate(nucleotideSequence.getRange(pos, len));
        } else {
            int pos = Util.randomWithBound(len - size);

            return nucleotideSequence.getRange(0, pos).
                    concatenate(nucleotideSequence.getRange(pos + size, len));
        }
    }
}
