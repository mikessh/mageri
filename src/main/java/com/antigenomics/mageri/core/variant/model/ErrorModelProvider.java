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

package com.antigenomics.mageri.core.variant.model;

import com.antigenomics.mageri.core.assemble.MinorCaller;
import com.antigenomics.mageri.core.mapping.MutationsTable;
import com.antigenomics.mageri.core.variant.VariantCallerParameters;

public class ErrorModelProvider {
    public static ErrorModel create(VariantCallerParameters parameters, MutationsTable mutationsTable,
                                    MinorCaller minorCaller) {
        switch (parameters.getErrorModelType()) {
            case MinorBased:
                return new MinorBasedErrorModel(parameters.getModelOrder(),
                        parameters.getModelCycles(), parameters.getModelEfficiency(),
                        parameters.getModelCoverageThreshold(), parameters.getModelMinorCountThreshold(),
                        mutationsTable, minorCaller);
            case RawData:
                return new RawDataErrorModel(mutationsTable);
            case Custom:
                return parameters.getParsedSubstitutionErrorRateMatrix();
            default:
                throw new IllegalArgumentException("Unknown error model " + parameters.getErrorModelType().toString());
        }
    }

    public static String getErrorModelHeader(VariantCallerParameters parameters) {
        String[] statisticNames = getErrorModelStatisticNames(parameters);

        String header = "";
        if (statisticNames.length > 0) {
            for (String name : statisticNames) {
                header += "\t" + name;
            }
        }
        return header;
    }

    public static String[] getErrorModelStatisticIDs(VariantCallerParameters parameters) {
        switch (parameters.getErrorModelType()) {
            case MinorBased:
                return new String[]{"ER", "MC", "FDR", "RCL"};
            default:
                return new String[0];
        }
    }

    public static String[] getErrorModelStatisticNames(VariantCallerParameters parameters) {
        switch (parameters.getErrorModelType()) {
            case MinorBased:
                return new String[]{"error.rate", "minor.count", "minor.fdr", "minor.recall"};
            default:
                return new String[0];
        }
    }

    public static String[] getErrorModelStatisticDescriptions(VariantCallerParameters parameters) {
        switch (parameters.getErrorModelType()) {
            case MinorBased:
                return new String[]{"PCR per cycle per base error rate estimate",
                        "Number of detected PCR minors",
                        "PCR minor detection FDR (H0=sequencing errors)",
                        "PCR minor recall, assuming PCR minors are lost due to sampling and finite MIG size"};
            default:
                return new String[0];
        }
    }
}
