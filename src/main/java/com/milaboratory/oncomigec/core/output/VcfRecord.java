/*
 * Copyright 2013-2015 Mikhail Shugay (mikhail.shugay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last modified on 17.4.2015 by mikesh
 */

package com.milaboratory.oncomigec.core.output;

import com.milaboratory.oncomigec.misc.Record;

public class VcfRecord implements Record {
    private final String chromosome, id,
            referenceBase, alternativeBase,
            filterString, infoString,
            formatKeyString, formatValueString;
    private final int position, quality;

    public VcfRecord(String chromosome, int position, String id,
                     String referenceBase, String alternativeBase,
                     int quality, String filterString,
                     String infoString) {
        this(chromosome, position, id, referenceBase, alternativeBase, quality, filterString, infoString,
                "GT", "0/0");
    }

    public VcfRecord(String chromosome, int position, String id,
                     String referenceBase, String alternativeBase,
                     int quality, String filterString,
                     String infoString, String formatKeyString, String formatValueString) {
        this.chromosome = chromosome;
        this.id = id;
        this.referenceBase = referenceBase;
        this.alternativeBase = alternativeBase;
        this.filterString = filterString;
        this.infoString = infoString;
        this.formatKeyString = formatKeyString;
        this.formatValueString = formatValueString;
        this.position = position;
        this.quality = quality;
    }

    @Override
    public String toString() {
        return chromosome + "\t" + position + "\t" + id + "\t" +
                referenceBase + "\t" + alternativeBase + "\t" +
                quality + "\t" + filterString + "\t" +
                infoString + "\t" + formatKeyString + "\t" + formatValueString;
    }
}
