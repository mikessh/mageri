/*
 * Copyright 2014-2016 Mikhail Shugay
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

package com.antigenomics.mageri.pipeline;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Speaker {
    private RuntimeParameters runtimeParameters = RuntimeParameters.DEFAULT;

    private Date start = null;

    public static final Speaker INSTANCE = new Speaker();

    /**
     * Prints a message to system output stream in accordance to verbosity settings.
     *
     * @param message message to be printed.
     * @param verbosityLevel verbosity level of the message: 0 - silent (errors only),
     *                       1 - major steps (pre-processing, assembly, ...),
     *                       2 - minor steps (reads parsed, migs processed, etc),
     *                       3 - full.
     */
    public void sout(String message, int verbosityLevel) {
        if (verbosityLevel <= runtimeParameters.getVerbosityLevel()) {
            if (start == null)
                start = new Date();

            Date now = new Date();
            System.out.println("[" + now.toString() + " +" + timePassed(now.getTime() - start.getTime()) + "] " +
                    message);
        }
    }

    private String timePassed(long millis) {
        return String.format("%02dm%02ds",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }


    private Speaker() {

    }

    public RuntimeParameters getRuntimeParameters() {
        return runtimeParameters;
    }

    public void setRuntimeParameters(RuntimeParameters runtimeParameters) {
        this.runtimeParameters = runtimeParameters;
    }
}
