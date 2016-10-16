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

public class DummyMinorCaller extends MinorCaller<DummyMinorCaller> {
    public static final DummyMinorCaller INSTANCE = new DummyMinorCaller();

    private DummyMinorCaller() {
        super("MinorCaller.DUMMY");
    }

    @Override
    public double computeFdr(int from, int to) {
        return 0;
    }

    @Override
    boolean callAndUpdate(int from, int to, int k, int n) {
        return k > 0;
    }

    @Override
    DummyMinorCaller combine(DummyMinorCaller other) {
        return this;
    }

    @Override
    public double getReadFractionForCalledMinors(int from, int to) {
        return 1.0;
    }

    @Override
    public String getHeader() {
        return "";
    }

    @Override
    public String getBody() {
        return "";
    }
}
