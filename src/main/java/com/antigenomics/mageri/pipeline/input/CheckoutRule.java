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

package com.antigenomics.mageri.pipeline.input;

import com.antigenomics.mageri.preprocessing.CheckoutProcessor;
import com.antigenomics.mageri.preprocessing.DemultiplexParameters;

import java.io.Serializable;
import java.util.List;

public abstract class CheckoutRule implements Serializable {
    protected DemultiplexParameters demultiplexParameters = DemultiplexParameters.DEFAULT;

    protected CheckoutRule() {

    }

    public abstract CheckoutProcessor getProcessor();

    public abstract List<String> getSampleNames();

    public abstract boolean hasSubMultiplexing();

    public DemultiplexParameters getDemultiplexParameters() {
        return demultiplexParameters;
    }

    public void setDemultiplexParameters(DemultiplexParameters demultiplexParameters) {
        this.demultiplexParameters = demultiplexParameters;
    }
}
