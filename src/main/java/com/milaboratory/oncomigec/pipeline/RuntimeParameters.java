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
 * Last modified on 12.3.2015 by mikesh
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

    public static RuntimeParameters DEFAULT = new RuntimeParameters(),
            DEBUG = new RuntimeParameters(1, -1, (byte) 3);


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
}
