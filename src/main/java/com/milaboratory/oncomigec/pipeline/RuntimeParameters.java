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

import com.milaboratory.oncomigec.util.ParameterSet;
import org.jdom.Element;

public class RuntimeParameters implements ParameterSet {
    private final int numberOfThreads;

    public RuntimeParameters(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    @Override
    public Element toXml() {
        Element e = new Element("MigecRuntimeParameters");
        e.addContent(new Element("numberOfThreads").setText(Integer.toString(numberOfThreads)));
        return e;
    }
}
