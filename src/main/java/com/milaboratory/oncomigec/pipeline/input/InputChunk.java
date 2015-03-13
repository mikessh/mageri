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

package com.milaboratory.oncomigec.pipeline.input;

import com.milaboratory.oncomigec.core.ReadSpecific;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.InputStream;
import java.util.List;

public class InputChunk implements ReadSpecific {
    private final InputStream inputStream1, inputStream2;
    private final String index;
    private final UmiRule umiRule;
    private final List<SubMultiplexRule> subMultiplexRules;

    public InputChunk(@NotNull InputStream inputStream1, @Nullable InputStream inputStream2,
                      @NotNull String index, @NotNull UmiRule umiRule, @NotNull List<SubMultiplexRule> subMultiplexRules) {
        this.inputStream1 = inputStream1;
        this.inputStream2 = inputStream2;
        this.index = index;
        this.umiRule = umiRule;
        this.subMultiplexRules = subMultiplexRules;
    }

    public boolean hasSubMultiplexing() {
        return !subMultiplexRules.isEmpty();
    }

    public InputStream getInputStream1() {
        return inputStream1;
    }

    public InputStream getInputStream2() {
        return inputStream2;
    }

    public String getIndex() {
        return index;
    }

    public UmiRule getUmiRule() {
        return umiRule;
    }

    public List<SubMultiplexRule> getSubMultiplexRules() {
        return subMultiplexRules;
    }

    @Override
    public boolean isPairedEnd() {
        return inputStream2 != null;
    }
}
