package com.milaboratory.oncomigec.pipeline;

import com.milaboratory.oncomigec.core.io.misc.MigReaderParameters;
import com.milaboratory.oncomigec.core.io.misc.UmiHistogram;
import com.milaboratory.oncomigec.model.classifier.BaseVariantClassifier;
import com.milaboratory.oncomigec.model.classifier.VariantClassifier;
import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.milaboratory.oncomigec.pipeline.IOUtils.writeStringToFile;

public final class MigecCli {
    private static boolean appendMode = false;

    private static final Class ME = MigecCli.class;
    private static final String MY_PATH = ME.getProtectionDomain().getCodeSource().getLocation().getFile(),
            MY_VERSION = ME.getPackage().getImplementationVersion();

    private static Date start = new Date();

    private static void print1(String message) {
        start = new Date();
        System.out.println("[" + start.toString() + "] " + message);
    }

    public static void print2(String message) {
        Date now = new Date();
        System.out.println("[" + now.toString() + " +" + timePassed(now.getTime() - start.getTime()) + "] " + message);
        //start = now;
    }

    private static String timePassed(long millis) {
        return String.format("%02dm%02ds",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }


    @SuppressWarnings("static-access")
    public static void main(String[] args) throws Exception {
        Options options = new Options();

        final String OPT_HELP_SHORT = "h", OPT_HELP_LONG = "help",
                OPT_VERSION_SHORT = "v", OPT_VERSION_LONG = "version",
                OPT_LOAD_PRESET = "load-preset",
                OPT_IMPORT_PRESET = "import-preset", OPT_EXPORT_PRESET = "export-preset",
                OPT_EXOME_LONG = "exome-mode", OPT_EXOME_SHORT = "E",
                OPT_TEST_LONG = "test-mode", OPT_TEST_SHORT = "T",
                OPT_APPEND = "append-mode",
                OPT_CLASSIFIER_FILE = "load-classifier",
                OPT_BARCODES_LONG = "barcodes", OPT_BARCODES_SHORT = "B",
                OPT_NO_BARCODES_LONG = "no-barcodes", OPT_NO_BARCODES_SHORT = "N",
                OPT_REFERENCES_LONG = "references", OPT_REFERENCES_SHORT = "R",
                OPT_FASTQ1_LONG = "first-read", OPT_FASTQ1_SHORT = "1",
                OPT_FASTQ2_LONG = "second-read", OPT_FASTQ2_SHORT = "2",
                OPT_UNPAIRED_LONG = "single-end", OPT_UNPAIRED_SHORT = "S",
                OPT_OUTPUT_LONG = "output-path", OPT_OUTPUT_SHORT = "O",
                OPT_DUMP_LONG = "dump-variants", OPT_DUMP_SHORT = "D";

        options
                .addOption(
                        OptionBuilder
                                .withDescription("display help message")
                                .withLongOpt(OPT_HELP_LONG)
                                .create(OPT_HELP_SHORT)
                )
                .addOption(
                        OptionBuilder
                                .withDescription("display version information")
                                .withLongOpt(OPT_VERSION_LONG)
                                .create(OPT_VERSION_SHORT)
                )
                        //
                        // parameters
                .addOption(
                        OptionBuilder
                                .withArgName("name")
                                .hasArg(true)
                                .withDescription("name of OncoMIGEC parameter preset " +
                                        "(currently Illumina-Exome or Torrent454-Exome, case-insensitive)")
                                .withLongOpt(OPT_LOAD_PRESET)
                                .create()
                )
                .addOption(
                        OptionBuilder
                                .withArgName("file")
                                .hasArg(true)
                                .withDescription("OncoMIGEC parameter file in XML format")
                                .withLongOpt(OPT_IMPORT_PRESET)
                                .create()
                )
                .addOption(
                        OptionBuilder
                                .withArgName("file")
                                .hasArg(true)
                                .withDescription("output current parameter preset to the specified XML file")
                                .withLongOpt(OPT_EXPORT_PRESET)
                                .create()
                )
                .addOption(
                        OptionBuilder
                                .withArgName("file")
                                .hasArg(true)
                                .withDescription("specifies a pre-trained classifier binary file (Weka model)")
                                .withLongOpt(OPT_CLASSIFIER_FILE)
                                .create()
                )
                        //
                        // modes
                .addOption(
                        OptionBuilder
                                .hasArg(false)
                                .withDescription("append mode, " +
                                        "will not overwrite files if specified")
                                .withLongOpt(OPT_APPEND)
                                .create()
                )
                .addOption(
                        OptionBuilder
                                .hasArg(false)
                                .withDescription("exome sequencing mode " +
                                        "(consensus is a subsequence " +
                                        "of a single exon or CDS)")
                                .withLongOpt(OPT_EXOME_LONG)
                                .create(OPT_EXOME_SHORT)
                )
                .addOption(
                        OptionBuilder
                                .withArgName("integer")
                                .hasArg(true)
                                .withDescription("test checkout of barcodes " +
                                        "for a sample of reads")
                                .withLongOpt(OPT_TEST_LONG)
                                .create(OPT_TEST_SHORT)
                )
                        //
                        // barcodes
                .addOption(
                        OptionBuilder
                                .withArgName("file")
                                .hasArg(true)
                                .withDescription("file with barcodes")
                                .withLongOpt(OPT_BARCODES_LONG)
                                .create(OPT_BARCODES_SHORT)
                )
                .addOption(
                        OptionBuilder
                                .withArgName("string")
                                .hasArg(true)
                                .withDescription("sample name (for naming consistency); " +
                                        "fastq input is assumed " +
                                        "to be pre-processed with Checkout")
                                .withLongOpt(OPT_NO_BARCODES_LONG)
                                .create(OPT_NO_BARCODES_SHORT)
                )
                        //
                        // input
                .addOption(
                        OptionBuilder
                                .withArgName("fasta")
                                .hasArg(true)
                                .withDescription("file with references")
                                .withLongOpt(OPT_REFERENCES_LONG)
                                .create(OPT_REFERENCES_SHORT)
                )
                .addOption(
                        OptionBuilder
                                .withArgName("fastq")
                                .hasArg(true)
                                .withDescription("fastq R1 file (paired-end)")
                                .withLongOpt(OPT_FASTQ1_LONG)
                                .create(OPT_FASTQ1_SHORT)
                )
                .addOption(
                        OptionBuilder
                                .withArgName("fastq")
                                .hasArg(true)
                                .withDescription("fastq R2 file (paired-end)")
                                .withLongOpt(OPT_FASTQ2_LONG)
                                .create(OPT_FASTQ2_SHORT)
                )
                .addOption(
                        OptionBuilder
                                .withArgName("fastq")
                                .hasArg(true)
                                .withDescription("fastq file (single-end)")
                                .withLongOpt(OPT_UNPAIRED_LONG)
                                .create(OPT_UNPAIRED_SHORT)
                )
                        //
                        // output
                .addOption(
                        OptionBuilder
                                .withArgName("path")
                                .hasArg(true)
                                .withDescription("path to output")
                                .withLongOpt(OPT_OUTPUT_LONG)
                                .create(OPT_OUTPUT_SHORT)
                )
                .addOption(
                        OptionBuilder
                                .withArgName("double")
                                .hasArg(true)
                                .withDescription("dumping mode for variants having less than the threshold frequency")
                                .withLongOpt(OPT_DUMP_LONG)
                                .create(OPT_DUMP_SHORT)
                );


        // create the parser
        CommandLineParser parser = new BasicParser();
        MigecPipeline pipeline = null;
        VariantClassifier variantClassifier = null;
        File outputFolder = null;
        double dumpFreq = -1;

        try {
            // parse the command line arguments
            CommandLine commandLine = parser.parse(options, args);

            // help
            if (args.length == 0 || commandLine.hasOption(OPT_HELP_SHORT)) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar " +
                                Paths.get(MY_PATH).getFileName(),
                        options
                );
                System.exit(0);
            }

            // version
            if (commandLine.hasOption(OPT_VERSION_SHORT)) {
                System.out.println("You are using OncoMIGEC pipeline of version " + MY_VERSION);
                System.exit(0);
            }

            // check all necessary arguments are provided

            // parameters
            MigecParameterSet parameterSet;
            if (commandLine.hasOption(OPT_IMPORT_PRESET)) {
                parameterSet = MigecParameterSet.loadFromFile(
                        new File(commandLine.getOptionValue(OPT_IMPORT_PRESET)));
            } else if (commandLine.hasOption(OPT_LOAD_PRESET)) {
                parameterSet = MigecParameterSet.fromPreset(commandLine.getOptionValue(OPT_LOAD_PRESET));
            } else
                parameterSet = new MigecParameterSet();

            if (commandLine.hasOption(OPT_EXPORT_PRESET)) {
                File exportPresetFile = new File(commandLine.getOptionValue(OPT_EXPORT_PRESET));
                parameterSet.writeToFile(exportPresetFile);
                System.out.println("Saved current preset to " + exportPresetFile.getAbsolutePath());
                System.exit(0);
            }

            if (commandLine.hasOption(OPT_CLASSIFIER_FILE)) {
                File classifierFile = new File(commandLine.getOptionValue(OPT_CLASSIFIER_FILE));
                variantClassifier = BaseVariantClassifier.pretrained(classifierFile);
            }

            // mode
            if (!commandLine.hasOption(OPT_EXOME_SHORT) && !commandLine.hasOption(OPT_TEST_SHORT))
                throw new ParseException("No mode has been set");
            if (commandLine.hasOption(OPT_APPEND))
                appendMode = true;

            // barcodes
            boolean doCheckout = commandLine.hasOption(OPT_BARCODES_SHORT);
            if (!doCheckout && !commandLine.hasOption(OPT_NO_BARCODES_SHORT))
                throw new ParseException("Barcodes / preprocessed input not specified");

            // input
            if (!commandLine.hasOption(OPT_REFERENCES_SHORT) && !commandLine.hasOption(OPT_TEST_SHORT))
                throw new ParseException("No references were provided");

            boolean paired = commandLine.hasOption(OPT_FASTQ1_SHORT) &&
                    commandLine.hasOption(OPT_FASTQ2_SHORT);

            if (!paired && (commandLine.hasOption(OPT_FASTQ1_SHORT) ||
                    commandLine.hasOption(OPT_FASTQ2_SHORT)))
                throw new ParseException("Arguments " + OPT_FASTQ1_SHORT + " and " +
                        OPT_FASTQ2_SHORT + " should be provided together");

            if (!paired && !commandLine.hasOption(OPT_UNPAIRED_SHORT))
                throw new ParseException("No input files provided");

            if (!commandLine.hasOption(OPT_OUTPUT_SHORT))
                throw new ParseException("No output path provided");


            // output
            outputFolder = new File(commandLine.getOptionValue(OPT_OUTPUT_SHORT));

            if (!outputFolder.mkdirs())
                if (!outputFolder.exists())
                    throw new ParseException("Failed to create output folder");
                else if (!appendMode)
                    System.out.println("WARNING: Output folder already exists, " +
                            "files may be overwritten! " +
                            "You can still skip with Ctrl + C while FASTQ files are being indexed");

            // dump
            if (commandLine.hasOption(OPT_DUMP_SHORT)) {
                dumpFreq = Double.parseDouble(commandLine.getOptionValue(OPT_DUMP_SHORT));
            }

            // =================
            // Pipeline creation
            // =================
            print1("Running OncoMIGEC v" + MigecCli.class.getPackage().getImplementationVersion() +
                            " for " +
                            (paired ?
                                    (commandLine.getOptionValue(OPT_FASTQ1_SHORT) +
                                            ", " + commandLine.getOptionValue(OPT_FASTQ2_SHORT)) :
                                    commandLine.getOptionValue(OPT_UNPAIRED_SHORT))
            );

            // --- test mode
            if (commandLine.hasOption(OPT_TEST_SHORT)) {
                if (!doCheckout)
                    throw new ParseException("Barcodes should be provided for testing mode");

                int testReadsCount;
                try {
                    testReadsCount = Integer.parseInt(commandLine.getOptionValue(OPT_TEST_SHORT));
                } catch (NumberFormatException e) {
                    throw new ParseException("Bad test mode parameter");
                }

                print2("Testing");

                TestPipeline testPipeline;

                if (paired)
                    testPipeline = new TestPipeline(
                            new File(commandLine.getOptionValue(OPT_FASTQ1_SHORT)),
                            new File(commandLine.getOptionValue(OPT_FASTQ2_SHORT)),
                            new File(commandLine.getOptionValue(OPT_BARCODES_SHORT)),
                            parameterSet,
                            MigReaderParameters.TEST(testReadsCount));
                else
                    testPipeline = new TestPipeline(
                            new File(commandLine.getOptionValue(OPT_UNPAIRED_SHORT)),
                            new File(commandLine.getOptionValue(OPT_BARCODES_SHORT)),
                            parameterSet,
                            MigReaderParameters.TEST(testReadsCount));

                writeStringToFile(new File(outputFolder.getAbsolutePath() + "/_" +
                                FilenameUtils.removeExtension(
                                        new File(commandLine.getOptionValue(OPT_BARCODES_SHORT)).getName()) +
                                ".test.txt"),
                        testPipeline.getCheckoutProcessor().toString()
                );

                print2("Finished, " +
                        ((int) (testPipeline.getCheckoutProcessor().extractionRatio() * 100)) +
                        "% barcodes extracted");

                System.exit(0);
            }

            // --- exome mode
            if (commandLine.hasOption(OPT_EXOME_SHORT)) {
                print2("Preprocessing data");
                if (!doCheckout) {
                    if (paired)
                        pipeline = ExomePipeline.preprocess(
                                new File(commandLine.getOptionValue(OPT_FASTQ1_SHORT)),
                                new File(commandLine.getOptionValue(OPT_FASTQ2_SHORT)),
                                commandLine.getOptionValue(OPT_NO_BARCODES_SHORT),
                                new File(commandLine.getOptionValue(OPT_REFERENCES_SHORT)),
                                parameterSet);
                    else
                        pipeline = ExomePipeline.preprocess(
                                new File(commandLine.getOptionValue(OPT_UNPAIRED_SHORT)),
                                commandLine.getOptionValue(OPT_NO_BARCODES_SHORT),
                                new File(commandLine.getOptionValue(OPT_REFERENCES_SHORT)),
                                parameterSet);
                } else {
                    if (paired)
                        pipeline = ExomePipeline.preprocess(
                                new File(commandLine.getOptionValue(OPT_FASTQ1_SHORT)),
                                new File(commandLine.getOptionValue(OPT_FASTQ2_SHORT)),
                                new File(commandLine.getOptionValue(OPT_BARCODES_SHORT)),
                                new File(commandLine.getOptionValue(OPT_REFERENCES_SHORT)),
                                parameterSet);
                    else
                        pipeline = ExomePipeline.preprocess(
                                new File(commandLine.getOptionValue(OPT_UNPAIRED_SHORT)),
                                new File(commandLine.getOptionValue(OPT_BARCODES_SHORT)),
                                new File(commandLine.getOptionValue(OPT_REFERENCES_SHORT)),
                                parameterSet);
                }
            }

            checkPreprocess(pipeline, outputFolder, parameterSet.getMinUniqueUmis(), parameterSet.getMinOverseq());
        } catch (ParseException e) {
            System.err.println("Bad arguments: " + e.getMessage());
            System.exit(-1);
        } catch (Exception e) {
            System.err.println("Error pre-processing input files: " + e.toString());
            e.printStackTrace();
            System.exit(-1);
        }

        // =======
        // Running
        // =======
        runFirstStage(pipeline, outputFolder);

        if (dumpFreq > 0) {
            dumpVariants(pipeline, outputFolder, dumpFreq);
            print2("Finished dumping variants");
            return;
        }

        if (variantClassifier != null) {
            // user-defined classifier
            pipeline.setVariantClassifier(variantClassifier);
        }

        runSecondStage(pipeline, outputFolder);

        print2("Finished");
    }

    private static void checkPreprocess(MigecPipeline pipeline, File outputFolder,
                                        int minMigCount, int minOverseq) throws IOException {
        // Check molecule count & overseq
        writeStringToFile(new File(outputFolder.getAbsolutePath() + "/_checkout.txt"),
                pipeline.getCheckoutOutput());

        List<String> samplesToSkip = new ArrayList<>();
        String sampleMessages = "";

        for (String sampleName : pipeline.getSamples()) {
            UmiHistogram histogram = pipeline.getHistogram(sampleName);

            String samplePrefix = outputFolder.getAbsolutePath() + "/" + sampleName;
            writeStringToFile(new File(samplePrefix + ".0.umihistogram.txt"),
                    histogram.toString());

            int migsTotal = pipeline.getMigsTotal(sampleName),
                    overSeq = pipeline.getOverSeq(sampleName);

            String message;

            if (migsTotal < minMigCount) {
                message = "SKIPPED\tEstimated number of molecules in sample is below allowed threshold, " +
                        migsTotal + " < " + minMigCount;
                System.out.println(message + ". Skipping " + sampleName);
                samplesToSkip.add(sampleName);
            } else {
                if (overSeq < minOverseq) {
                    message = "SKIPPED\tEstimated minimal MIG size is below allowed threshold, " +
                            overSeq + " < " + minOverseq;
                    System.out.println(message + ". Skipping " + sampleName);
                    samplesToSkip.add(sampleName);
                } else {
                    message = "PASSED\tEstimated minimal MIG size = " + overSeq +
                            ", MIGSs that passed size threshold = " + histogram.calculateMigsRetained(overSeq) +
                            ", number of reads in them = " + histogram.calculateReadsRetained(overSeq);
                    System.out.println(message + ". Proceeding with " + sampleName);
                }
            }

            sampleMessages += sampleName + "\t" + message + "\n";
        }

        pipeline.skipSamples(samplesToSkip);

        writeStringToFile(new File(outputFolder.getAbsolutePath() + "/_sample_messages.txt"),
                sampleMessages);
    }

    private static void runFirstStage(MigecPipeline pipeline, File outputFolder) {
        try {
            print2("Running first stage of OncoMiGEC");
            pipeline.runFirstStage();
        } catch (Exception e) {
            System.err.println("Error on first stage of analysis: " + e.toString());
            e.printStackTrace();
            System.exit(-1);
        }

        try {
            for (String sampleName : pipeline.getSamples()) {
                String samplePrefix = outputFolder.getAbsolutePath() + "/" + sampleName;
                writeStringToFile(new File(samplePrefix + ".1.assembler.txt"),
                        pipeline.getAssemblerOutput(sampleName));
                writeStringToFile(new File(samplePrefix + ".1.consaligner.txt"),
                        pipeline.getConsAlignerOutput(sampleName));
            }
        } catch (IOException e) {
            System.err.println("Error writing output: " + e.toString());
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static void dumpVariants(MigecPipeline pipeline, File outputFolder, double dumpFreq) throws IOException {
        writeStringToFile(new File(outputFolder.getAbsolutePath() + "/_vardump.txt"),
                pipeline.getMinorVariantDump(dumpFreq));
    }

    private static void runSecondStage(MigecPipeline pipeline, File outputFolder) {
        try {
            print2("Running second stage of OncoMiGEC");
            pipeline.runSecondStage();
        } catch (Exception e) {
            System.err.println("Error on second stage of analysis: " + e.toString());
            e.printStackTrace();
            System.exit(-1);
        }

        try {
            for (String sampleName : pipeline.getSamples()) {
                String samplePrefix = outputFolder.getAbsolutePath() + "/" + sampleName;
                writeStringToFile(new File(samplePrefix + ".2.corrector.txt"),
                        pipeline.getCorrectorOutput(sampleName));
                writeStringToFile(new File(samplePrefix + ".2.errorstat.txt"),
                        pipeline.getVariantLibraryOutput(sampleName));
                writeStringToFile(new File(samplePrefix + ".2.haplotypes.txt"),
                        pipeline.getHaplotypeTreeOutput(sampleName));
                writeStringToFile(new File(samplePrefix + ".2.haplotypes.fa"),
                        pipeline.getHaplotypeTreeFastaOutput(sampleName));
            }
        } catch (Exception e) {
            System.err.println("Error writing output: " + e.toString());
            e.printStackTrace();
            System.exit(-1);
        }
    }
}