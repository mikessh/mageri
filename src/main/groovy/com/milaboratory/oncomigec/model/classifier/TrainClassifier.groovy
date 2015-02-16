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

package com.milaboratory.oncomigec.model.classifier

import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet
import com.milaboratory.oncomigec.model.variant.Variant
import weka.core.Instance

def cli = new CliBuilder(usage: "TrainClassifier [options] control_variants input_vardump output_prefix\n" +
        "Control variants is a tab-delimited table of the following structure:\n" +
        "|reference name|reference type|position|variant")
cli.h("display help message")

cli.width = 100

def opt = cli.parse(args)

if (opt == null)
    System.exit(-1)

if (opt.h || opt.arguments().size() != 3) {
    cli.usage()
    System.exit(-1)
}

def rc = [((char) 'A'): (char) 'T', ((char) 'T'): (char) 'A', ((char) 'G'): (char) 'C', ((char) 'C'): (char) 'G']

def controlFileName = opt.arguments()[0], vardumpFileName = opt.arguments()[1], outputPrefix = opt.arguments()[2]

def truePositiveSet = new HashSet(new File(controlFileName).readLines().collect { it.toUpperCase() })

def idMap = new HashMap<String, Integer>()
def ids = ["referenceName", "referenceType", "referenceRC", "referenceLen",
           "pos", "to",
           "bgMinorMigFreq", "bgMinorReadFreq",
           "sumAtPosMig", "sumAtPosRead",
           "minorMigCount", "majorMigCount",
           "minorReadCount", "majorReadCount"]

def instanceFactory = new BaseInstanceFactory()
def variantMap = new HashMap<String, Instance>()

def varDumpHeader

new File(vardumpFileName).withReader { reader ->
    varDumpHeader = reader.readLine()[1..-1].split("\t")
    ids.each { id -> idMap.put(id, varDumpHeader.findIndexOf { id.toUpperCase() == it.toUpperCase() }) }

    def line
    while ((line = reader.readLine()) != null) {
        def splitLine = line.split("\t")

        def referenceName = splitLine[idMap["referenceName"]],
            referenceType = splitLine[idMap["referenceType"]],
            referenceRC = splitLine[idMap["referenceRC"]].toUpperCase() == "TRUE",
            referenceLen = splitLine[idMap["referenceLen"]].toInteger(),
            pos = splitLine[idMap["pos"]].toInteger() - 1,
            to = splitLine[idMap["to"]].charAt(0),
            bgMinorMigFreq = splitLine[idMap["bgMinorMigFreq"]].toDouble(),
            bgMinorReadFreq = splitLine[idMap["bgMinorReadFreq"]].toDouble(),
            sumAtPosMig = splitLine[idMap["sumAtPosMig"]].toInteger(),
            sumAtPosRead = splitLine[idMap["sumAtPosRead"]].toLong(),
            minorMigCount = splitLine[idMap["minorMigCount"]].toInteger(),
            majorMigCount = splitLine[idMap["majorMigCount"]].toInteger(),
            minorReadCount = splitLine[idMap["minorReadCount"]].toLong(),
            majorReadCount = splitLine[idMap["majorReadCount"]].toLong()

        def variantKey = [referenceName, referenceType,
                          referenceRC ? (referenceLen - pos - 1) : pos,
                          referenceRC ? rc[to] : to].join("\t").toUpperCase()

        boolean truePositive = truePositiveSet.contains(variantKey)

        def variant = new Variant(null, pos, NucleotideAlphabet.INSTANCE.codeFromSymbol(to),
                null, bgMinorMigFreq, bgMinorReadFreq,
                sumAtPosMig, sumAtPosRead,
                minorMigCount, majorMigCount,
                minorReadCount, majorReadCount)

        def instance = instanceFactory.convertAndStore(variant, truePositive)
        variantMap.put(line + "\t" + (truePositive ? 1 : 0), instance)
    }
}

new File(outputPrefix + "_tmp.txt").absoluteFile.parentFile.mkdirs()

// This will allow further exploration of classifier performance
// via Weka software
instanceFactory.save(new File(outputPrefix + "_training_set.arff"))

// Save a trained classifier to be further re-used
def variantClassifier = BaseVariantClassifier.train(instanceFactory)
variantClassifier.save(new File(outputPrefix + "_classifier.model"))

// Output predictions
new File(outputPrefix + "_predictions.txt").withPrintWriter { pw ->
    pw.println(varDumpHeader.join("\t") + "\ttruePositive\tclassifierVerdict\tpValue")
    variantMap.each {
        def result = variantClassifier.classify((Instance) it.value)
        pw.println(it.key + "\t" + (result.passed() ? 1 : 0) + "\t" + result.PValue)
    }
}