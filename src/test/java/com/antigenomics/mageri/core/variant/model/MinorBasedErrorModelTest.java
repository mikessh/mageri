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

package com.antigenomics.mageri.core.variant.model;

import com.antigenomics.mageri.core.Mig;
import com.antigenomics.mageri.core.assemble.Assembler;
import com.antigenomics.mageri.core.assemble.Consensus;
import com.antigenomics.mageri.core.assemble.SAssembler;
import com.antigenomics.mageri.core.genomic.Reference;
import com.antigenomics.mageri.core.genomic.ReferenceLibrary;
import com.antigenomics.mageri.core.mapping.ConsensusAligner;
import com.antigenomics.mageri.core.mapping.MutationsTable;
import com.antigenomics.mageri.core.mapping.SConsensusAligner;
import com.antigenomics.mageri.core.mapping.alignment.Aligner;
import com.antigenomics.mageri.core.mapping.alignment.ExtendedKmerAligner;
import com.antigenomics.mageri.core.variant.VariantCallerParameters;
import com.antigenomics.mageri.generators.ModelMigGenerator2;
import com.antigenomics.mageri.generators.RandomReferenceGenerator;
import org.junit.Assert;
import org.junit.Test;

public class MinorBasedErrorModelTest {
    @Test
    public void test() {
        int nMigs = 10000, migSize = 100;

        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator();
        randomReferenceGenerator.setReferenceSizeMin(200);
        randomReferenceGenerator.setReferenceSizeMax(200);

        ReferenceLibrary referenceLibrary = randomReferenceGenerator.nextReferenceLibrary(1);
        Reference reference = referenceLibrary.getAt(0);
        Assembler assembler = new SAssembler();
        Aligner aligner = new ExtendedKmerAligner(referenceLibrary);

        ConsensusAligner consensusAligner = new SConsensusAligner(aligner);

        VariantCallerParameters variantCallerParameters =
                VariantCallerParameters.DEFAULT
                        .withOrder(0);

        ModelMigGenerator2 modelMigGenerator = new ModelMigGenerator2(variantCallerParameters,
                reference, migSize);

        for (int j = 0; j < nMigs; j++) {
            Mig mig = modelMigGenerator.nextMig();
            Consensus consensus = assembler.assemble(mig);
            if (consensus != null) {
                consensusAligner.align(consensus);
            }
        }

        MutationsTable mutationsTable = consensusAligner.getAlignerTable(reference);
        MinorBasedErrorModel errorModel = new MinorBasedErrorModel(variantCallerParameters,
                mutationsTable, assembler.getMinorCaller());

        for (int i = 0; i < reference.size(); i++) {
            int base = reference.codeAt(i);
            for (int j = 0; j < 4; j++) {
                if (base != j) {
                    double errorRateEst = errorModel.computeErrorRate(i, base, j).getErrorRate(),
                            errorRateExp = modelMigGenerator.getPcrMutationGenerator().getSubstitutionModel().getValue(base, j);
                    if (errorRateExp < 1e-6) {
                        Assert.assertTrue("Small error rate in absence of errors",
                                errorRateEst < 5e-6);
                    } else {
                        Assert.assertTrue("No more than order of magnitude difference between expected PCR " +
                                        "error rate and its estimate",
                                Math.abs(Math.log10(errorRateEst) - Math.log10(errorRateExp)) <= 1.0);
                    }
                }
            }
        }
    }
}
