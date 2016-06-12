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

package com.antigenomics.mageri.core.mutations;

import java.util.Arrays;

public abstract class Indel extends Mutation {
    protected final int[] codes;

    protected Indel(MutationArray parent, int[] codes) {
        super(parent);
        this.codes = codes;
    }

    public int getLength() {
        return codes.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Indel indel = (Indel) o;

        if (!Arrays.equals(codes, indel.codes)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(codes);
    }
}
