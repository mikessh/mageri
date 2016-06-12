/*
 * Copyright 2014-2016 Mikhail Shugay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.antigenomics.mageri.pipeline.input;

import com.antigenomics.mageri.preprocessing.barcode.BarcodeListParser;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PrimerRule extends AdapterRule {
    public PrimerRule(String sampleName,
                      List<String> barcodes, boolean paired) throws IOException {
        super(sampleName, barcodes, paired);
    }

    @Override
    protected List<String> prepareBarcodes(List<String> barcodes) {
        List<String> newBarcodes = new ArrayList<>();
        for (String barcode : barcodes) {
            if (!barcode.startsWith(BarcodeListParser.COMMENT)) {
                String[] tokenized = barcode.split(BarcodeListParser.SEPARATOR);
                tokenized[0] = index; // replace reference name by sample name
                newBarcodes.add(StringUtils.join(tokenized, BarcodeListParser.SEPARATOR));
            }
        }
        return newBarcodes;
    }

    @Override
    public List<String> getSampleNames() {
        List<String> sampleNames = new ArrayList<>();
        sampleNames.add(index);
        return sampleNames;
    }

    @Override
    public boolean hasSubMultiplexing() {
        return false;
    }
}
