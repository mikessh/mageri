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

package com.milaboratory.oncomigec.generators;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.input.PMig;
import com.milaboratory.oncomigec.core.input.SMig;
import com.milaboratory.oncomigec.core.input.index.Read;
import com.milaboratory.oncomigec.pipeline.analysis.Sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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