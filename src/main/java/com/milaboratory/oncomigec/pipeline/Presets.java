package com.milaboratory.oncomigec.pipeline;

import com.milaboratory.oncomigec.core.assemble.misc.AssemblerParameters;
import com.milaboratory.oncomigec.core.consalign.misc.ConsensusAlignerParameters;
import com.milaboratory.oncomigec.core.correct.CorrectorParameters;
import com.milaboratory.oncomigec.core.haplotype.HaplotypeTreeParameters;
import com.milaboratory.oncomigec.core.io.misc.PreprocessorParameters;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.DemultiplexParameters;
import com.milaboratory.oncomigec.util.ParameterSet;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.*;

public class Presets implements ParameterSet {
    public static final Presets DEFAULT = new Presets();

    private final PreprocessorParameters preprocessorParameters;
    private final AssemblerParameters assemblerParameters;
    private final ConsensusAlignerParameters consensusAlignerParameters;
    private final CorrectorParameters correctorParameters;
    private final HaplotypeTreeParameters haplotypeTreeParameters;
    private final DemultiplexParameters demultiplexParameters;

    private static String DEDUCE_VERSION() {
        return Presets.class.getPackage().getImplementationVersion();
    }

    private final static boolean TEST_VERSION;
    private final static String VERSION = (TEST_VERSION = (DEDUCE_VERSION() == null)) ? "TEST" : DEDUCE_VERSION();

    private Presets() {
        this(AssemblerParameters.DEFAULT);
    }

    public Presets(AssemblerParameters assemblerParameters) {
        this(DemultiplexParameters.DEFAULT,
                PreprocessorParameters.DEFAULT,
                assemblerParameters,
                ConsensusAlignerParameters.DEFAULT,
                CorrectorParameters.DEFAULT,
                HaplotypeTreeParameters.DEFAULT);
    }

    public Presets(DemultiplexParameters demultiplexParameters,
                   PreprocessorParameters preprocessorParameters,
                   AssemblerParameters assemblerParameters,
                   ConsensusAlignerParameters consensusAlignerParameters,
                   CorrectorParameters correctorParameters,
                   HaplotypeTreeParameters haplotypeTreeParameters) {
        this.preprocessorParameters = preprocessorParameters;
        this.assemblerParameters = assemblerParameters;
        this.consensusAlignerParameters = consensusAlignerParameters;
        this.correctorParameters = correctorParameters;
        this.haplotypeTreeParameters = haplotypeTreeParameters;
        this.demultiplexParameters = demultiplexParameters;
    }


    public DemultiplexParameters getDemultiplexParameters() {
        return demultiplexParameters;
    }

    public PreprocessorParameters getPreprocessorParameters() {
        return preprocessorParameters;
    }

    public AssemblerParameters getAssemblerParameters() {
        return assemblerParameters;
    }

    public ConsensusAlignerParameters getConsensusAlignerParameters() {
        return consensusAlignerParameters;
    }

    public CorrectorParameters getCorrectorParameters() {
        return correctorParameters;
    }

    public HaplotypeTreeParameters getHaplotypeTreeParameters() {
        return haplotypeTreeParameters;
    }

    public static Presets loadFromFile(File xmlFile) throws JDOMException, IOException {
        return readFromStream(new FileInputStream(xmlFile));
    }

    public void writeToStream(OutputStream oStream) throws IOException {
        Element e = this.toXml();
        Document document = new Document(e);
        new XMLOutputter(Format.getPrettyFormat()).output(document, oStream);
    }

    public void writeToFile(File file) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        writeToStream(fileOutputStream);
        fileOutputStream.close();
    }

    public static Presets readFromStream(InputStream iStream) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(iStream);
        return fromXml(document.getRootElement());
    }

    public static Presets create(String instrument, String libraryType) {
        // todo: extend

        AssemblerParameters assemblerParameters;
        switch (instrument.toUpperCase()) {
            case "ILLUMINA":
                assemblerParameters = AssemblerParameters.DEFAULT;
                break;
            case "454":
            case "IONTORRENT":
                assemblerParameters = AssemblerParameters.TORRENT454;
                break;
            default:
                throw new IllegalArgumentException("Unknown instrument: " + instrument);
        }

        switch (libraryType.toUpperCase()) {
            case "MULTIPLEX":
                break;
            case "TRAPPING":
                break;
            case "WALKING":
                break;
            default:
                throw new IllegalArgumentException("Unknown library type preset: " + libraryType);
        }

        return new Presets(assemblerParameters);
    }

    @Override
    public Element toXml() {
        Element e = new Element("OncomigecPresets");
        e.addContent(new Element("version").setText(VERSION));
        e.addContent(demultiplexParameters.toXml());
        e.addContent(preprocessorParameters.toXml());
        e.addContent(consensusAlignerParameters.toXml());
        e.addContent(correctorParameters.toXml());
        e.addContent(haplotypeTreeParameters.toXml());
        return e;
    }

    public static Presets fromXml(Element e) {
        //Extracting format information
        String format = e.getChildTextTrim("version");

        //Checking for compatibility
        if (!TEST_VERSION && !format.equals(VERSION))
            throw new RuntimeException("Unsupported parameters format version.");

        return new Presets(
                DemultiplexParameters.fromXml(e),
                PreprocessorParameters.fromXml(e),
                AssemblerParameters.fromXml(e),
                ConsensusAlignerParameters.fromXml(e),
                CorrectorParameters.fromXml(e),
                HaplotypeTreeParameters.fromXml(e)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Presets presets = (Presets) o;

        if (!assemblerParameters.equals(presets.assemblerParameters)) return false;
        if (!consensusAlignerParameters.equals(presets.consensusAlignerParameters)) return false;
        if (!correctorParameters.equals(presets.correctorParameters)) return false;
        if (!demultiplexParameters.equals(presets.demultiplexParameters)) return false;
        if (!haplotypeTreeParameters.equals(presets.haplotypeTreeParameters)) return false;
        if (!preprocessorParameters.equals(presets.preprocessorParameters)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = preprocessorParameters.hashCode();
        result = 31 * result + assemblerParameters.hashCode();
        result = 31 * result + consensusAlignerParameters.hashCode();
        result = 31 * result + correctorParameters.hashCode();
        result = 31 * result + haplotypeTreeParameters.hashCode();
        result = 31 * result + demultiplexParameters.hashCode();
        return result;
    }
}
