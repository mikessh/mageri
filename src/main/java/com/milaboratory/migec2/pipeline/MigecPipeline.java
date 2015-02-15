package com.milaboratory.migec2.pipeline;

import cc.redberry.pipe.OutputPort;
import cc.redberry.pipe.blocks.Merger;
import cc.redberry.pipe.blocks.ParallelProcessor;
import cc.redberry.pipe.util.CountingOutputPort;
import com.milaboratory.migec2.core.align.reference.Reference;
import com.milaboratory.migec2.core.assemble.entity.Consensus;
import com.milaboratory.migec2.core.assemble.misc.AssemblerFactory;
import com.milaboratory.migec2.core.assemble.processor.Assembler;
import com.milaboratory.migec2.core.consalign.entity.AlignedConsensus;
import com.milaboratory.migec2.core.consalign.entity.AlignerReferenceLibrary;
import com.milaboratory.migec2.core.consalign.misc.ConsensusAlignerFactory;
import com.milaboratory.migec2.core.consalign.mutations.MutationsAndCoverage;
import com.milaboratory.migec2.core.consalign.processor.ConsensusAligner;
import com.milaboratory.migec2.core.correct.CorrectedConsensus;
import com.milaboratory.migec2.core.correct.Corrector;
import com.milaboratory.migec2.core.haplotype.HaplotypeTree;
import com.milaboratory.migec2.core.io.entity.Mig;
import com.milaboratory.migec2.core.io.misc.UmiHistogram;
import com.milaboratory.migec2.core.io.readers.MigReader;
import com.milaboratory.migec2.model.classifier.BaseVariantClassifier;
import com.milaboratory.migec2.model.classifier.VariantClassifier;
import com.milaboratory.migec2.model.variant.Variant;
import com.milaboratory.migec2.model.variant.VariantContainer;
import com.milaboratory.migec2.model.variant.VariantLibrary;
import com.milaboratory.migec2.util.ProcessorResultWrapper;

import java.util.*;

public class MigecPipeline {
    private static final boolean ENABLE_BUFFERING = true, VERBOSE = true;
    private static final int THREADS = Runtime.getRuntime().availableProcessors();   // todo: as parameter
    protected final MigReader reader;
    protected final Map<String, Assembler> assemblerBySample;
    protected final Map<String, ConsensusAligner> alignerBySample;
    protected final Map<String, List<AlignedConsensus>> alignmentDataBySample;
    protected final Map<String, Corrector> correctorBySample;
    protected final Map<String, VariantLibrary> variantLibraryBySample;
    protected final Map<String, HaplotypeTree> haplotypeTreeBySample;
    protected final List<String> sampleNames, skippedSamples;
    protected final MigecParameterSet migecParameterSet;
    protected final VariantClassifier variantClassifier = BaseVariantClassifier.BUILT_IN; // todo: implement loading from file

    protected MigecPipeline(MigReader reader,
                            AssemblerFactory assemblerFactory,
                            ConsensusAlignerFactory consensusAlignerFactory,
                            MigecParameterSet migecParameterSet) {
        this.reader = reader;
        this.alignmentDataBySample = new HashMap<>();
        this.assemblerBySample = new HashMap<>();
        this.alignerBySample = new HashMap<>();
        this.correctorBySample = new HashMap<>();
        this.variantLibraryBySample = new HashMap<>();
        this.haplotypeTreeBySample = new HashMap<>();
        this.sampleNames = reader.getSampleNames();
        this.skippedSamples = new ArrayList<>();
        this.migecParameterSet = migecParameterSet;
        for (String sampleName : sampleNames) {
            assemblerBySample.put(sampleName, assemblerFactory.create());
            alignerBySample.put(sampleName, consensusAlignerFactory.create());
        }
    }

    public void skipSamples(List<String> samplesToSkip) {
        skippedSamples.addAll(samplesToSkip);
        sampleNames.removeAll(samplesToSkip);
    }

    public UmiHistogram getHistogram(String sampleName) {
        return reader.getUmiHistogram(sampleName);
    }

    public int getOverSeq(String sampleName) {
        return migecParameterSet.forceOverseq() ? migecParameterSet.getDefaultOverseq() :
                reader.getUmiHistogram(sampleName).getMigSizeThreshold();
    }

    public int getMigsTotal(String sampleName) {
        return reader.getUmiHistogram(sampleName).getMigsTotal();
    }

    public void runFirstStage() {
        for (final String sampleName : sampleNames) {
            Assembler assembler = assemblerBySample.get(sampleName);
            ConsensusAligner aligner = alignerBySample.get(sampleName);

            List<AlignedConsensus> alignmentDataList;
            alignmentDataBySample.put(sampleName, alignmentDataList = new LinkedList<>());

            int overSeq = getOverSeq(sampleName);

            reader.setCurrentSample(sampleName);
            reader.setSizeThreshold(overSeq);

            if (migecParameterSet.filterMismatchUmis())
                reader.setMinMismatchRatio(migecParameterSet.getUmiMismatchFilterRatio());

            OutputPort<Mig> input = reader;

            if (ENABLE_BUFFERING) {
                final Merger<Mig> bufferedInput = new Merger<>(524288);
                bufferedInput.merge(input);
                bufferedInput.start();
                input = bufferedInput;
            }

            final CountingOutputPort<Mig> countingInput = new CountingOutputPort<>(input);

            if (VERBOSE)
                new Thread(new Runnable() {
                    long prevCount = -1;

                    @Override
                    public void run() {
                        try {
                            while (!countingInput.isClosed()) {
                                long count = countingInput.getCount();
                                if (prevCount != count) {
                                    MigecCli.print2("Running first stage of MIGEC for " + sampleName +
                                            ", " + count + " MIGs processed..");
                                    prevCount = count;
                                }
                                Thread.sleep(10000);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            // Assemble in parallel
            final OutputPort<ProcessorResultWrapper<Consensus>> assemblyResults =
                    new ParallelProcessor<>(countingInput, assembler, THREADS);

            final OutputPort<ProcessorResultWrapper<AlignedConsensus>> alignerResults =
                    new ParallelProcessor<>(assemblyResults, aligner, THREADS);

            ProcessorResultWrapper<AlignedConsensus> alignmentData;
            while ((alignmentData = alignerResults.take()) != null)
                if (alignmentData.hasResult())
                    alignmentDataList.add(alignmentData.getResult());

            if (VERBOSE)
                MigecCli.print2("Finished first stage of MIGEC for " + sampleName +
                        ", " + countingInput.getCount() + " MIGs processed in total");
        }
    }

    public void runSecondStage() throws Exception {
        for (String sampleName : sampleNames) {
            List<AlignedConsensus> alignmentDataList = alignmentDataBySample.get(sampleName);
            ConsensusAligner aligner = alignerBySample.get(sampleName);

            // Find major and minor mutations
            Corrector corrector = new Corrector(aligner.getAlignerReferenceLibrary(),
                    migecParameterSet.getCorrectorParameters(),
                    variantClassifier);
            correctorBySample.put(sampleName, corrector);

            // Error statistics for haplotype filtering using binomial test
            // Store here for output summary purposes
            variantLibraryBySample.put(sampleName,
                    corrector.getCorrectorReferenceLibrary().getVariantLibrary());

            // Haplotype 1-mm graph
            HaplotypeTree haplotypeTree = new HaplotypeTree(
                    corrector.getCorrectorReferenceLibrary(),
                    migecParameterSet.getHaplotypeTreeParameters());
            haplotypeTreeBySample.put(sampleName, haplotypeTree);

            // Correction processing (MIGEC)
            for (AlignedConsensus alignmentData : alignmentDataList) {
                CorrectedConsensus correctedConsensus = corrector.correct(alignmentData);
                if (correctedConsensus != null)
                    haplotypeTree.add(correctedConsensus);
            }

            // Haplotype filtering
            haplotypeTree.calculatePValues();
        }
    }

    public List<String> getSamples() {
        return sampleNames;
    }

    public String getCheckoutOutput() {
        return reader.getCheckoutProcessor().toString();
    }

    public String getAssemblerOutput(String sampleName) {
        return assemblerBySample.get(sampleName).toString();
    }

    public String getConsAlignerOutput(String sampleName) {
        return alignerBySample.get(sampleName).getAlignerReferenceLibrary().toString();
    }

    public String getCorrectorOutput(String sampleName) {
        return correctorBySample.get(sampleName).getCorrectorReferenceLibrary().toString();
    }

    public String getVariantLibraryOutput(String sampleName) {
        return variantLibraryBySample.get(sampleName).toString();
    }

    public String getHaplotypeTreeOutput(String sampleName) {
        return haplotypeTreeBySample.get(sampleName).toString();
    }

    public String getHaplotypeTreeFastaOutput(String sampleName) {
        if (migecParameterSet.outputFasta())
            return haplotypeTreeBySample.get(sampleName).toFastaString();
        else
            return "";
    }

    public String getMinorVariantDump(double threshold) {
        String dump = "#SampleName\t" + Variant.HEADER;
        for (String sample : sampleNames) {
            AlignerReferenceLibrary alignerReferenceLibrary = alignerBySample.get(sample).getAlignerReferenceLibrary();
            for (Reference reference : alignerReferenceLibrary.getReferenceLibrary().getReferences()) {
                MutationsAndCoverage mutationsAndCoverage = alignerReferenceLibrary.getMutationsAndCoverage(reference);
                VariantContainer variantContainer = new VariantContainer(mutationsAndCoverage, threshold);
                for (Variant variant : variantContainer.getMinorVariants()) {
                    dump += "\n" + sample + "\t" + variant.toString();
                }
            }
        }
        return dump;
    }
}
