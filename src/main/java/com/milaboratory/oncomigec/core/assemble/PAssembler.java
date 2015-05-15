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

package com.milaboratory.oncomigec.core.assemble;

import com.milaboratory.oncomigec.core.input.PMig;

public final class PAssembler extends Assembler<PConsensus, PMig> {
    private final SAssembler assembler1, assembler2;

    public PAssembler() {
        this(new SAssembler(), new SAssembler());
    }

    public PAssembler(AssemblerParameters assemblerParameters) {
        this.assembler1 = new SAssembler(assemblerParameters);
        this.assembler2 = new SAssembler(assemblerParameters);
        assembler1.storeConsensuses = false;
        assembler2.storeConsensuses = false;
    }

    public PAssembler(SAssembler assembler1, SAssembler assembler2) {
        assembler1.storeConsensuses = false;
        assembler2.storeConsensuses = false;
        this.assembler1 = assembler1;
        this.assembler2 = assembler2;
    }

    @Override
    public PConsensus assemble(PMig pMig) {
        SConsensus result1 = assembler1.assemble(pMig.getMig1()),
                result2 = assembler2.assemble(pMig.getMig2());

        readsTotal.addAndGet(pMig.size());
        migsTotal.incrementAndGet();
        if (result1 == null || result2 == null) {
            //System.out.println(pMig.getMig1().getReads().get(0).getData().getSequence().toString() + "\t" +
            //        pMig.getMig2().getReads().get(0).getData().getSequence().toString());
            return null;
        } else {
            PConsensus consensus = new PConsensus(result1, result2);
            readsAssembled.addAndGet(consensus.getAssembledSize());
            migsAssembled.incrementAndGet();
            consensusList.add(consensus);
            return consensus;
        }
    }

    @Override
    public long getReadsDroppedShortR1() {
        return assembler1.getReadsDroppedShortR1();
    }

    @Override
    public long getReadsDroppedErrorR1() {
        return assembler1.getReadsDroppedErrorR1();
    }

    @Override
    public long getReadsDroppedShortR2() {
        return assembler2.getReadsDroppedShortR1();
    }

    @Override
    public long getReadsDroppedErrorR2() {
        return assembler2.getReadsDroppedErrorR1();
    }

    @Override
    public boolean isPairedEnd() {
        return true;
    }
}
