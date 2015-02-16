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

package com.milaboratory.oncomigec.model.classifier;

import com.milaboratory.oncomigec.model.variant.Variant;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BaseInstanceFactory implements InstanceFactory {
    private final Instances dataset;
    private final List<Variant> variants = new LinkedList<>();

    public final static String[] FEATURES = new String[]{
            "BgMinorMigFreq", "BgMinorReadFreq",
            "MajorMigFreq", "MinorMigFreq",
            "MajorReadFreq", "MinorReadFreq"};

    private static String buildSchema() {
        StringBuilder sb = new StringBuilder("@RELATION	MIGEC2_BASE");
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

    protected Instance getInstance(Variant variant) {
        return getInstance(variant, false);
    }

    protected Instance getInstance(Variant variant, boolean real) {
        double[] features = new double[]{
                // don't worry abt NaNs, we'll set them as missing
                Math.log10(variant.getBgMinorMigFreq()),
                Math.log10(variant.getBgMinorReadFreq()),
                Math.log10(variant.getMajorMigCount() / (double) variant.getSumAtPosMig()),
                Math.log10(variant.getMinorMigCount() / (double) variant.getSumAtPosMig()),
                Math.log10(variant.getMajorReadCount() / (double) variant.getSumAtPosRead()),
                Math.log10(variant.getMinorReadCount() / (double) variant.getSumAtPosRead()),
                real ? 1 : 0 // class
        };

        Instance instance = new Instance(1.0, features);

        for (int i = 0; i < features.length; i++)
            if (Double.isNaN(features[i]))
                instance.setMissing(i);

        return instance;
    }

    @Override
    public Instance convert(Variant variant) {
        Instance instance = getInstance(variant);

        instance.setDataset(dataset);

        return instance;
    }

    @Override
    public Instance convertAndStore(Variant variant, boolean truePositive) {
        Instance instance = getInstance(variant, truePositive);

        dataset.add(instance);
        instance.setDataset(dataset);
        variants.add(variant);

        return instance;
    }

    @Override
    public boolean hasInstances() {
        return dataset.numInstances() > 0;
    }

    @Override
    public Instances getDataset() {
        return dataset;
    }

    @Override
    public List<Variant> getVariants() {
        return Collections.unmodifiableList(variants);
    }

    @Override
    public void save(File outputFile) throws IOException {
        ArffSaver saver = new ArffSaver();
        saver.setInstances(dataset);
        saver.setFile(outputFile);
        saver.writeBatch();
    }
}
