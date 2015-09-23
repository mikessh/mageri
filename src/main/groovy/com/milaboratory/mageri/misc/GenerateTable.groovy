package com.milaboratory.mageri.misc

def variantMap = new HashMap<String, String>()

def signatureHeader = "\tchr\tpos\tfrom\tto"

def getSignature = { List<String> vcfLine ->
    vcfLine[[0, 1, 3, 4]].join("\t")
}

def statFields = "\tfreq\tdepth\tqual"

def getStatFields = { String processing, List<String> vcfLine ->
    if (processing.startsWith("mageri")) {
        def splitLine = vcfLine[7].split(";")
        return [splitLine.find { it.startsWith("AF=") }.substring(3),
                splitLine.find { it.startsWith("DP=") }.substring(3),
                vcfLine[5]].join("\t")
    } else {
        def splitLine = vcfLine[9].split(":")

        return [splitLine[6].replaceAll("%", "").replaceAll(",", ".").toDouble() / 100,
                splitLine[3],
                splitLine[1],
        ].join("\t")
    }
}

new File("h4_hd734_variants.vcf").splitEachLine("\t") {
    if (!it[0].startsWith("#")) {
        variantMap.put(getSignature(it), it[2])
    }
}

def somaticFiles = ["mageri_1"      : [1, 2, 3, 4].collect { m -> "p126/p126-2.h4_ballast_m${m}.vcf" },
                    "mageri_2"      : [1, 2, 3, 4].collect { m -> "p126/p126-3.h4_ballast_m${m}.vcf" },
                    "conventional_1": ["varscan_raw/P126_2.vcf"],
                    "conventional_2": ["varscan_raw/P126_3.vcf"]]
controlFiles = ["mageri_1"      : [1, 2, 3, 4].collect { m -> "p127/p127.ballast1_m${m}.vcf" },
                "mageri_2"      : [1, 2, 3, 4].collect { m -> "p127/p127.ballast2_m${m}.vcf" },
                "conventional_1": ["varscan_raw/P127_1.vcf"],
                "conventional_2": ["varscan_raw/P127_2.vcf"]]

new File("summary.txt").withPrintWriter { pw ->
    pw.println("processing\treplica\ttype\tname" + signatureHeader + statFields)

    controlFiles.each { entry ->
        def (sample, replica) = entry.key.split("_")
        entry.value.each { fileName ->
            new File(fileName).splitEachLine("\t") { splitLine ->
                if (!splitLine[0].startsWith("#")) {
                    pw.println(sample + "\t" + replica + "\terror\t.\t" +
                            getSignature(splitLine) + "\t" + getStatFields(entry.key, splitLine))
                }
            }
        }
    }

    somaticFiles.each { entry ->
        def (sample, replica) = entry.key.split("_")
        entry.value.each { fileName ->
            new File(fileName).splitEachLine("\t") { splitLine ->
                if (!splitLine[0].startsWith("#")) {
                    def signature = getSignature(splitLine)
                    def name = variantMap[signature]
                    if (name) {
                        pw.println(sample + "\t" + replica + "\tsomatic\t$name\t" +
                                signature + "\t" + getStatFields(entry.key, splitLine))
                    }
                }
            }
        }
    }
}