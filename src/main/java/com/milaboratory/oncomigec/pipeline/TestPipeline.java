package com.milaboratory.oncomigec.pipeline;

import com.milaboratory.oncomigec.core.io.misc.MigReaderParameters;
import com.milaboratory.oncomigec.core.io.readers.PMigReader;
import com.milaboratory.oncomigec.core.io.readers.SMigReader;
import com.milaboratory.oncomigec.preproc.demultiplex.config.BarcodeListParser;
import com.milaboratory.oncomigec.preproc.demultiplex.processor.CheckoutProcessor;
import com.milaboratory.oncomigec.preproc.demultiplex.processor.PAdapterExtractor;
import com.milaboratory.oncomigec.preproc.demultiplex.processor.SAdapterExtractor;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class TestPipeline {
    private final CheckoutProcessor checkoutProcessor;

    public TestPipeline(File fastq1, File fastq2,
                        File barcodes,
                        Presets presets,
                        MigReaderParameters migReaderParameters) throws Exception {
        checkoutProcessor = BarcodeListParser.generatePCheckoutProcessor(
                FileUtils.readLines(barcodes),
                presets.getDemultiplexParameters()
        );

        new PMigReader(fastq1, fastq2,
                (PAdapterExtractor) checkoutProcessor,
                migReaderParameters);
    }

    public TestPipeline(File fastq1,
                        File barcodes,
                        Presets presets,
                        MigReaderParameters migReaderParameters) throws Exception {
        checkoutProcessor = BarcodeListParser.generateSCheckoutProcessor(
                FileUtils.readLines(barcodes),
                presets.getDemultiplexParameters()
        );

        new SMigReader(fastq1,
                (SAdapterExtractor) checkoutProcessor,
                migReaderParameters);
    }

    public CheckoutProcessor getCheckoutProcessor() {
        return checkoutProcessor;
    }
}
