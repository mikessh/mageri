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
 * Last modified on 23.11.2014 by mikesh
 */

package com.milaboratory.migec2.model.classifier

import weka.classifiers.bayes.NaiveBayes
import weka.core.Instances
import weka.core.converters.ArffLoader


class ClassifierWrapper {
    private final NaiveBayes classifier
    private final Instances trainingData

    public ClassifierWrapper(String inputFile) {
        classifier = new NaiveBayes()
        classifier.useSupervisedDiscretization = true

        ArffLoader loader = new ArffLoader()
        loader.setFile(new File(inputFile))
        trainingData = loader.getDataSet()
        trainingData.setClassIndex(trainingData.numAttributes() - 1)

        classifier.buildClassifier(trainingData)
    }

    public void saveClassifier(String outputFileName) {

    }
}
