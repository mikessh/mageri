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

import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.core.input.SMig;
import com.milaboratory.oncomigec.core.input.index.Read;
import com.milaboratory.oncomigec.pipeline.analysis.Sample;

import java.util.*;

import static com.milaboratory.oncomigec.generators.RandomUtil.randomSequence;

public class RandomMigGenerator {
    private static final Random rnd = new Random(123456);
    private boolean maskMinorSubstitutions = false;
    private int migSizeMin = 5, migSizeMax = 100;
    private int umiSize = 12;
    private int maxRandomFlankSize = 0;
    private MutationGenerator mutationGenerator = MutationGenerator.DEFAULT,
            majorMutationGenerator = MutationGenerator.DEFAULT.multiply(0.1);

    public RandomMigGenerator() {

    }

    public MigWithMutations nextMig(RandomReferenceGenerator referenceGenerator) {
        return nextMig(referenceGenerator.nextSequence());
    }

    public MigWithMutations nextMig(RandomReferenceGenerator referenceGenerator, ReferenceLibrary referenceLibrary) {
        return nextMig(referenceGenerator.nextReference(referenceLibrary));
    }

    public MigWithMutations nextMigWithMajorMutations(Reference reference) {
        return nextMigWithMajorMutations(reference.getSequence());
    }

    public MigWithMutations nextMigWithMajorMutations(NucleotideSequence reference) {
        int[] majorMutations = majorMutationGenerator.nextMutations(reference);
        return nextMig(reference, majorMutations);
    }

    public MigWithMutations nextMig(Reference reference) {
        return nextMig(reference.getSequence());
    }

    public MigWithMutations nextMig(NucleotideSequence sequence) {
        return nextMig(sequence, new int[0]);
    }

    private MigWithMutations nextMig(NucleotideSequence sequence, int[] majorMutations) {
        sequence = Mutations.mutate(sequence, majorMutations);

        List<Read> reads = new ArrayList<>();
        int migSize = RandomUtil.nextFromRange(migSizeMin, migSizeMax);

        Map<Integer, Integer> minorMutationCounts = new HashMap<>();
        for (int j = 0; j < migSize; j++) {

            int[] mutations = mutationGenerator.nextMutations(sequence);
            Mutations.shiftIndelsAtHomopolymers(sequence, mutations);

            for (int code : mutations) {
                Integer count = minorMutationCounts.get(code);
                minorMutationCounts.put(code, count == null ? 1 : (count + 1));
            }

            NucleotideSequence sequence2 = Mutations.mutate(sequence,
                    mutations);

            int offset5 = rnd.nextInt(maxRandomFlankSize + 1),
                    offset3 = rnd.nextInt(maxRandomFlankSize + 1);

            NucleotideSequence flank5 = randomSequence(offset5),
                    flank3 = randomSequence(offset3),
                    sequence3 = flank5.concatenate(sequence2.concatenate(flank3));

            BitSet qualMask = new BitSet(sequence3.size());

            if (maskMinorSubstitutions) {
                for (int code : mutations) {
                    if (Mutations.isSubstitution(code)) {
                        qualMask.set(offset5 + Mutations.convertPosition(mutations, Mutations.getPosition(code)));
                    }
                }
            }

            reads.add(new Read(sequence3, qualMask));
        }

        SMig sMig = new SMig(Sample.create("dummy", false), randomSequence(umiSize), reads);

        return new MigWithMutations(sequence, sMig, minorMutationCounts, majorMutations);
    }

    public int getMigSizeMin() {
        return migSizeMin;
    }

    public int getMigSizeMax() {
        return migSizeMax;
    }

    public void setMigSizeMin(int migSizeMin) {
        this.migSizeMin = migSizeMin;
    }

    public void setMigSizeMax(int migSizeMax) {
        this.migSizeMax = migSizeMax;
    }

    public boolean getMaskMinorSubstitutions() {
        return maskMinorSubstitutions;
    }

    public void setMaskMinorSubstitutions(boolean maskMinorSubstitutions) {
        this.maskMinorSubstitutions = maskMinorSubstitutions;
    }

    public MutationGenerator getMutationGenerator() {
        return mutationGenerator;
    }

    public void setMutationGenerator(MutationGenerator mutationGenerator) {
        this.mutationGenerator = mutationGenerator;
    }

    public MutationGenerator getMajorMutationGenerator() {
        return majorMutationGenerator;
    }

    public void setMajorMutationGenerator(MutationGenerator majorMutationGenerator) {
        this.majorMutationGenerator = majorMutationGenerator;
    }

    public int getUmiSize() {
        return umiSize;
    }

    public void setUmiSize(int umiSize) {
        this.umiSize = umiSize;
    }

    public int getMaxRandomFlankSize() {
        return maxRandomFlankSize;
    }

    public void setMaxRandomFlankSize(int maxRandomFlankSize) {
        this.maxRandomFlankSize = maxRandomFlankSize;
    }
}
