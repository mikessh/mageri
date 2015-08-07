/*
 * Copyright (c) 2014-2015, Bolotin Dmitry, Chudakov Dmitry, Shugay Mikhail
 * (here and after addressed as Inventors)
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact the Inventors using one of the following
 * email addresses: chudakovdm@mail.ru, chudakovdm@gmail.com
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */

package com.milaboratory.mageri.core.input.index;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequenceBuilder;
import com.milaboratory.core.sequence.quality.SequenceQualityPhred;
import com.milaboratory.core.sequence.quality.SequenceQualityUtils;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Random;

public class Read implements Serializable {
    private static final Random rnd = new Random(511022);
    private final NucleotideSequence sequence;
    private final BitSet qualityMask;

    public Read(NucleotideSequence sequence,
                BitSet qualityMask) {
        this.sequence = sequence;
        this.qualityMask = qualityMask;
    }

    public Read(NucleotideSQPair nucleotideSQPair) {
        this(nucleotideSQPair, QualityProvider.DEFAULT);
    }

    public Read(NucleotideSQPair nucleotideSQPair,
                QualityProvider qualityProvider) {
        NucleotideSequence sequence = nucleotideSQPair.getSequence();

        SequenceQualityPhred qual = nucleotideSQPair.getQuality();

        // This is required as old milib reader replaces N's with A.
        // It also sets quality to BAD_QUALITY_VALUE, so here we
        // generate a random base at those positions
        NucleotideSequenceBuilder nsb = new NucleotideSequenceBuilder(sequence.size());

        for (int i = 0; i < sequence.size(); i++) {
            if (qual.value(i) > SequenceQualityUtils.BAD_QUALITY_VALUE) {
                nsb.setCode(i, sequence.codeAt(i));
            } else {
                nsb.setCode(i, (byte) rnd.nextInt(4));
            }
        }

        this.sequence = nsb.create();
        this.qualityMask = qualityProvider.convert(nucleotideSQPair.getQuality());
    }

    public Read rc() {
        BitSet qualityMask = new BitSet(length());
        for (int i = 0; i < length(); i++) {
            qualityMask.set(i, this.qualityMask.get(length() - i - 1));
        }
        return new Read(sequence.getReverseComplement(), qualityMask);
    }

    public Read trim5Prime(int from) {
        return region(from, length());
    }

    public Read trim3Prime(int to) {
        return region(0, to);
    }

    public Read region(int from, int to) {
        return new Read(sequence.getRange(from, to),
                qualityMask.get(from, to));
    }

    public NucleotideSequence getSequence() {
        return sequence;
    }

    public boolean goodQuality(int pos) {
        return !qualityMask.get(pos);
    }

    public int length() {
        return sequence.size();
    }
}
