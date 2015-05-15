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
package com.milaboratory.oncomigec.preprocessing;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.preprocessing.barcode.BarcodeSearcherResult;

public final class PCheckoutResult extends CheckoutResult {
    private final boolean orientation, masterFirst;
    private final BarcodeSearcherResult slaveResult;

    public PCheckoutResult(int sampleId, String sampleName, boolean orientation, boolean masterFirst,
                           BarcodeSearcherResult masterResult, BarcodeSearcherResult slaveResult) {
        super(sampleId, sampleName, masterResult);
        this.slaveResult = slaveResult;
        this.orientation = orientation;
        this.masterFirst = masterFirst;
    }

    @Override
    public boolean getOrientation() {
        return orientation;
    }

    @Override
    public boolean getMasterFirst() {
        return masterFirst;
    }

    // Should be removed - null checkout result is returned when slave is not found
    // If no slave search is performed, BarcodeSearcherResult.hasAdapterMatch() function can handle all checks
    @Deprecated
    public boolean slaveFound() {
        return slaveResult != null;
    }

    @Override
    public NucleotideSequence getUmi() {
        if (slaveFound()) {
            return masterResult.getUmi().concatenate(slaveResult.getUmi());
        } else {
            return masterResult.getUmi();
        }
    }

    @Override
    public boolean isGood(byte umiQualThreshold) {
        return slaveFound() &&
                (byte) Math.min(masterResult.getUmiWorstQual(), slaveResult.getUmiWorstQual()) >= umiQualThreshold;
    }

    public BarcodeSearcherResult getSlaveResult() {
        return slaveResult;
    }
}
