package com.milaboratory.migec2.pipeline;

import com.milaboratory.migec2.core.assemble.misc.AssemblerParameters;
import com.milaboratory.migec2.core.consalign.misc.ConsensusAlignerParameters;
import com.milaboratory.migec2.core.correct.CorrectorParameters;
import com.milaboratory.migec2.core.haplotype.HaplotypeTreeParameters;
import com.milaboratory.migec2.preproc.demultiplex.entity.DemultiplexParameters;
import com.milaboratory.migec2.util.ParameterSet;
import com.milaboratory.migec2.util.Util;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.*;

public class MigecParameterSet implements ParameterSet {
    private final AssemblerParameters assemblerParameters;
    private final ConsensusAlignerParameters consensusAlignerParameters;
    private final CorrectorParameters correctorParameters;
    private final HaplotypeTreeParameters haplotypeTreeParameters;
    private final DemultiplexParameters demultiplexParameters;
    private final int minOverseq, forcedOverseq, minMigCount;
    private final byte readerUmiQualThreshold;
    private final boolean outputFasta, filterMismatchUmis;

    private static String DEDUCE_VERSION() {
        return MigecParameterSet.class.getPackage().getImplementationVersion();
    }

    private final static boolean TEST_VERSION;
    private final static String VERSION = (TEST_VERSION = (DEDUCE_VERSION() == null)) ? "TEST" : DEDUCE_VERSION();

    public MigecParameterSet() {
        this(AssemblerParameters.DEFAULT);
    }

    public MigecParameterSet(AssemblerParameters assemblerParameters) {
        this(assemblerParameters, ConsensusAlignerParameters.DEFAULT,
                CorrectorParameters.DEFAULT, HaplotypeTreeParameters.DEFAULT,
                DemultiplexParameters.DEFAULT,
                4, -1, 50, Util.PH33_LOW_QUAL, true, true);
    }

    public MigecParameterSet(AssemblerParameters assemblerParameters, ConsensusAlignerParameters consensusAlignerParameters,
                             CorrectorParameters correctorParameters, HaplotypeTreeParameters haplotypeTreeParameters,
                             DemultiplexParameters demultiplexParameters,
                             int minOverseq, int forcedOverseq, int minMigCount,
                             byte readerUmiQualThreshold, boolean filterMismatchUmis,
                             boolean outputFasta) {
        this.assemblerParameters = assemblerParameters;
        this.consensusAlignerParameters = consensusAlignerParameters;
        this.correctorParameters = correctorParameters;
        this.haplotypeTreeParameters = haplotypeTreeParameters;
        this.demultiplexParameters = demultiplexParameters;
        this.minMigCount = minMigCount;
        this.minOverseq = minOverseq;
        this.forcedOverseq = forcedOverseq;
        this.readerUmiQualThreshold = readerUmiQualThreshold;
        this.filterMismatchUmis = filterMismatchUmis;
        this.outputFasta = outputFasta;
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

    public DemultiplexParameters getDemultiplexParameters() {
        return demultiplexParameters;
    }

    public int getMinMigCount() {
        return minMigCount;
    }

    public int getMinOverseq() {
        return minOverseq;
    }

    public int getForcedOverseq() {
        return forcedOverseq;
    }

    public byte getReaderUmiQualThreshold() {
        return readerUmiQualThreshold;
    }

    public boolean filterMismatchUmis() {
        return filterMismatchUmis;
    }

    public boolean outputFasta() {
        return outputFasta;
    }

    public static MigecParameterSet loadFromFile(File xmlFile) throws JDOMException, IOException {
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

    public static MigecParameterSet readFromStream(InputStream iStream) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(iStream);
        return fromXml(document.getRootElement());
    }

    public static MigecParameterSet fromPreset(String presetName) {
        // todo: as resources
        switch (presetName.toUpperCase()) {
            case "ILLUMINA-EXOME":
                return new MigecParameterSet(AssemblerParameters.DEFAULT);
            case "TORRENT454-EXOME":
                return new MigecParameterSet(AssemblerParameters.TORRENT454);
            default:
                throw new IllegalArgumentException("Unknown parameter preset: " + presetName.toUpperCase());
        }
    }

    @Override
    public Element toXml() {
        Element e = new Element("MigecParameterSet");
        e.addContent(new Element("version").setText(VERSION));
        e.addContent(assemblerParameters.toXml());
        e.addContent(consensusAlignerParameters.toXml());
        e.addContent(correctorParameters.toXml());
        e.addContent(haplotypeTreeParameters.toXml());
        e.addContent(demultiplexParameters.toXml());
        e.addContent(new Element("minOverseq").setText(Integer.toString(minOverseq)));
        e.addContent(new Element("forcedOverseq").setText(Integer.toString(forcedOverseq)));
        e.addContent(new Element("minMigCount").setText(Integer.toString(minMigCount)));
        e.addContent(new Element("readerUmiQualThreshold").setText(Byte.toString(readerUmiQualThreshold)));
        e.addContent(new Element("filterMismatchUmis").setText(Boolean.toString(filterMismatchUmis)));
        e.addContent(new Element("outputFasta").setText(Boolean.toString(outputFasta)));
        return e;
    }

    public static MigecParameterSet fromXml(Element e) {
        //Extracting format information
        String format = e.getChildTextTrim("version");

        //Checking for compatibility
        if (!TEST_VERSION && !format.equals(VERSION))
            throw new RuntimeException("Unsupported parameters format version.");

        return new MigecParameterSet(
                AssemblerParameters.fromXml(e),
                ConsensusAlignerParameters.fromXml(e),
                CorrectorParameters.fromXml(e),
                HaplotypeTreeParameters.fromXml(e),
                DemultiplexParameters.fromXml(e),
                Integer.parseInt(e.getChildTextTrim("minOverseq")),
                Integer.parseInt(e.getChildTextTrim("forcedOverseq")),
                Integer.parseInt(e.getChildTextTrim("minMigCount")),
                Byte.parseByte(e.getChildTextTrim("readerUmiQualThreshold")),
                Boolean.parseBoolean(e.getChildTextTrim("filterMismatchUmis")),
                Boolean.parseBoolean(e.getChildTextTrim("outputFasta"))
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MigecParameterSet that = (MigecParameterSet) o;

        if (filterMismatchUmis != that.filterMismatchUmis) return false;
        if (forcedOverseq != that.forcedOverseq) return false;
        if (minMigCount != that.minMigCount) return false;
        if (minOverseq != that.minOverseq) return false;
        if (outputFasta != that.outputFasta) return false;
        if (readerUmiQualThreshold != that.readerUmiQualThreshold) return false;
        if (!assemblerParameters.equals(that.assemblerParameters)) return false;
        if (!consensusAlignerParameters.equals(that.consensusAlignerParameters)) return false;
        if (!correctorParameters.equals(that.correctorParameters)) return false;
        if (!demultiplexParameters.equals(that.demultiplexParameters)) return false;
        if (!haplotypeTreeParameters.equals(that.haplotypeTreeParameters)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = assemblerParameters.hashCode();
        result = 31 * result + consensusAlignerParameters.hashCode();
        result = 31 * result + correctorParameters.hashCode();
        result = 31 * result + haplotypeTreeParameters.hashCode();
        result = 31 * result + demultiplexParameters.hashCode();
        result = 31 * result + minOverseq;
        result = 31 * result + forcedOverseq;
        result = 31 * result + minMigCount;
        result = 31 * result + (int) readerUmiQualThreshold;
        result = 31 * result + (outputFasta ? 1 : 0);
        result = 31 * result + (filterMismatchUmis ? 1 : 0);
        return result;
    }
}
