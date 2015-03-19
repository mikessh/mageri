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
package com.milaboratory.oncomigec.util;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.SequenceBuilder;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.io.fasta.FastaReader;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.core.sequencing.read.SSequencingReadImpl;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public final class Util {
    public static final int THREADS = Runtime.getRuntime().availableProcessors();

    public static final byte PH33_BAD_QUAL = (byte) 10, PH33_GOOD_QUAL = (byte) 30,
            PH33_MIN_QUAL = (byte) 2, PH33_MAX_QUAL = (byte) 40;

    public static final String UMI_FIELD_ID = "UMI";

    public static final String DEFAULT_SAMPLE_NAME = "DEFAULT";

    private static final int UMI_QUAL_OFFSET = UMI_FIELD_ID.length() + 2;

    public static NucleotideSequence extractUmi(String header) throws Exception {
        for (String field : header.split(" "))
            if (field.startsWith(UMI_FIELD_ID))
                return new NucleotideSequence(field.split(":")[1]);

        throw new Exception("No UMI field in header");
    }

    public static NucleotideSQPair extractUmiWithQual(String header) {
        for (String field : header.split(" "))
            if (field.startsWith(UMI_FIELD_ID)) {
                String seq = field.split(":")[1];
                String qual = field.substring(UMI_QUAL_OFFSET + seq.length());
                return new NucleotideSQPair(seq, qual);
            }

        return null;
    }

    private static Random rnd = new Random(2106803L);

    public static List<SSequencingRead> readFasta(File fasta) throws IOException {
        FastaReader reader = new FastaReader(fasta);
        List<SSequencingRead> records = new LinkedList<>();
        SSequencingRead record;
        while ((record = reader.take()) != null) {
            records.add(record);
        }
        return records;
    }

    public static NucleotideSequence randomSequence(int length) {
        SequenceBuilder<NucleotideSequence> builder = NucleotideAlphabet.INSTANCE.getBuilderFactory().create(length);
        for (int i = 0; i < length; ++i)
            builder.setCode(i, (byte) rnd.nextInt(4));
        return builder.create();
    }

    public static int randomInRange(int min, int max) {
        return min + rnd.nextInt(max - min + 1);
    }

    public static int randomWithBound(int n) {
        return rnd.nextInt(n);
    }

    public static NucleotideSQPair createRead(String seq, String qual) {
        return new NucleotideSQPair(seq, qual);
    }

    public static NucleotideSQPair cloneRead(NucleotideSQPair read) {
        return new NucleotideSQPair(read.getSequence(),
                read.getQuality());
    }

    public static NucleotideSQPair rc(NucleotideSQPair read) {
        return read.getRC();
    }

    public static SSequencingRead sub(SSequencingRead read, int from, int to) {
        return new SSequencingReadImpl(read.getDescription(),
                read.getData().getRange(from, to),
                read.id());
    }

    public static SSequencingRead sub(SSequencingRead read, int from) {
        return new SSequencingReadImpl(read.getDescription(),
                read.getData().getRange(from, read.getData().size()),
                read.id());
    }

    private static double CQS_FACTOR = PH33_MAX_QUAL / 0.75;

    public static byte percentageToCqs(double percentage) {
        return (byte) Math.max(PH33_MIN_QUAL, (percentage - 0.25) * CQS_FACTOR);
    }

    public static double cqsToPercentage(byte cqs) {
        return cqs / CQS_FACTOR + 0.25;
    }
}
