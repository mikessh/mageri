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

import com.antigenomics.mageri.misc.AtomicDouble;

import java.util.concurrent.atomic.AtomicInteger;

public class DummyMinorCaller extends MinorCaller<DummyMinorCaller> {
    private final AtomicInteger totalMigs = new AtomicInteger();
    private final AtomicDouble logMigSize = new AtomicDouble();

    public DummyMinorCaller() {
        super("MinorCaller.DUMMY");
    }

    @Override
    public double computeFdr(int from, int to) {
        return 0;
    }

    @Override
    boolean callAndUpdate(int from, int to, int k, int n, int n0) {
        totalMigs.incrementAndGet();
        logMigSize.addAndGet(Math.log(n));
        return k > 0;
    }

    @Override
    DummyMinorCaller combine(DummyMinorCaller other) {
        DummyMinorCaller dummyMinorCaller = new DummyMinorCaller();

        dummyMinorCaller.totalMigs.addAndGet(this.totalMigs.get() + other.totalMigs.get());
        dummyMinorCaller.logMigSize.addAndGet(this.logMigSize.get() + other.logMigSize.get());

        return dummyMinorCaller;
    }

    @Override
    public double getReadFractionForCalledMinors(int from, int to) {
        return 1.0;
    }

    @Override
    public double getFilteredReadFraction(int from, int to) {
        return 0.0;
    }

    @Override
    public double getGlobalMinorRate(int from, int to) {
        return 0;
    }

    @Override
    public String getHeader() {
        return "";
    }

    @Override
    public String getBody() {
        return "";
    }

    @Override
    public int getTotalMigs() {
        return totalMigs.get();
    }

    @Override
    public int getGeometricMeanMigSize() {
        return (int) Math.exp(logMigSize.get() / getTotalMigs());
    }
}
