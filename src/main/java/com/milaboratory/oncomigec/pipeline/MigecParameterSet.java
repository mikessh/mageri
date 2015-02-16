package com.milaboratory.oncomigec.pipeline;

import com.milaboratory.oncomigec.core.assemble.misc.AssemblerParameters;
import com.milaboratory.oncomigec.core.consalign.misc.ConsensusAlignerParameters;
import com.milaboratory.oncomigec.core.correct.CorrectorParameters;
import com.milaboratory.oncomigec.core.haplotype.HaplotypeTreeParameters;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.DemultiplexParameters;
import com.milaboratory.oncomigec.util.ParameterSet;
import com.milaboratory.oncomigec.util.Util;
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
    private final int minOverseq, defaultOverseq, minUniqueUmis;
    private final byte readerUmiQualThreshold;
    private final boolean outputFasta, forceOverseq, filterMismatchUmis;
    private final double umiMismatchFilterRatio;

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
                Util.PH33_LOW_QUAL,
                4, false, 4, 1,
                true, 8.0,
                true);
    }

    public MigecParameterSet(AssemblerParameters assemblerParameters, ConsensusAlignerParameters consensusAlignerParameters,
                             CorrectorParameters correctorParameters, HaplotypeTreeParameters haplotypeTreeParameters,
                             DemultiplexParameters demultiplexParameters,
                             byte readerUmiQualThreshold,
                             int minOverseq, boolean forceOverseq, int defaultOverseq, int minUniqueUmis,
                             boolean filterMismatchUmis, double umiMismatchFilterRatio,
                             boolean outputFasta) {
        this.assemblerParameters = assemblerParameters;
        this.consensusAlignerParameters = consensusAlignerParameters;
        this.correctorParameters = correctorParameters;
        this.haplotypeTreeParameters = haplotypeTreeParameters;
        this.demultiplexParameters = demultiplexParameters;

        this.readerUmiQualThreshold = readerUmiQualThreshold;

        this.minOverseq = minOverseq;
        this.forceOverseq = forceOverseq;
        this.defaultOverseq = defaultOverseq;
        this.minUniqueUmis = minUniqueUmis;

        this.filterMismatchUmis = filterMismatchUmis;
        this.umiMismatchFilterRatio = umiMismatchFilterRatio;

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

    public int getMinUniqueUmis() {
        return minUniqueUmis;
    }

    public int getMinOverseq() {
        return minOverseq;
    }

    public boolean forceOverseq() {
        return forceOverseq;
    }

    public int getDefaultOverseq() {
        return defaultOverseq;
    }

    public byte getReaderUmiQualThreshold() {
        return readerUmiQualThreshold;
    }

    public boolean filterMismatchUmis() {
        return filterMismatchUmis;
    }

    public double getUmiMismatchFilterRatio() {
        return umiMismatchFilterRatio;
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

        e.addContent(new Element("readerUmiQualThreshold").setText(Byte.toString(readerUmiQualThreshold)));

        e.addContent(new Element("minOverseq").setText(Integer.toString(minOverseq)));
        e.addContent(new Element("forceOverseq").setText(Boolean.toString(forceOverseq)));
        e.addContent(new Element("defaultOverseq").setText(Integer.toString(defaultOverseq)));
        e.addContent(new Element("minUniqueUmis").setText(Integer.toString(minUniqueUmis)));

        e.addContent(new Element("filterMismatchUmis").setText(Boolean.toString(filterMismatchUmis)));
        e.addContent(new Element("umiMismatchFilterRatio").setText(Double.toString(umiMismatchFilterRatio)));

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

                Byte.parseByte(e.getChildTextTrim("readerUmiQualThreshold")),

                Integer.parseInt(e.getChildTextTrim("minOverseq")),
                Boolean.parseBoolean(e.getChildTextTrim("forceOverseq")),
                Integer.parseInt(e.getChildTextTrim("defaultOverseq")),
                Integer.parseInt(e.getChildTextTrim("minUniqueUmis")),

                Boolean.parseBoolean(e.getChildTextTrim("filterMismatchUmis")),
                Double.parseDouble(e.getChildTextTrim("umiMismatchFilterRatio")),

                Boolean.parseBoolean(e.getChildTextTrim("outputFasta"))
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MigecParameterSet that = (MigecParameterSet) o;

        if (defaultOverseq != that.defaultOverseq) return false;
        if (filterMismatchUmis != that.filterMismatchUmis) return false;
        if (forceOverseq != that.forceOverseq) return false;
        if (minOverseq != that.minOverseq) return false;
        if (minUniqueUmis != that.minUniqueUmis) return false;
        if (outputFasta != that.outputFasta) return false;
        if (readerUmiQualThreshold != that.readerUmiQualThreshold) return false;
        if (Double.compare(that.umiMismatchFilterRatio, umiMismatchFilterRatio) != 0) return false;
        if (!assemblerParameters.equals(that.assemblerParameters)) return false;
        if (!consensusAlignerParameters.equals(that.consensusAlignerParameters)) return false;
        if (!correctorParameters.equals(that.correctorParameters)) return false;
        if (!demultiplexParameters.equals(that.demultiplexParameters)) return false;
        if (!haplotypeTreeParameters.equals(that.haplotypeTreeParameters)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = assemblerParameters.hashCode();
        result = 31 * result + consensusAlignerParameters.hashCode();
        result = 31 * result + correctorParameters.hashCode();
        result = 31 * result + haplotypeTreeParameters.hashCode();
        result = 31 * result + demultiplexParameters.hashCode();
        result = 31 * result + minOverseq;
        result = 31 * result + defaultOverseq;
        result = 31 * result + minUniqueUmis;
        result = 31 * result + (int) readerUmiQualThreshold;
        result = 31 * result + (outputFasta ? 1 : 0);
        result = 31 * result + (forceOverseq ? 1 : 0);
        result = 31 * result + (filterMismatchUmis ? 1 : 0);
        temp = Double.doubleToLongBits(umiMismatchFilterRatio);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
