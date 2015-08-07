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

package com.milaboratory.mageri.pipeline.input;

import com.milaboratory.mageri.preprocessing.CheckoutProcessor;
import com.milaboratory.mageri.preprocessing.PPositionalExtractor;
import com.milaboratory.mageri.preprocessing.SPositionalExtractor;

import java.util.ArrayList;
import java.util.List;

public class PositionalRule extends CheckoutRule {
    private final String sampleName, mask1, mask2;
    private final boolean paired;

    public PositionalRule(String sampleName,
                          String mask1, String mask2,
                          boolean paired) {
        this.sampleName = sampleName;
        this.mask1 = mask1;
        this.mask2 = mask2;
        this.paired = paired;
    }

    @Override
    public CheckoutProcessor getProcessor() {
        return mask2 == null ?
                (paired ?
                        new PPositionalExtractor(sampleName, mask1) :
                        new SPositionalExtractor(sampleName, mask1)) :
                (paired ?
                        new PPositionalExtractor(sampleName, mask1, mask2) :
                        new SPositionalExtractor(sampleName, mask1));
    }

    @Override
    public List<String> getSampleNames() {
        List<String> sampleNames = new ArrayList<>();
        sampleNames.add(sampleName);
        return sampleNames;
    }

    @Override
    public boolean hasSubMultiplexing() {
        return false;
    }

    @Override
    public String toString() {
        return "preprocessed_rule\n-submultiplex:" + hasSubMultiplexing() + "\n-samples:" + sampleName;
    }
}
