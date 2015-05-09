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
 * Last modified on 12.4.2015 by mikesh
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
        if (passed)
            return "PASSED";

        String result = "";

        for (int i = 0; i < filterMask.length; i++) {
            if (!filterMask[i])
                result += ";" + parent.getFilter(i).getId();
        }

        return result.substring(1);
    }
}
