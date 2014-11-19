package com.milaboratory.migec2.pipeline;

import com.milaboratory.migec2.core.io.misc.MigReaderParameters;
import com.milaboratory.migec2.core.io.readers.PMigReader;
import com.milaboratory.migec2.core.io.readers.SMigReader;
import com.milaboratory.migec2.preproc.demultiplex.config.BarcodeListParser;
import com.milaboratory.migec2.preproc.demultiplex.processor.CheckoutProcessor;
import com.milaboratory.migec2.preproc.demultiplex.processor.PCheckoutProcessor;
import com.milaboratory.migec2.preproc.demultiplex.processor.SCheckoutProcessor;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class TestPipeline {
    private final CheckoutProcessor checkoutProcessor;

    public TestPipeline(File fastq1, File fastq2,
                        File barcodes,
                        MigecParameterSet migecParameterSet,
                        MigReaderParameters migReaderParameters) throws Exception {
        checkoutProcessor = BarcodeListParser.generatePCheckoutProcessor(
                FileUtils.readLines(barcodes),
                migecParameterSet.getDemultiplexParameters()
        );

        new PMigReader(fastq1, fastq2,
                (PCheckoutProcessor) checkoutProcessor,
                migReaderParameters);
    }

    public TestPipeline(File fastq1,
                        File barcodes,
                        MigecParameterSet migecParameterSet,
                        MigReaderParameters migReaderParameters) throws Exception {
        checkoutProcessor = BarcodeListParser.generateSCheckoutProcessor(
                FileUtils.readLines(barcodes),
                migecParameterSet.getDemultiplexParameters()
        );

        new SMigReader(fastq1,
                (SCheckoutProcessor) checkoutProcessor,
                migReaderParameters);
    }

    public CheckoutProcessor getCheckoutProcessor() {
        return checkoutProcessor;
    }
}
