package com.milaboratory.oncomigec.pipeline;

import com.milaboratory.oncomigec.pipeline.analysis.ProjectAnalysis;
import com.milaboratory.oncomigec.pipeline.input.Input;
import com.milaboratory.oncomigec.pipeline.input.InputParser;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@SuppressWarnings("static-access")
public final class Oncomigec {
    private static final Class ME = Oncomigec.class;
    public static final String MY_PATH = ME.getProtectionDomain().getCodeSource().getLocation().getFile(),
            MY_VERSION = ME.getPackage().getImplementationVersion();

    public static ProjectAnalysis loadAnalysis(File file) throws IOException, ClassNotFoundException {
        return (ProjectAnalysis) SerializationUtils.readObjectFromFile(file);
    }

    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new BasicParser();

        RuntimeParameters runtimeParameters = null;
        Presets presets = null;
        Input input = null;
        String outputFolder = null;

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

            runtimeParameters = new RuntimeParameters(numberOfThreads, readLimit, verbosity);

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Presets
            String instrument = "illumina";
            if (commandLine.hasOption(OPT_INSTRUMENT)) {
                instrument = commandLine.getOptionValue(OPT_INSTRUMENT);
            }
            presets = Presets.create(instrument, "multiplex"); // todo: implement library type

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

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Input
            if (commandLine.hasOption(OPT_INPUT_SHORT)) {
                input = new InputParser().parseJson(commandLine.getOptionValue(OPT_INPUT_SHORT));
            } else {
                throw new ParseException("Path to project json file should be provided.");
            }

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Output
            outputFolder = ".";
            if (commandLine.hasOption(OPT_OUTPUT_SHORT)) {
                outputFolder = commandLine.getOptionValue(OPT_OUTPUT_SHORT);
            }
            FileUtils.forceMkdir(new File(outputFolder));
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

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Run
        projectAnalysis.run();

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Write output
        projectAnalysis.write(outputFolder, true);
    }

    private static final String OPT_HELP_SHORT = "h", OPT_HELP_LONG = "help", OPT_VERSION_SHORT = "v", OPT_VERSION_LONG = "version",
            OPT_VERBOSITY = "verbosity", OPT_THREADS = "threads", OPT_LIMIT = "limit",
            OPT_INSTRUMENT = "instrument", OPT_IMPORT_PRESET = "import-preset", OPT_EXPORT_PRESET = "export-preset",
            OPT_INPUT_LONG = "input", OPT_INPUT_SHORT = "I",
            OPT_OUTPUT_LONG = "output-path", OPT_OUTPUT_SHORT = "O";

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
                            .withDescription("Sequencer type: " +
                                    "Illumina, " +
                                    "IonTorrent " +
                                    "or 454. " +
                                    "[default = Illumina]")
                            .withLongOpt(OPT_INSTRUMENT)
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
                    // output
            .addOption(
                    OptionBuilder
                            .withArgName("path")
                            .hasArg(true)
                            .withDescription("Path to output. [default = \".\"]")
                            .withLongOpt(OPT_OUTPUT_LONG)
                            .create(OPT_OUTPUT_SHORT)
            );
}