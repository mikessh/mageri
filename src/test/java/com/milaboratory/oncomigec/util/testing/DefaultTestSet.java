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

package com.milaboratory.oncomigec.util.testing;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.milaboratory.oncomigec.util.testing.TestUtil.getResourceAsStream;
import static org.apache.commons.io.IOUtils.readLines;

public class DefaultTestSet {
    public static String SAMPLE_NAME = "GOOD";

    public static int getNumberOfReads() throws IOException {
        return readLines(getResourceAsStream("sample/R1.fastq.gz")).size() / 4;
    }

    public static List<String> getBarcodes() throws IOException {
        InputStream barcodes = getResourceAsStream("sample/barcodes.txt");

        return readLines(barcodes);
    }

    public static InputStream getR1() throws IOException {
        return getResourceAsStream("sample/R1.fastq.gz");
    }

    public static InputStream getR2() throws IOException {
        return getResourceAsStream("sample/R2.fastq.gz");
    }
}
