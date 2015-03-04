/*
 * Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)
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
package com.milaboratory.oncomigec.preproc.demultiplex.config;

import com.milaboratory.oncomigec.preproc.demultiplex.barcode.BarcodeSearcher;
import com.milaboratory.oncomigec.preproc.demultiplex.entity.DemultiplexParameters;
import com.milaboratory.oncomigec.preproc.demultiplex.processor.PCheckoutProcessor;
import com.milaboratory.oncomigec.preproc.demultiplex.processor.SCheckoutProcessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class BarcodeListParser {
    private static Pattern bcRgx = Pattern.compile("^[ATGCatgcNnRrYyMmSsWwKkVvDdHhBb]+$");

    public static SCheckoutProcessor generateSCheckoutProcessor(List<String> lines) throws Exception {
        return generateSCheckoutProcessor(lines, DemultiplexParameters.DEFAULT);
    }

    public static SCheckoutProcessor generateSCheckoutProcessor(List<String> lines,
                                                                DemultiplexParameters demultiplexParameters) {
        return generateSCheckoutProcessor(lines, demultiplexParameters.scanRC(),
                demultiplexParameters.getMaxTruncations(), demultiplexParameters.getMaxGoodQualMMRatio(),
                demultiplexParameters.getMaxLowQualityMMRatio(), demultiplexParameters.getLowQualityThreshold());
    }

    public static SCheckoutProcessor generateSCheckoutProcessor(List<String> lines,
                                                                boolean checkRC,
                                                                int maxTruncations,
                                                                double maxGoodMMRatio,
                                                                double maxLowQualMMRatio,
                                                                byte lowQualityThreshold) {
        List<BarcodeSearcher> barcodeSearchers = new ArrayList<>();
        List<String> sampleNames = new ArrayList<>();
        Set<String> usedBarcodes = new HashSet<>();

        for (String line : lines) {
            if (!line.startsWith("#")) {
                String[] splitLine = line.split("\t");
                if (splitLine.length < 3)
                    throw new RuntimeException("Bad barcode line:\t" + line);
                String sampleName = splitLine[0];
                String barcode = splitLine[2];
                if (usedBarcodes.contains(barcode))
                    throw new RuntimeException("Duplicate barcode:\t" + line);
                else
                    usedBarcodes.add(barcode);
                if (barcode.equals("-"))
                    throw new RuntimeException("Blank master barcode not allowed:\t" + line);
                if (!bcRgx.matcher(barcode).matches())
                    throw new RuntimeException("Bad barcode character set:\t" + line);

                BarcodeSearcher bs = new BarcodeSearcher(barcode, maxTruncations,
                        maxGoodMMRatio, maxLowQualMMRatio, lowQualityThreshold);

                sampleNames.add(sampleName);
                barcodeSearchers.add(bs);
            }
        }

        return new SCheckoutProcessor(sampleNames.toArray(new String[sampleNames.size()]),
                barcodeSearchers.toArray(new BarcodeSearcher[barcodeSearchers.size()]),
                checkRC);
    }


    public static PCheckoutProcessor generatePCheckoutProcessor(List<String> lines) {
        return generatePCheckoutProcessor(lines, DemultiplexParameters.DEFAULT);
    }

    public static PCheckoutProcessor generatePCheckoutProcessor(List<String> lines,
                                                                DemultiplexParameters demultiplexParameters) {
        return generatePCheckoutProcessor(lines, demultiplexParameters.scanRC(), demultiplexParameters.orientedReads(),
                demultiplexParameters.illuminaReads(),
                demultiplexParameters.getMaxTruncations(), demultiplexParameters.getMaxGoodQualMMRatio(),
                demultiplexParameters.getMaxLowQualityMMRatio(), demultiplexParameters.getLowQualityThreshold());
    }

    public static PCheckoutProcessor generatePCheckoutProcessor(List<String> lines,
                                                                boolean checkRC,
                                                                boolean oriented,
                                                                boolean illuminaReads,
                                                                int maxTruncations,
                                                                double maxGoodMMRatio,
                                                                double maxLowQualMMRatio,
                                                                byte lowQualityThreshold) {
        List<BarcodeSearcher> masterBarcodeSearchers = new ArrayList<>(),
                slaveBarcodeSearchers = new ArrayList<>();
        List<Boolean> masterFirstList = new ArrayList<>();
        List<String> sampleNames = new ArrayList<>();
        Set<String> usedBarcodes = new HashSet<>(), usedMasterBarcodes = new HashSet<>();

        for (String line : lines) {
            if (!line.startsWith("#")) {
                String[] splitLine = line.split("\t");

                if (splitLine.length < 4)
                    throw new RuntimeException("Bad barcode line:\t" + line.replace("\t", "(tab)"));

                String sampleName = splitLine[0];

                if (!(splitLine[1].equals("0") || splitLine[1].equals("1")))
                    throw new RuntimeException("Values in master first column should be either 0 or 1:\t" + line);

                boolean masterFirst = Integer.parseInt(splitLine[1]) > 0;
                masterFirstList.add(masterFirst);

                String masterBarcode = splitLine[2], slaveBarcode = splitLine[3],
                        barcode = masterBarcode + slaveBarcode, noSlaveBarcode = masterBarcode + "-";

                if (usedBarcodes.contains(barcode) ||
                        // already has same master without slave specified
                        usedBarcodes.contains(noSlaveBarcode) ||
                        // slave not specified here, but master barcode already used
                        (slaveBarcode.equals("-") && usedMasterBarcodes.contains(masterBarcode)))
                    throw new RuntimeException("Duplicate barcode:\t" + line);
                else {
                    usedBarcodes.add(barcode);
                    usedMasterBarcodes.add(masterBarcode);
                }

                if (masterBarcode.equals("-"))
                    throw new RuntimeException("Blank master barcode not allowed:\t" + line);

                if (!bcRgx.matcher(masterBarcode).matches() ||
                        (!slaveBarcode.equals("-") && !bcRgx.matcher(slaveBarcode).matches()))
                    throw new RuntimeException("Bad barcode character set:\t" + line);

                BarcodeSearcher masterBs = new BarcodeSearcher(masterBarcode, maxTruncations,
                        maxGoodMMRatio, maxLowQualMMRatio, lowQualityThreshold),
                        slaveBs = slaveBarcode.equals("-") ? null : new BarcodeSearcher(slaveBarcode, maxTruncations,
                                maxGoodMMRatio, maxLowQualMMRatio, lowQualityThreshold);

                sampleNames.add(sampleName);
                masterBarcodeSearchers.add(masterBs);
                slaveBarcodeSearchers.add(slaveBs);
            }
        }

        boolean[] masterFirstArr = new boolean[masterFirstList.size()];
        for (int i = 0; i < masterFirstArr.length; i++)
            masterFirstArr[i] = masterFirstList.get(i);

        return new PCheckoutProcessor(sampleNames.toArray(new String[sampleNames.size()]),
                masterBarcodeSearchers.toArray(new BarcodeSearcher[masterBarcodeSearchers.size()]),
                slaveBarcodeSearchers.toArray(new BarcodeSearcher[slaveBarcodeSearchers.size()]),
                masterFirstArr,
                checkRC, !oriented, illuminaReads);
    }
}
