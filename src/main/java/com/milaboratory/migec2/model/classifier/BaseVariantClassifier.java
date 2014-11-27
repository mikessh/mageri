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
import weka.classifiers.Classifier;
import weka.classifiers.misc.SerializedClassifier;
import weka.core.Instance;
import weka.core.SerializationHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class BaseVariantClassifier implements VariantClassifier {
    private final Classifier classifier;
    private final InstanceFactory instanceFactory;
    private static boolean verbose = false;

    public final static BaseVariantClassifier BUILT_IN;

    static {
        BaseVariantClassifier _BUILT_IN;
        try {
            _BUILT_IN = pretrained(
                    Thread.currentThread().getContextClassLoader().
                            getResourceAsStream("classifier/default_classifier.model")
            );
        } catch (Exception e) {
            _BUILT_IN = null;
            System.out.println("[CRITICAL ERROR] Could not load default classifier");
            e.printStackTrace();
        }

        BUILT_IN = _BUILT_IN;

        verbose = true;
    }

    private BaseVariantClassifier(Classifier classifier, InstanceFactory instanceFactory) {
        this.classifier = classifier;
        this.instanceFactory = instanceFactory;
    }

    public static BaseVariantClassifier pretrained(File inputFile) throws Exception {
        return pretrained(new FileInputStream(inputFile),
                new BaseInstanceFactory());
    }

    public static BaseVariantClassifier pretrained(File inputFile,
                                                   InstanceFactory instanceFactory) throws Exception {
        return pretrained(new FileInputStream(inputFile),
                instanceFactory);
    }

    public static BaseVariantClassifier pretrained(InputStream inputStream) throws Exception {
        return pretrained(inputStream,
                new BaseInstanceFactory());
    }

    public static BaseVariantClassifier pretrained(InputStream inputStream,
                                                   InstanceFactory instanceFactory) throws Exception {
        SerializedClassifier classifier = new SerializedClassifier();
        classifier.setModel((Classifier) SerializationHelper.read(inputStream));

        // Important: check if attributes match
        classifier.getCapabilities().testWithFail(instanceFactory.getDataset());

        if (verbose)
            System.out.println("[BaseVariantClassifier] Loaded " + classifier.toString() + " model.");

        return new BaseVariantClassifier(classifier, instanceFactory);
    }

    public static BaseVariantClassifier train(InstanceFactory instanceFactory) throws Exception {
        return train(
                "weka.classifiers.bayes.BayesNet",
                new String[]{
                        "-Q", "weka.classifiers.bayes.net.search.local.K2",
                        "--",
                        "-P", "999",
                        "-mbc",
                        "-S", "AIC",
                        "-E", "weka.classifiers.bayes.net.estimate.SimpleEstimator",
                        "--",
                        "-A", "0.5"
                },
                instanceFactory);
    }

    public static BaseVariantClassifier train(String classifierName, String[] options,
                                              InstanceFactory instanceFactory) throws Exception {
        if (!instanceFactory.hasInstances())
            throw new Exception("Empty InstanceFactory supplied for classifier training");
        Classifier classifier = Classifier.forName(classifierName, options);
        classifier.buildClassifier(instanceFactory.getDataset());
        return new BaseVariantClassifier(classifier, instanceFactory);
    }

    public ClassifierResult classify(Instance instance) {
        ClassifierResult classifierResult = null;
        try {
            classifierResult = new ClassifierResult(classifier.distributionForInstance(instance));
        } catch (Exception ignored) {
            // We've already performed all checks
        }
        return classifierResult;
    }

    @Override
    public ClassifierResult classify(Variant variant) {
        return classify(instanceFactory.convert(variant));
    }

    @Override
    public void save(File outputFile) throws Exception {
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        SerializationHelper.write(outputStream, classifier);
        outputStream.close();
    }
}
