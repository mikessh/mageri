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

package com.milaboratory.oncomigec.pipeline;

import com.milaboratory.oncomigec.pipeline.analysis.ProjectAnalysis;
import com.milaboratory.oncomigec.pipeline.input.*;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;

@SuppressWarnings("static-access")
public final class Oncomigec {
    private static final Class ME = Oncomigec.class;
    public static final String
            MY_NAME = "oncomigec",
            MY_PATH = ME.getProtectionDomain().getCodeSource().getLocation().getFile(),
            MY_VERSION = ME.getPackage().getImplementationVersion(),
            MY_COMMAND = System.getProperty("sun.java.command");

    public static ProjectAnalysis loadAnalysis(File file) throws IOException, ClassNotFoundException {
        return (ProjectAnalysis) SerializationUtils.readObjectFromFile(file);
    }

    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new BasicParser();

        RuntimeParameters runtimeParameters = null;
        Presets presets = null;
        Input input = null;
        String outputFolder = null;
        boolean writeBinary = false;

        try {
            // parse the command line arguments
            CommandLine commandLine = parser.parse(CLI, args);

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Basic

            // help
            if (args.length == 0 || commandLine.hasOption(OPT_HELP_SHORT)) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar " +
                                Paths.get(MY_PATH).getFileName(),
                        CLI
                );
                System.exit(0);
            }

            // version
            if (commandLine.hasOption(OPT_VERSION_SHORT)) {
                System.out.println("You are using OncoMIGEC pipeline of version " + MY_VERSION);
                System.exit(0);
            }

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Runtime
            runtimeParameters = parseRuntimeParameters(commandLine);

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Presets
            presets = parsePresets(commandLine);

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Input
            input = parseInput(commandLine);

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Output
            outputFolder = ".";
            if (commandLine.hasOption(OPT_OUTPUT_SHORT)) {
                outputFolder = commandLine.getOptionValue(OPT_OUTPUT_SHORT);
            }
            FileUtils.forceMkdir(new File(outputFolder));

            writeBinary = commandLine.hasOption(OPT_BINARY_OUTPUT);
        } catch (ParseException e) {
            System.err.println("Bad arguments: " + e.getMessage());
            System.exit(-1);
        } catch (IOException e) {
            System.err.println("Bad input file: " + e.toString());
            e.printStackTrace();
            System.exit(-1);
        } catch (Exception e) {
            System.err.println("Unknown error: " + e.toString());
            e.printStackTrace();
            System.exit(-1);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Prepare
        ProjectAnalysis projectAnalysis = new ProjectAnalysis(input, presets, runtimeParameters);

        projectAnalysis.setOutputPath(outputFolder);
        projectAnalysis.setWriteBinary(writeBinary);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Run
        projectAnalysis.run();
    }

    public static Presets parsePresets(CommandLine commandLine) throws JDOMException, IOException {
        Presets presets;
        String platform = commandLine.getOptionValue(OPT_PLATFORM, "illumina"),
                libraryType = commandLine.getOptionValue(OPT_LIBRARY_TYPE, "ss");
        presets = Presets.create(platform, libraryType);

        if (commandLine.hasOption(OPT_IMPORT_PRESET)) {
            presets = Presets.loadFromFile(
                    new File(commandLine.getOptionValue(OPT_IMPORT_PRESET)));
        }

        if (commandLine.hasOption(OPT_EXPORT_PRESET)) {
            File exportPresetFile = new File(commandLine.getOptionValue(OPT_EXPORT_PRESET));
            presets.writeToFile(exportPresetFile);
            System.out.println("Saved current preset to " + exportPresetFile.getAbsolutePath());
            System.exit(0);
        }

        return presets;
    }

    public static RuntimeParameters parseRuntimeParameters(CommandLine commandLine) {
        int numberOfThreads = Runtime.getRuntime().availableProcessors();
        long readLimit = -1;
        byte verbosity = 2;

        if (commandLine.hasOption(OPT_THREADS)) {
            numberOfThreads = Integer.parseInt(commandLine.getOptionValue(OPT_THREADS));
        }
        if (commandLine.hasOption(OPT_LIMIT)) {
            readLimit = Long.parseLong(commandLine.getOptionValue(OPT_LIMIT));
        }
        if (commandLine.hasOption(OPT_VERBOSITY)) {
            verbosity = Byte.parseByte(commandLine.getOptionValue(OPT_VERBOSITY));
        }

        return new RuntimeParameters(numberOfThreads, readLimit, verbosity);
    }

    public static Input parseInput(CommandLine commandLine) throws ParseException, IOException {
        Input input;
        if (commandLine.hasOption(OPT_INPUT_SHORT)) {
            input = new InputParser().parseJson(commandLine.getOptionValue(OPT_INPUT_SHORT));
        } else {
            if (!commandLine.hasOption(OPT_R1)) {
                throw new ParseException("Neither input FASTQ file (-" + OPT_R1 +
                        ") or project JSON file (-" + OPT_INPUT_SHORT + ") was specified.");
            }
            if (!commandLine.hasOption(OPT_META_REFS)) {
                throw new ParseException("Should provide reference FASTA file (--" + OPT_META_REFS + ") " +
                        " when input files are specified manually (-" + OPT_R1 + ").");
            }

            String projectName = commandLine.getOptionValue(OPT_NAME_PROJECT, "my_project");

            InputChunk inputChunk = parseInputChunk(commandLine);

            String referenceFileName = commandLine.getOptionValue(OPT_META_REFS),
                    bedFileName = getOptionValue(commandLine, OPT_META_BED),
                    contigsFileName = getOptionValue(commandLine, OPT_META_CONTIGS);

            input = new Input(projectName,
                    FileIOProvider.INSTANCE.getWrappedStream(referenceFileName),
                    bedFileName == null ? null : FileIOProvider.INSTANCE.getWrappedStream(bedFileName),
                    contigsFileName == null ? null : FileIOProvider.INSTANCE.getWrappedStream(contigsFileName),
                    inputChunk
            );
        }

        return input;
    }

    private static String getOptionValue(CommandLine commandLine, String optionName) {
        return commandLine.hasOption(optionName) ? commandLine.getOptionValue(optionName) : null;
    }

    public static InputChunk parseInputChunk(CommandLine commandLine) throws IOException, ParseException {
        String sampleName = commandLine.getOptionValue(OPT_NAME_SAMPLE, "my_sample"),
                groupName = commandLine.getOptionValue(OPT_NAME_GROUP, "my_group");

        boolean paired = commandLine.hasOption(OPT_R2);

        String fastq1Name = commandLine.getOptionValue(OPT_R1),
                fastq2Name = getOptionValue(commandLine, OPT_R2);

        InputStream fastq1Stream = FileIOProvider.INSTANCE.getStream(fastq1Name),
                fastq2Stream = paired ? FileIOProvider.INSTANCE.getStream(fastq2Name) : null;

        return new InputChunk(fastq1Stream, fastq2Stream, groupName,
                parseCheckoutRule(commandLine, sampleName, paired));
    }

    public static CheckoutRule parseCheckoutRule(CommandLine commandLine,
                                                 String sampleName,
                                                 boolean paired) throws IOException, ParseException {
        CheckoutRule checkoutRule = null;

        if (commandLine.hasOption(OPT_MODE_MULTIPLEX_SHORT)) {
            String barcodesFileName = commandLine.getOptionValue(OPT_MODE_MULTIPLEX_SHORT);
            List<String> barcodes = FileUtils.readLines(new File(barcodesFileName));
            checkoutRule = new SubMultiplexRule(sampleName, barcodes, paired);
        }
        if (commandLine.hasOption(OPT_MODE_PRIMER_SHORT)) {
            if (checkoutRule != null) {
                throw new ParseException("Only one checkout rule is allowed.");
            }

            String barcodesFileName = commandLine.getOptionValue(OPT_MODE_PRIMER_SHORT);
            List<String> barcodes = FileUtils.readLines(new File(barcodesFileName));
            checkoutRule = new PrimerRule(sampleName, barcodes, paired);
        }
        if (commandLine.hasOption(OPT_MODE_POSITIONAL_SHORT)) {
            if (checkoutRule != null) {
                throw new ParseException("Only one checkout rule is allowed.");
            }

            String maskString = commandLine.getOptionValue(OPT_MODE_POSITIONAL_SHORT);

            String[] masks = maskString.split(":");

            String mask1 = masks[0], mask2 = masks.length > 1 ? masks[1] : null;
            checkoutRule = new PositionalRule(sampleName, mask1, mask2, paired);
        }
        if (commandLine.hasOption(OPT_MODE_HEADER_SHORT)) {
            if (checkoutRule != null) {
                throw new ParseException("Only one checkout rule is allowed.");
            }

            checkoutRule = new PreprocessedRule(sampleName);
        }

        if (checkoutRule == null) {
            throw new ParseException("No checkout rule was specified for manual input  (-" + OPT_R1 + ").");
        }

        return checkoutRule;
    }

    // General options, batch analysis
    private static final String
            OPT_HELP_SHORT = "h", OPT_HELP_LONG = "help", OPT_VERSION_SHORT = "v", OPT_VERSION_LONG = "version",
            OPT_VERBOSITY = "verbosity", OPT_THREADS = "threads", OPT_LIMIT = "limit",
            OPT_PLATFORM = "platform", OPT_LIBRARY_TYPE = "library-type",
            OPT_IMPORT_PRESET = "import-preset", OPT_EXPORT_PRESET = "export-preset",
            OPT_INPUT_LONG = "input", OPT_INPUT_SHORT = "I",
            OPT_OUTPUT_LONG = "output-path", OPT_OUTPUT_SHORT = "O",
            OPT_BINARY_OUTPUT = "write-binary";

    // Manual analysis options
    private static final String
            OPT_MODE_MULTIPLEX_LONG = "multiplex", OPT_MODE_MULTIPLEX_SHORT = "M1",
            OPT_MODE_PRIMER_LONG = "primer", OPT_MODE_PRIMER_SHORT = "M2",
            OPT_MODE_POSITIONAL_LONG = "positional", OPT_MODE_POSITIONAL_SHORT = "M3",
            OPT_MODE_HEADER_LONG = "header", OPT_MODE_HEADER_SHORT = "M4",
            OPT_NAME_PROJECT = "project-name",
            OPT_NAME_GROUP = "group-name",
            OPT_NAME_SAMPLE = "sample-name",
            OPT_META_REFS = "references",
            OPT_META_BED = "bed",
            OPT_META_CONTIGS = "contigs",
            OPT_R1 = "R1", OPT_R2 = "R2";

    private static final Options CLI = new Options()
            //
            // Basic
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
                    // Runtime
            .addOption(
                    OptionBuilder
                            .withArgName("0..3")
                            .hasArg(true)
                            .withDescription("Sets the verbosity level: " +
                                    "0 - silent (errors only), " +
                                    "1 - major steps (pre-processing, assembly, etc)," +
                                    "2 - minor steps (reads parsed, migs processed, etc)," +
                                    "3 - miscellaneous messages. " +
                                    "[default = 2]")
                            .withLongOpt(OPT_VERBOSITY)
                            .create()
            )
            .addOption(
                    OptionBuilder
                            .withArgName("integer")
                            .hasArg(true)
                            .withDescription("Number of processors to use. " +
                                    "[default = all available processors]")
                            .withLongOpt(OPT_THREADS)
                            .create()
            )
            .addOption(
                    OptionBuilder
                            .withArgName("long integer")
                            .hasArg(true)
                            .withDescription("Number of reads to take. " +
                                    "[default = -1, all]")
                            .withLongOpt(OPT_LIMIT)
                            .create()
            )
                    //
                    // Preset
            .addOption(
                    OptionBuilder
                            .withArgName("name")
                            .hasArg(true)
                            .withDescription("Sequencing platform: " +
                                    "Illumina, " +
                                    "IonTorrent " +
                                    "or Roche454. " +
                                    "[default = Illumina]")
                            .withLongOpt(OPT_PLATFORM)
                            .create()
            )
            .addOption(
                    OptionBuilder
                            .withArgName("name")
                            .hasArg(true)
                            .withDescription("Library type: " +
                                    "SS (single-stranded start, RT-PCR or linear PCR) " +
                                    "or DS (double stranded start)" +
                                    "[default = SS]")
                            .withLongOpt(OPT_LIBRARY_TYPE)
                            .create()
            )
            .addOption(
                    OptionBuilder
                            .withArgName("file")
                            .hasArg(true)
                            .withDescription("OncoMIGEC parameter file in XML format.")
                            .withLongOpt(OPT_IMPORT_PRESET)
                            .create()
            )
            .addOption(
                    OptionBuilder
                            .withArgName("file")
                            .hasArg(true)
                            .withDescription("Output current parameter preset to the specified XML file.")
                            .withLongOpt(OPT_EXPORT_PRESET)
                            .create()
            )
                    //
                    // Input
            .addOption(
                    OptionBuilder
                            .withArgName("json file")
                            .hasArg(true)
                            .withDescription("Path to a file that specifies the input project structure and data. [required]")
                            .withLongOpt(OPT_INPUT_LONG)
                            .create(OPT_INPUT_SHORT)
            )
                    //
                    // Manual
            .addOption(
                    OptionBuilder
                            .withArgName("fastq[.gz]")
                            .hasArg(true)
                            .withDescription("(manual input) First read file.")
                            .create(OPT_R1)
            )
            .addOption(
                    OptionBuilder
                            .withArgName("fastq[.gz]")
                            .hasArg(true)
                            .withDescription("(manual input) Second read file. [optional]")
                            .create(OPT_R1)
            )
            .addOption(
                    OptionBuilder
                            .withArgName("fasta")
                            .hasArg(true)
                            .withDescription("(manual input) File with reference sequences.")
                            .withLongOpt(OPT_META_REFS)
                            .create()
            )
            .addOption(
                    OptionBuilder
                            .withArgName("bed")
                            .hasArg(true)
                            .withDescription("(manual input) " +
                                    "BED file with genomic info for references. " +
                                    "0-based. " +
                                    "Should contain 6 columns, including strand and name. " +
                                    "See https://genome.ucsc.edu/FAQ/FAQformat.html#format1 for details. " +
                                    "Name column should match names in reference FASTA file, " +
                                    "unmatched references will be skipped." +
                                    "[optional]")
                            .withLongOpt(OPT_META_BED)
                            .create()
            )
            .addOption(
                    OptionBuilder
                            .withArgName("file")
                            .hasArg(true)
                            .withDescription("(manual input) " +
                                    "Contig information file, " +
                                    "should contain three columns: " +
                                    "contig name, assembly name and contig length. " +
                                    "Required if using BED file for genomic info, " +
                                    "contig names should match names specified in BED file." +
                                    "[optional]")
                            .withLongOpt(OPT_META_CONTIGS)
                            .create()
            )
            .addOption(
                    OptionBuilder
                            .withArgName("file")
                            .hasArg(true)
                            .withDescription("(manual input) " +
                                    "Specifies sub-multiplexing rule and a path to barcodes file. " +
                                    "[one of M1-4 options should be specified]")
                            .withLongOpt(OPT_MODE_MULTIPLEX_LONG)
                            .create(OPT_MODE_MULTIPLEX_SHORT)
            )
            .addOption(
                    OptionBuilder
                            .withArgName("file")
                            .hasArg(true)
                            .withDescription("(manual input) " +
                                    "Specifies primer set. " +
                                    "[one of M1-4 options should be specified]")
                            .withLongOpt(OPT_MODE_PRIMER_LONG)
                            .create(OPT_MODE_PRIMER_SHORT)
            )
            .addOption(
                    OptionBuilder
                            .withArgName("mask1[:mask2]")
                            .hasArg(true)
                            .withDescription("(manual input) " +
                                    "Specifies positional UMI matching rule. " +
                                    "[one of M1-4 options should be specified]")
                            .withLongOpt(OPT_MODE_POSITIONAL_LONG)
                            .create(OPT_MODE_POSITIONAL_SHORT)
            )
            .addOption(
                    OptionBuilder
                            .withDescription("(manual input) " +
                                    "Tells that the sample is pre-processed and UMI " +
                                    "information is stored in the read header. " +
                                    "[one of M1-4 options should be specified]")
                            .withLongOpt(OPT_MODE_HEADER_LONG)
                            .create(OPT_MODE_HEADER_SHORT)
            )
                    //
                    // output
            .addOption(
                    OptionBuilder
                            .withArgName("path")
                            .hasArg(true)
                            .withDescription("Path to output. [default = \".\"]")
                            .withLongOpt(OPT_OUTPUT_LONG)
                            .create(OPT_OUTPUT_SHORT)
            )
                    //
                    // Advanced
            .addOption(
                    OptionBuilder
                            .withDescription("[advanced] Write an additional binary file with output.")
                            .withLongOpt(OPT_BINARY_OUTPUT)
                            .create()
            );
}