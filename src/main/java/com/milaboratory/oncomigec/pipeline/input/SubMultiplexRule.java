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
import com.sun.istack.internal.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SubMultiplexRule extends AdapterRule {

    public SubMultiplexRule(@NotNull String sampleName,
                            @NotNull List<String> barcodes, boolean paired) throws IOException {
        super(sampleName, barcodes, paired);
    }

    @Override
    protected List<String> prepareBarcodes(List<String> barcodes) {
        List<String> newBarcodes = new ArrayList<>();
        for (String barcode : barcodes) {
            if (!barcode.startsWith(BarcodeListParser.COMMENT))
                newBarcodes.add(index + "." + barcode);
        }
        return newBarcodes;
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
