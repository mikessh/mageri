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

package com.milaboratory.oncomigec.pipeline.input;

import com.milaboratory.oncomigec.preprocessing.barcode.BarcodeListParser;
import com.milaboratory.oncomigec.preprocessing.DemultiplexParameters;
import com.milaboratory.oncomigec.preprocessing.CheckoutProcessor;

import java.io.IOException;
import java.util.List;

public abstract class AdapterRule extends CheckoutRule {
    protected final String index;
    protected final List<String> barcodes;
    protected final boolean paired;
    private DemultiplexParameters demultiplexParameters = DemultiplexParameters.DEFAULT;

    public AdapterRule(String index,
                       List<String> barcodes, boolean paired) throws IOException {
        this.index = index;
        this.paired = paired;
        this.barcodes = prepareBarcodes(barcodes);
    }

    protected abstract List<String> prepareBarcodes(List<String> barcodes);

    @Override
    public CheckoutProcessor getProcessor() {
        return paired ? BarcodeListParser.generatePCheckoutProcessor(barcodes, demultiplexParameters) :
                BarcodeListParser.generateSCheckoutProcessor(barcodes, demultiplexParameters);
    }

    @Override
    public abstract boolean hasSubMultiplexing();

    @Override
    public String toString() {
        String out = "adapter_rule\n-submultiplex:" + hasSubMultiplexing() + "\n-samples:";
        for (String str : getSampleNames()) {
            out += "\n--" + str;
        }
        return out;
    }
}
