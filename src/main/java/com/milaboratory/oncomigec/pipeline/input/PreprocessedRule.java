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
 * Last modified on 16.3.2015 by mikesh
 */

package com.milaboratory.oncomigec.pipeline.input;

import com.milaboratory.oncomigec.preproc.demultiplex.processor.CheckoutProcessor;
import com.milaboratory.oncomigec.preproc.demultiplex.processor.HeaderExtractor;

import java.util.ArrayList;
import java.util.List;

public final class PreprocessedRule extends CheckoutRule {
    private final String sampleName;

    public PreprocessedRule(String sampleName) {
        this.sampleName = sampleName;
    }

    @Override
    public CheckoutProcessor getProcessor() {
        return new HeaderExtractor(sampleName);
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
        return "preprocessed\n-submultiplex:" + hasSubMultiplexing() + "\n-samples:" + sampleName;
    }
}
