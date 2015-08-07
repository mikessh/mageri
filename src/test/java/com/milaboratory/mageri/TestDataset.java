/*
 * Copyright (c) 2014-2015, Bolotin Dmitry, Chudakov Dmitry, Shugay Mikhail
 * (here and after addressed as Inventors)
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact the Inventors using one of the following
 * email addresses: chudakovdm@mail.ru, chudakovdm@gmail.com
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */

package com.milaboratory.mageri;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.milaboratory.mageri.TestUtil.getResourceAsStream;
import static org.apache.commons.io.IOUtils.readLines;

public class TestDataset {
    public static String SAMPLE_NAME = "GOOD";

    public static int getNumberOfReads() throws IOException {
        return readLines(getResourceAsStream("sample/R1.fastq.gz")).size() / 4;
    }

    public static List<String> getBarcodesMix() throws IOException {
        return readLines(getResourceAsStream("sample/barcodes_mix.txt"));
    }

    public static List<String> getBarcodesNoSlave() throws IOException {
        return readLines(getResourceAsStream("sample/barcodes_noslave.txt"));
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
