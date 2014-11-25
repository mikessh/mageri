/*
 * Copyright 2013-2014 Mikhail Shugay (mikhail.shugay@gmail.com)
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
 * Last modified on 25.11.2014 by mikesh
 */

package com.milaboratory.migec2.model.classifier;

import com.milaboratory.migec2.model.variant.Variant;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class BaseInstanceFactory implements InstanceFactory {
    private final Instances dataset;

    public final static String[] FEATURES = new String[]{
            "BgMinorMigFreq", "BgMinorReadFreq",
            "MajorMigCount", "MinorMigCount",
            "MajorReadCount", "MinorReadCount"};

    private static String buildSchema() {
        StringBuilder sb = new StringBuilder("@RELATION	MIGEC2");
        for (String feature : FEATURES) {
            sb.append("\n@ATTRIBUTE\t").append(feature).append("\tNUMERIC");
        }
        sb.append("\n@ATTRIBUTE\tclass\t{0,1}\n@DATA");
        return sb.toString();
    }

    public final static String SCHEMA = buildSchema();

    public BaseInstanceFactory() throws IOException {
        ArffLoader loader = new ArffLoader();
        loader.setSource(new ByteArrayInputStream(SCHEMA.getBytes()));
        this.dataset = loader.getDataSet();
        dataset.setClassIndex(dataset.numAttributes() - 1);
    }

    @Override
    public Instance convert(Variant variant) {
        double[] features = new double[]{
                variant.getBgMinorMigFreq(), variant.getBgMinorReadFreq(),
                variant.getMajorMigCount(), variant.getMinorMigCount(),
                variant.getMajorReadCount(), variant.getMinorReadCount(),
                0 // class
        };

        Instance instance = new Instance(0.0, features);

        for (int i = 0; i < features.length; i++)
            if (Double.isNaN(features[i]))
                instance.setMissing(i);

        instance.setDataset(dataset);
        dataset.add(instance);

        return instance;
    }

    public Instances getDataset() {
        return dataset;
    }
}
