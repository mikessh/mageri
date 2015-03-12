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

import com.milaboratory.oncomigec.core.align.reference.ReferenceLibrary;
import com.milaboratory.oncomigec.preproc.demultiplex.processor.CheckoutProcessor;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

public class ProjectProcessor {
    protected final ReferenceLibrary referenceLibrary;
    protected final CheckoutProcessor checkoutProcessor;
    protected final Project project;
    protected final Presets presets;
    protected final RuntimeParameters runtimeParameters;

    private final Map<Sample, SampleProcessor> processorBySample = new TreeMap<>();

    public ProjectProcessor(ReferenceLibrary referenceLibrary, CheckoutProcessor checkoutProcessor,
                            Project project,
                            Presets presets, RuntimeParameters runtimeParameters) {
        this.referenceLibrary = referenceLibrary;
        this.checkoutProcessor = checkoutProcessor;
        this.project = project;
        this.presets = presets;
        this.runtimeParameters = runtimeParameters;

        //for (Sample sample : project) {
        //    SampleProcessor sampleProcessor = new SampleProcessor()
        //}
    }

    public void preprocess(File r1, File r2) {


    }

    public ReferenceLibrary getReferenceLibrary() {
        return referenceLibrary;
    }

    public Project getProject() {
        return project;
    }

    public Presets getPresets() {
        return presets;
    }

    public RuntimeParameters getRuntimeParameters() {
        return runtimeParameters;
    }
}
