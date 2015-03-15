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

import com.milaboratory.oncomigec.preproc.demultiplex.config.BarcodeListParser;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.DemultiplexParameters;
import com.milaboratory.oncomigec.preproc.demultiplex.processor.CheckoutProcessor;
import com.sun.istack.internal.NotNull;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public final class SubMultiplexRule implements CheckoutRule {
    private final List<String> barcodes;
    private final boolean paired;
    private DemultiplexParameters demultiplexParameters = DemultiplexParameters.DEFAULT;

    public SubMultiplexRule(@NotNull String barcodesFileName, boolean paired) throws IOException {
        this.barcodes = FileUtils.readLines(new File(barcodesFileName));
        this.paired = paired;
    }

    @Override
    public CheckoutProcessor getProcessor() {
        return paired ? BarcodeListParser.generatePCheckoutProcessor(barcodes, demultiplexParameters) :
                BarcodeListParser.generateSCheckoutProcessor(barcodes, demultiplexParameters);
    }

    public DemultiplexParameters getDemultiplexParameters() {
        return demultiplexParameters;
    }

    public void setDemultiplexParameters(DemultiplexParameters demultiplexParameters) {
        this.demultiplexParameters = demultiplexParameters;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getSampleNames() {
        // parse list every time
        // but who knows, maybe demultiplex parameters will tell
        // to skip some samples in future imp
        return Collections.unmodifiableList(getProcessor().getSampleNames());
    }

    @Override
    public boolean hasSubMultiplexing() {
        return true;
    }
}
