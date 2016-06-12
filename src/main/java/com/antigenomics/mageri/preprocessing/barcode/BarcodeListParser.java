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
package com.antigenomics.mageri.preprocessing.barcode;

import com.antigenomics.mageri.preprocessing.DemultiplexParameters;
import com.antigenomics.mageri.preprocessing.SAdapterExtractor;
import com.antigenomics.mageri.preprocessing.PAdapterExtractor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class BarcodeListParser {
    public static final String COMMENT = "#", SEPARATOR = "\t",
            EMPTY_BARCODE = ".", MASTER_FIRST_TRUE = "1", MASTER_FIRST_FALSE = "0";
    public static final Pattern ALLOWED_CHARACTERS = Pattern.compile("^[ATGCatgcNnRrYyMmSsWwKkVvDdHhBb]+$"),
            NO_SEED_SLAVE = Pattern.compile("^[atgcNnrymswkvdhb]+$");

    public static SAdapterExtractor generateSCheckoutProcessor(List<String> lines) throws Exception {
        return generateSCheckoutProcessor(lines, DemultiplexParameters.DEFAULT);
    }

    public static SAdapterExtractor generateSCheckoutProcessor(List<String> lines,
                                                               DemultiplexParameters demultiplexParameters) {
        return generateSCheckoutProcessor(lines,
                demultiplexParameters.getMaxTruncations(), demultiplexParameters.getMaxGoodQualMMRatio(),
                demultiplexParameters.getMaxLowQualityMMRatio(), demultiplexParameters.getLowQualityThreshold());
    }

    public static SAdapterExtractor generateSCheckoutProcessor(List<String> lines,
                                                               int maxTruncations,
                                                               double maxGoodMMRatio,
                                                               double maxLowQualMMRatio,
                                                               byte lowQualityThreshold) {
        List<SeedAndExtendBarcodeSearcher> barcodeSearchers = new ArrayList<>();
        List<String> sampleNames = new ArrayList<>();
        Set<String> usedBarcodes = new HashSet<>();

        for (String line : lines) {
            if (!line.startsWith(COMMENT)) {
                String[] splitLine = line.split(SEPARATOR);
                if (splitLine.length < 3)
                    throw new RuntimeException("Bad barcode line:\t" + line);
                String sampleName = splitLine[0];
                String barcode = splitLine[2];
                if (usedBarcodes.contains(barcode))
                    throw new RuntimeException("Duplicate barcode:\t" + line);
                else
                    usedBarcodes.add(barcode);
                if (barcode.equals(EMPTY_BARCODE))
                    throw new RuntimeException("Blank master barcode not allowed:\t" + line);
                if (!ALLOWED_CHARACTERS.matcher(barcode).matches())
                    throw new RuntimeException("Bad barcode character set:\t" + line);

                SeedAndExtendBarcodeSearcher bs = new SeedAndExtendBarcodeSearcher(barcode, maxTruncations,
                        maxGoodMMRatio, maxLowQualMMRatio, lowQualityThreshold);

                sampleNames.add(sampleName);
                barcodeSearchers.add(bs);
            }
        }

        return new SAdapterExtractor(sampleNames.toArray(new String[sampleNames.size()]),
                barcodeSearchers.toArray(new SeedAndExtendBarcodeSearcher[barcodeSearchers.size()]));
    }


    public static PAdapterExtractor generatePCheckoutProcessor(List<String> lines) {
        return generatePCheckoutProcessor(lines, DemultiplexParameters.DEFAULT);
    }

    public static PAdapterExtractor generatePCheckoutProcessor(List<String> lines,
                                                               DemultiplexParameters demultiplexParameters) {
        return generatePCheckoutProcessor(lines, demultiplexParameters.orientedReads(),
                demultiplexParameters.getMaxTruncations(), demultiplexParameters.getMaxGoodQualMMRatio(),
                demultiplexParameters.getMaxLowQualityMMRatio(), demultiplexParameters.getLowQualityThreshold());
    }

    public static PAdapterExtractor generatePCheckoutProcessor(List<String> lines,
                                                               boolean oriented,
                                                               int maxTruncations,
                                                               double maxGoodMMRatio,
                                                               double maxLowQualMMRatio,
                                                               byte lowQualityThreshold) {
        List<SeedAndExtendBarcodeSearcher> masterBarcodeSearchers = new ArrayList<>();
        List<BarcodeSearcher> slaveBarcodeSearchers = new ArrayList<>();
        List<Boolean> masterFirstList = new ArrayList<>();
        List<String> sampleNames = new ArrayList<>();
        Set<String> usedBarcodes = new HashSet<>(), usedMasterBarcodes = new HashSet<>();

        for (String line : lines) {
            if (!line.startsWith(COMMENT)) {
                String[] splitLine = line.split(SEPARATOR);

                if (splitLine.length < 4)
                    throw new RuntimeException("Bad barcode line:\t" + line.replace("\t", "(tab)"));

                String sampleName = splitLine[0];

                if (!(splitLine[1].equals(MASTER_FIRST_FALSE) || splitLine[1].equals(MASTER_FIRST_TRUE)))
                    throw new RuntimeException("Values in master first column should be either " + MASTER_FIRST_FALSE +
                            " or " + MASTER_FIRST_TRUE + ":\t" + line);

                boolean masterFirst = Integer.parseInt(splitLine[1]) > 0;
                masterFirstList.add(masterFirst);

                String masterBarcode = splitLine[2], slaveBarcode = splitLine[3],
                        barcode = masterBarcode + slaveBarcode, noSlaveBarcode = masterBarcode + "-";

                if (usedBarcodes.contains(barcode) ||
                        // already has same master without slave specified
                        usedBarcodes.contains(noSlaveBarcode) ||
                        // slave not specified here, but master barcode already used
                        (slaveBarcode.equals(EMPTY_BARCODE) && usedMasterBarcodes.contains(masterBarcode)))
                    throw new RuntimeException("Duplicate barcode:\t" + line);
                else {
                    usedBarcodes.add(barcode);
                    usedMasterBarcodes.add(masterBarcode);
                }

                if (masterBarcode.equals(EMPTY_BARCODE))
                    throw new RuntimeException("Blank master barcode not allowed:\t" + line);

                if (!ALLOWED_CHARACTERS.matcher(masterBarcode).matches())
                    throw new RuntimeException("Bad master barcode character set:\t" + line);

                if (!slaveBarcode.equals(EMPTY_BARCODE) && !ALLOWED_CHARACTERS.matcher(slaveBarcode).matches())
                    throw new RuntimeException("Bad slave barcode character set:\t" + line);

                SeedAndExtendBarcodeSearcher masterBs = new SeedAndExtendBarcodeSearcher(masterBarcode, maxTruncations,
                        maxGoodMMRatio, maxLowQualMMRatio, lowQualityThreshold);

                BarcodeSearcher slaveBs = slaveBarcode.equals(EMPTY_BARCODE) ? null :
                        (NO_SEED_SLAVE.matcher(slaveBarcode).matches() ?
                                // no upper case characters in slave - positional extraction
                                new SlidingBarcodeSearcher(slaveBarcode) :
                                // seed-and-extend otherwise
                                new SeedAndExtendBarcodeSearcher(slaveBarcode, maxTruncations,
                                        maxGoodMMRatio, maxLowQualMMRatio, lowQualityThreshold)
                        );

                sampleNames.add(sampleName);
                masterBarcodeSearchers.add(masterBs);
                slaveBarcodeSearchers.add(slaveBs);
            }
        }

        boolean[] masterFirstArr = new boolean[masterFirstList.size()];
        for (int i = 0; i < masterFirstArr.length; i++)
            masterFirstArr[i] = masterFirstList.get(i);

        return new PAdapterExtractor(sampleNames.toArray(new String[sampleNames.size()]),
                masterBarcodeSearchers.toArray(new SeedAndExtendBarcodeSearcher[masterBarcodeSearchers.size()]),
                slaveBarcodeSearchers.toArray(new BarcodeSearcher[slaveBarcodeSearchers.size()]),
                masterFirstArr, oriented);
    }
}
