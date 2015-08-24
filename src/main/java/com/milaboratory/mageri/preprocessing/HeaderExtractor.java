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

package com.milaboratory.mageri.preprocessing;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.milaboratory.mageri.preprocessing.barcode.BarcodeSearcher;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public final class HeaderExtractor extends CheckoutProcessor<SequencingRead, CheckoutResult> {
    public HeaderExtractor(String sampleName) {
        super(new String[]{sampleName}, new BarcodeSearcher[]{null});
    }

    @Override
    public CheckoutResult checkoutImpl(SequencingRead sequencingRead) {
        NucleotideSQPair umiSQPair = extractUmiWithQual(sequencingRead.getDescription(0));

        if (umiSQPair == null) {
            return null;
        } else {
            return new SimpleCheckoutResult(sampleNames[0], umiSQPair);
        }
    }

    @Override
    public boolean isPairedEnd() {
        throw new NotImplementedException();
    }

    public static final String UMI_FIELD_ID = "UMI";
    public static final int UMI_QUAL_OFFSET = UMI_FIELD_ID.length() + 2;

    public static NucleotideSQPair extractUmiWithQual(String header) {
        for (String field : header.split("[ \t]")) {
            if (field.startsWith(UMI_FIELD_ID)) {
                String seq = field.split(":")[1];
                String qual = field.substring(UMI_QUAL_OFFSET + seq.length());
                return new NucleotideSQPair(seq, qual);
            }
        }

        return null;
    }
}
