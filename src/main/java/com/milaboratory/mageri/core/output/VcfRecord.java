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

package com.milaboratory.mageri.core.output;

import com.milaboratory.mageri.misc.Record;

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
