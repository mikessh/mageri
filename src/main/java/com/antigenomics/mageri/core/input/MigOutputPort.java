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

package com.antigenomics.mageri.core.input;

import cc.redberry.pipe.OutputPort;
import com.antigenomics.mageri.core.ReadSpecific;
import com.antigenomics.mageri.core.Mig;
import com.antigenomics.mageri.pipeline.analysis.Sample;

public class MigOutputPort<MigType extends Mig> implements OutputPort<MigType>, ReadSpecific {
    private transient final MigReader<MigType> migReader;
    private final Sample sample;
    private final int sizeThreshold;

    public MigOutputPort(MigReader<MigType> migReader, Sample sample, int sizeThreshold) {
        this.migReader = migReader;
        this.sample = sample;
        this.sizeThreshold = sizeThreshold;
    }

    @Override
    public MigType take() {
        return migReader.take(sample, sizeThreshold);
    }

    @Override
    public boolean isPairedEnd() {
        return migReader.isPairedEnd();
    }

    public void clear() {
        migReader.clear(sample);
    }
}
