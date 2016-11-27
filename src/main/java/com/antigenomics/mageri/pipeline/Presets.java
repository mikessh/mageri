/*
 * Copyright 2014-2016 Mikhail Shugay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.antigenomics.mageri.pipeline;

import com.antigenomics.mageri.core.assemble.AssemblerParameters;
import com.antigenomics.mageri.core.genomic.ReferenceLibraryParameters;
import com.antigenomics.mageri.core.input.PreprocessorParameters;
import com.antigenomics.mageri.core.variant.VariantCallerParameters;
import com.antigenomics.mageri.misc.ParameterSet;
import com.antigenomics.mageri.preprocessing.DemultiplexParameters;
import com.antigenomics.mageri.core.mapping.ConsensusAlignerParameters;
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
    private final ReferenceLibraryParameters referenceLibraryParameters;
    private final ConsensusAlignerParameters consensusAlignerParameters;
    private final VariantCallerParameters variantCallerParameters;
    private final DemultiplexParameters demultiplexParameters;

    private final Platform platform;
    private final LibraryType libraryType;

    private static String DEDUCE_VERSION() {
        return Presets.class.getPackage().getImplementationVersion();
    }

    private final static boolean TEST_VERSION;
    private final static String VERSION = (TEST_VERSION = (DEDUCE_VERSION() == null)) ? "TEST" : DEDUCE_VERSION();

    public Presets() {
        this(Platform.ILLUMINA,
                LibraryType.A,
                DemultiplexParameters.DEFAULT,
                PreprocessorParameters.DEFAULT,
                AssemblerParameters.DEFAULT,
                ReferenceLibraryParameters.DEFAULT,
                ConsensusAlignerParameters.DEFAULT,
                VariantCallerParameters.DEFAULT);
    }

    public Presets(Platform platform,
                   LibraryType libraryType,
                   DemultiplexParameters demultiplexParameters,
                   PreprocessorParameters preprocessorParameters,
                   AssemblerParameters assemblerParameters,
                   ReferenceLibraryParameters referenceLibraryParameters,
                   ConsensusAlignerParameters consensusAlignerParameters,
                   VariantCallerParameters variantCallerParameters) {
        this.platform = platform;
        this.libraryType = libraryType;
        this.preprocessorParameters = preprocessorParameters;
        this.assemblerParameters = assemblerParameters;
        this.referenceLibraryParameters = referenceLibraryParameters;
        this.consensusAlignerParameters = consensusAlignerParameters;
        this.variantCallerParameters = variantCallerParameters;
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

    public ReferenceLibraryParameters getReferenceLibraryParameters() {
        return referenceLibraryParameters;
    }

    public ConsensusAlignerParameters getConsensusAlignerParameters() {
        return consensusAlignerParameters;
    }

    public VariantCallerParameters getVariantCallerParameters() {
        return variantCallerParameters;
    }

    public Platform getPlatform() {
        return platform;
    }

    public LibraryType getLibraryType() {
        return libraryType;
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

    public static Presets create(String platformStr, String libraryTypeStr) {
        DemultiplexParameters demultiplexParameters = DemultiplexParameters.DEFAULT;
        PreprocessorParameters preprocessorParameters = PreprocessorParameters.DEFAULT;
        ReferenceLibraryParameters referenceLibraryParameters = ReferenceLibraryParameters.DEFAULT;
        ConsensusAlignerParameters consensusAlignerParameters = ConsensusAlignerParameters.DEFAULT;
        VariantCallerParameters variantCallerParameters;
        AssemblerParameters assemblerParameters;

        LibraryType libraryType = LibraryType.valueOf(libraryTypeStr.toUpperCase());
        Platform platform = Platform.valueOf(platformStr.toUpperCase());

        switch (platform) {
            case ILLUMINA:
                assemblerParameters = AssemblerParameters.DEFAULT;
                break;
            case ROCHE454:
            case IONTORRENT:
                assemblerParameters = AssemblerParameters.TORRENT454;
                break;
            default:
                throw new IllegalArgumentException("Unknown platform: " + platformStr);
        }

        switch (libraryType) {
            case A:
                variantCallerParameters = VariantCallerParameters.DEFAULT.withShouldPropagate(false);
                break;
            case L:
                variantCallerParameters = VariantCallerParameters.DEFAULT.withShouldPropagate(true);
                break;
            default:
                throw new IllegalArgumentException("Unknown library type: " + libraryTypeStr);
        }

        return new Presets(platform, libraryType,
                demultiplexParameters, preprocessorParameters, assemblerParameters,
                referenceLibraryParameters,
                consensusAlignerParameters, variantCallerParameters);
    }

    public Presets withPreprocessorParameters(PreprocessorParameters preprocessorParameters) {
        return new Presets(platform, libraryType,
                demultiplexParameters, preprocessorParameters,
                assemblerParameters,
                referenceLibraryParameters, consensusAlignerParameters, variantCallerParameters);
    }

    public Presets withAssemblerParameters(AssemblerParameters assemblerParameters) {
        return new Presets(platform, libraryType,
                demultiplexParameters, preprocessorParameters,
                assemblerParameters,
                referenceLibraryParameters, consensusAlignerParameters, variantCallerParameters);
    }

    public Presets withConsensusAlignerParameters(ConsensusAlignerParameters consensusAlignerParameters) {
        return new Presets(platform, libraryType,
                demultiplexParameters, preprocessorParameters,
                assemblerParameters,
                referenceLibraryParameters, consensusAlignerParameters, variantCallerParameters);
    }

    public Presets withVariantCallerParameters(VariantCallerParameters variantCallerParameters) {
        return new Presets(platform, libraryType,
                demultiplexParameters, preprocessorParameters,
                assemblerParameters,
                referenceLibraryParameters, consensusAlignerParameters, variantCallerParameters);
    }

    public Presets withDemultiplexParameters(DemultiplexParameters demultiplexParameters) {
        return new Presets(platform, libraryType,
                demultiplexParameters, preprocessorParameters,
                assemblerParameters,
                referenceLibraryParameters, consensusAlignerParameters, variantCallerParameters);
    }

    public Presets withReferenceLibraryParameters(ReferenceLibraryParameters referenceLibraryParameters) {
        return new Presets(platform, libraryType,
                demultiplexParameters, preprocessorParameters,
                assemblerParameters,
                referenceLibraryParameters, consensusAlignerParameters, variantCallerParameters);
    }

    public Presets withPlatform(Platform platform) {
        return new Presets(platform, libraryType,
                demultiplexParameters, preprocessorParameters,
                assemblerParameters,
                referenceLibraryParameters, consensusAlignerParameters, variantCallerParameters);
    }

    public Presets withLibraryType(LibraryType libraryType) {
        return new Presets(platform, libraryType,
                demultiplexParameters, preprocessorParameters,
                assemblerParameters,
                referenceLibraryParameters, consensusAlignerParameters, variantCallerParameters);
    }

    @Override
    public Element toXml() {
        Element e = new Element("MageriPresets");
        e.addContent(new Element("version").setText(VERSION));
        e.addContent(new Element("platform").setText(platform.toString()));
        e.addContent(new Element("libraryType").setText(libraryType.toString()));
        e.addContent(demultiplexParameters.toXml());
        e.addContent(preprocessorParameters.toXml());
        e.addContent(assemblerParameters.toXml());
        e.addContent(referenceLibraryParameters.toXml());
        e.addContent(consensusAlignerParameters.toXml());
        e.addContent(variantCallerParameters.toXml());
        return e;
    }

    public static Presets fromXml(Element e) {
        //Extracting format information
        String format = e.getChildTextTrim("version"),
                platform = e.getChildTextTrim("platform"),
                libraryType = e.getChildTextTrim("libraryType");

        //Checking for compatibility
        if (!TEST_VERSION && !format.equals(VERSION))
            throw new RuntimeException("Unsupported parameters format version.");

        return new Presets(
                Platform.valueOf(platform),
                LibraryType.valueOf(libraryType),
                DemultiplexParameters.fromXml(e),
                PreprocessorParameters.fromXml(e),
                AssemblerParameters.fromXml(e),
                ReferenceLibraryParameters.fromXml(e),
                ConsensusAlignerParameters.fromXml(e),
                VariantCallerParameters.fromXml(e)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Presets presets = (Presets) o;

        if (!preprocessorParameters.equals(presets.preprocessorParameters)) return false;
        if (!assemblerParameters.equals(presets.assemblerParameters)) return false;
        if (!referenceLibraryParameters.equals(presets.referenceLibraryParameters)) return false;
        if (!consensusAlignerParameters.equals(presets.consensusAlignerParameters)) return false;
        if (!variantCallerParameters.equals(presets.variantCallerParameters)) return false;
        if (!demultiplexParameters.equals(presets.demultiplexParameters)) return false;
        if (platform != presets.platform) return false;
        return libraryType == presets.libraryType;

    }

    @Override
    public int hashCode() {
        int result = preprocessorParameters.hashCode();
        result = 31 * result + assemblerParameters.hashCode();
        result = 31 * result + referenceLibraryParameters.hashCode();
        result = 31 * result + consensusAlignerParameters.hashCode();
        result = 31 * result + variantCallerParameters.hashCode();
        result = 31 * result + demultiplexParameters.hashCode();
        result = 31 * result + platform.hashCode();
        result = 31 * result + libraryType.hashCode();
        return result;
    }
}
