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

package com.milaboratory.oncomigec.core.variant;

import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import com.milaboratory.oncomigec.DoubleRangeAssertion;
import com.milaboratory.oncomigec.core.assemble.SAssembler;
import com.milaboratory.oncomigec.core.assemble.SConsensus;
import com.milaboratory.oncomigec.core.genomic.Reference;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.core.input.SMig;
import com.milaboratory.oncomigec.core.mapping.ConsensusAligner;
import com.milaboratory.oncomigec.core.mapping.SConsensusAligner;
import com.milaboratory.oncomigec.generators.MutationGenerator;
import com.milaboratory.oncomigec.generators.RandomMigGenerator;
import com.milaboratory.oncomigec.generators.RandomReferenceGenerator;
import org.junit.Test;

public class MinorMatrixTest {
    @Test
    @SuppressWarnings("unchecked")
    public void test() {
        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator();

        randomReferenceGenerator.setReferenceSizeMin(200);
        randomReferenceGenerator.setReferenceSizeMax(200);

        ReferenceLibrary referenceLibrary = randomReferenceGenerator.nextReferenceLibrary(1);
        Reference reference = referenceLibrary.getAt(0);

        int migSize = 100;

        RandomMigGenerator randomMigGenerator = new RandomMigGenerator();
        MutationGenerator mutationGenerator = MutationGenerator.NO_INDEL;
        randomMigGenerator.setMutationGenerator(mutationGenerator);
        randomMigGenerator.setMigSizeMax(migSize);
        randomMigGenerator.setMigSizeMin(migSize);

        SAssembler assembler = new SAssembler();
        ConsensusAligner aligner = new SConsensusAligner(referenceLibrary);

        for (int i = 0; i < 1000; i++) {
            SMig mig = randomMigGenerator.nextMig(reference).getSMig();
            SConsensus consensus = assembler.assemble(mig);
            if (consensus != null) {
                aligner.align(consensus);
            }
        }

        MinorMatrix minorMatrix = MinorMatrix.fromMutationsTable(aligner.getAlignerTable(reference));

        DoubleRangeAssertion assertion =
                DoubleRangeAssertion.createRange("Observed vs expected substitution rate ratio", "Random MIG test", 0.7, 1.3);
        
        double JITTER = 1e-6;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i != j) {
                    double observed = minorMatrix.getRate(i, j) / migSize + JITTER,
                            expected = mutationGenerator.getSubstitutionModel().getValue(i, j) + JITTER;
                    System.out.println(
                            NucleotideAlphabet.INSTANCE.symbolFromCode((byte) i) + ">" +
                                    NucleotideAlphabet.INSTANCE.symbolFromCode((byte) j) + "\t" +
                                    observed + "\t" + expected);
                    assertion.assertInRange(observed / expected);
                }
            }
        }
    }
}
