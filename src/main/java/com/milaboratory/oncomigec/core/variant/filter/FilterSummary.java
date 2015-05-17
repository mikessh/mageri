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

package com.milaboratory.oncomigec.core.variant.filter;

import com.milaboratory.oncomigec.core.variant.Variant;
import com.milaboratory.oncomigec.core.variant.VariantCaller;

import java.io.Serializable;

public class FilterSummary implements Serializable {
    private final VariantCaller parent;
    private final boolean[] filterMask;
    private final boolean passed;

    public static final FilterSummary DUMMY = new FilterSummary();

    private FilterSummary() {
        this.parent = null;
        this.filterMask = null;
        this.passed = true;
    }

    public FilterSummary(VariantCaller variantCaller,
                         Variant variant) {
        this.parent = variantCaller;
        this.filterMask = new boolean[variantCaller.getFilterCount()];
        boolean passed = true;
        for (int i = 0; i < filterMask.length; i++) {
            filterMask[i] = variantCaller.getFilter(i).pass(variant);
            if (!filterMask[i])
                passed = false;
        }
        this.passed = passed;
    }

    public boolean passed() {
        return passed;
    }

    @Override
    public String toString() {
        if (passed) {
            return ".";
        }

        String result = "";

        for (int i = 0; i < filterMask.length; i++) {
            if (!filterMask[i])
                result += ";" + parent.getFilter(i).getId();
        }

        return result.substring(1);
    }
}
