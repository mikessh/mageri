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

import com.milaboratory.oncomigec.core.ReadSpecific;

import java.io.InputStream;
import java.io.Serializable;

public class InputChunk implements ReadSpecific, Serializable {
    protected transient final InputStream inputStream1, inputStream2;
    protected final String name;
    protected final CheckoutRule checkoutRule;

    public InputChunk(InputStream inputStream1, InputStream inputStream2,
                      String name, CheckoutRule checkoutRule) {
        this.inputStream1 = inputStream1;
        this.inputStream2 = inputStream2;
        this.name = name;
        this.checkoutRule = checkoutRule;
    }

    public InputStream getInputStream1() {
        return inputStream1;
    }

    public InputStream getInputStream2() {
        return inputStream2;
    }

    public String getName() {
        return name;
    }

    public CheckoutRule getCheckoutRule() {
        return checkoutRule;
    }

    @Override
    public boolean isPairedEnd() {
        return inputStream2 != null;
    }

    @Override
    public String toString() {
        String out = name + "\n-paired:" + isPairedEnd() + "\n-checkout:";
        String[] tokens = checkoutRule.toString().split("\n");
        for (String token : tokens) {
            out += "\n-" + token;
        }
        return out;
    }
}
