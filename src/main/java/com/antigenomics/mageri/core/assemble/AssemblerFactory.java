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

package com.antigenomics.mageri.core.assemble;

import com.antigenomics.mageri.core.Mig;
import com.antigenomics.mageri.core.ReadSpecific;
import com.antigenomics.mageri.core.input.PreprocessorParameters;

public abstract class AssemblerFactory<T extends Consensus, V extends Mig> implements ReadSpecific {
    protected final AssemblerParameters parameters;
    protected final PreprocessorParameters preprocessorParameters;

    protected AssemblerFactory(PreprocessorParameters preprocessorParameters,
                               AssemblerParameters parameters) {
        this.preprocessorParameters = preprocessorParameters;
        this.parameters = parameters;
    }

    protected AssemblerFactory() {
        this(PreprocessorParameters.DEFAULT,
                AssemblerParameters.DEFAULT);
    }

    public abstract Assembler<T, V> create();

    public PreprocessorParameters getPreprocessorParameters() {
        return preprocessorParameters;
    }

    public AssemblerParameters getParameters() {
        return parameters;
    }
}
