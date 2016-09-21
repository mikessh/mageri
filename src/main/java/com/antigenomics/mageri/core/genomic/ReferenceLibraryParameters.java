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

package com.antigenomics.mageri.core.genomic;

import com.antigenomics.mageri.misc.ParameterSet;
import org.jdom.Element;

public class ReferenceLibraryParameters implements ParameterSet {
    private final boolean splitLargeReferences;
    private final int maxReferenceLength, readLength;

    public static final ReferenceLibraryParameters DEFAULT = new ReferenceLibraryParameters(
            true, 1000, 100),
            DUMMY = new ReferenceLibraryParameters(
                    false, -1, -1);

    public ReferenceLibraryParameters(boolean splitLargeReferences, int maxReferenceLength, int readLength) {
        this.splitLargeReferences = splitLargeReferences;
        this.maxReferenceLength = maxReferenceLength;
        this.readLength = readLength;
    }

    public boolean splitLargeReferences() {
        return splitLargeReferences;
    }

    public int getMaxReferenceLength() {
        return maxReferenceLength;
    }

    public int getReadLength() {
        return readLength;
    }

    public ReferenceLibraryParameters withSplitLargeReferences(boolean splitLargeReferences) {
        return new ReferenceLibraryParameters(splitLargeReferences, maxReferenceLength, readLength);
    }

    public ReferenceLibraryParameters withMaxReferenceLength(int maxReferenceLength) {
        return new ReferenceLibraryParameters(splitLargeReferences, maxReferenceLength, readLength);
    }

    public ReferenceLibraryParameters withReadLength(int readLength) {
        return new ReferenceLibraryParameters(splitLargeReferences, maxReferenceLength, readLength);
    }

    @Override
    public Element toXml() {
        Element e = new Element("ReferenceLibraryParameters");
        e.addContent(new Element("splitLargeReferences").setText(Boolean.toString(splitLargeReferences)));
        e.addContent(new Element("maxReferenceLength").setText(Integer.toString(maxReferenceLength)));
        e.addContent(new Element("readLength").setText(Integer.toString(readLength)));
        return e;
    }

    public static ReferenceLibraryParameters fromXml(Element parent) {
        Element e = parent.getChild("ReferenceLibraryParameters");
        return new ReferenceLibraryParameters(
                Boolean.parseBoolean(e.getChildTextTrim("splitLargeReferences")),
                Integer.parseInt(e.getChildTextTrim("maxReferenceLength")),
                Integer.parseInt(e.getChildTextTrim("readLength"))
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReferenceLibraryParameters that = (ReferenceLibraryParameters) o;

        if (splitLargeReferences != that.splitLargeReferences) return false;
        if (maxReferenceLength != that.maxReferenceLength) return false;
        return readLength == that.readLength;

    }

    @Override
    public int hashCode() {
        int result = (splitLargeReferences ? 1 : 0);
        result = 31 * result + maxReferenceLength;
        result = 31 * result + readLength;
        return result;
    }
}
