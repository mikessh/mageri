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

package com.milaboratory.oncomigec.pipeline;

import java.io.Serializable;

public class RuntimeParameters implements Serializable {
    // todo: verbosity levels
    // 0 - silent (errors only)
    // 1 - major steps (pre-processing, assembly, ...)
    // 2 - minor steps (reads parsed, migs processed, etc)
    // 3 - full

    private final int numberOfThreads;
    private final long readLimit;
    private final byte verbosityLevel;

    public static final RuntimeParameters DEFAULT = new RuntimeParameters();

    private RuntimeParameters() {
        this(Runtime.getRuntime().availableProcessors(), -1, (byte) 3);
    }

    public RuntimeParameters(int numberOfThreads, long readLimit, byte verbosityLevel) {
        this.numberOfThreads = numberOfThreads;
        this.readLimit = readLimit;
        this.verbosityLevel = verbosityLevel;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public long getReadLimit() {
        return readLimit;
    }

    public byte getVerbosityLevel() {
        return verbosityLevel;
    }

    public RuntimeParameters withNumberOfThreads(int numberOfThreads) {
        return new RuntimeParameters(numberOfThreads, readLimit, verbosityLevel);
    }

    public RuntimeParameters withReadLimit(long readLimit) {
        return new RuntimeParameters(numberOfThreads, readLimit, verbosityLevel);
    }

    public RuntimeParameters withVerbosityLevel(byte verbosityLevel) {
        return new RuntimeParameters(numberOfThreads, readLimit, verbosityLevel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RuntimeParameters that = (RuntimeParameters) o;

        if (numberOfThreads != that.numberOfThreads) return false;
        if (readLimit != that.readLimit) return false;
        if (verbosityLevel != that.verbosityLevel) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = numberOfThreads;
        result = 31 * result + (int) (readLimit ^ (readLimit >>> 32));
        result = 31 * result + (int) verbosityLevel;
        return result;
    }
}
