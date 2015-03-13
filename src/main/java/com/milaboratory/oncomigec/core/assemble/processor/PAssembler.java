package com.milaboratory.oncomigec.core.assemble.processor;

import com.milaboratory.oncomigec.core.assemble.entity.PConsensus;
import com.milaboratory.oncomigec.core.assemble.entity.SConsensus;
import com.milaboratory.oncomigec.core.assemble.misc.AssemblerParameters;
import com.milaboratory.oncomigec.core.io.entity.PMig;

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
            readsAssembled.addAndGet(consensus.size());
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
    public String formattedSequenceHeader() {
        return PConsensus.formattedSequenceHeader();
    }

    @Override
    public boolean isPairedEnd() {
        return true;
    }
}
