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

def cli = new CliBuilder(usage: "TrainClassifier [options] control_variants input_vardump output_prefix\n" +
        "Control variants is a tab-delimited table of the following structure:\n" +
        "|reference name|position|variant")
cli.h("display help message")

cli.width = 100

def opt = cli.parse(args)

if (opt == null)
    System.exit(-1)

if (opt.h || opt.arguments().size() != 3) {
    cli.usage()
    System.exit(-1)
}

def controlFileName = opt.arguments()[0], vardumpFileName = opt.arguments()[1], outputPrefix = opt.arguments()[2]

def controlSet = new HashSet(new File(controlFileName).readLines())

def idCols = ["ReferenceName", "Pos", "Nt"], idColIndices = [-1, -1, -1],
        featureCols = ["BgMinorMigFreq", "BgMinorReadFreq",
                       "MajorMigCount", "MinorMigCount",
                       "MajorReadCount", "MinorReadCount"], featureColIndices = [-1, -1, -1, -1, -1, -1]

def SCHEMA = "@RELATION\tMIGEC2\n" +
        featureCols.collect { "@ATTRIBUTE\t$it\tNUMERIC" }.join("\n") +
        "\n@ATTRIBUTE\tclass\t{0,1}\n@DATA"

new File(outputPrefix + "_training_set.arff").withPrintWriter { pw ->
    pw.println(SCHEMA)

    new File(vardumpFileName).withReader { reader ->
        def header = reader.readLine()[1..-1].split("\t")
        idCols.eachWithIndex { name, ind ->
            idColIndices[ind] = header.findIndexOf { it == name }
        }
        featureCols.eachWithIndex { name, ind ->
            featureColIndices[ind] = header.findIndexOf { it == name }
        }

        def line
        while ((line = reader.readLine()) != null) {
            def splitLine = line.split("\t")
            boolean control = controlSet.contains(splitLine[[idColIndices]].join("\t"))

            pw.println([line[featureColIndices], control ? 1 : 0].join("\t"))
        }
    }
}