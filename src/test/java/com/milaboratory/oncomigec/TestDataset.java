/*
 * Copyright 2013-2015 Mikhail Shugay (mikhail.shugay@gmail.com)
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
 * Last modified on 4.3.2015 by mikesh
 */

package com.milaboratory.oncomigec;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.milaboratory.oncomigec.TestUtil.getResourceAsStream;
import static org.apache.commons.io.IOUtils.readLines;

public class TestDataset {
    public static String SAMPLE_NAME = "GOOD";

    public static int getNumberOfReads() throws IOException {
        return readLines(getResourceAsStream("sample/R1.fastq.gz")).size() / 4;
    }

    public static List<String> getBarcodes() throws IOException {
        return readLines(getResourceAsStream("sample/barcodes.txt"));
    }

    public static List<String> getBarcodesGood() throws IOException {
        return readLines(getResourceAsStream("sample/barcodes_good.txt"));
    }

    public static List<String> getBarcodesBadSlave() throws IOException {
        return readLines(getResourceAsStream("sample/barcodes_badslave.txt"));
    }

    public static List<String> getBarcodesSlaveFirst() throws IOException {
        return readLines(getResourceAsStream("sample/barcodes_slavefirst.txt"));
    }

    public static InputStream getR1() throws IOException {
        return getResourceAsStream("sample/R1.fastq.gz");
    }

    public static InputStream getR2RC() throws IOException {
        return getResourceAsStream("sample/R2_RC.fastq.gz");
    }

    public static InputStream getR2() throws IOException {
        return getResourceAsStream("sample/R2.fastq.gz");
    }
}
