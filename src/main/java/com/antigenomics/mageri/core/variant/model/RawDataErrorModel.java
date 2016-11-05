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

package com.antigenomics.mageri.core.variant.model;

import com.antigenomics.mageri.core.mapping.MutationsTable;
import com.antigenomics.mageri.core.mutations.Mutation;
import com.antigenomics.mageri.core.mutations.Substitution;
import com.milaboratory.core.sequence.mutations.Mutations;

public class RawDataErrorModel implements ErrorModel {
    private final MutationsTable mutationsTable;

    public RawDataErrorModel(MutationsTable mutationsTable) {
        this.mutationsTable = mutationsTable;
    }

    @Override
    public ErrorRateEstimate computeErrorRate(Mutation mutation) {
        int code = ((Substitution) mutation).getCode();

        return computeErrorRate(Mutations.getPosition(code), 0, Mutations.getTo(code));
    }

    @Override
    public ErrorRateEstimate computeErrorRate(int pos, int from, int to) {
        double cqs = mutationsTable.getMeanCqs(pos, to);

        return new ErrorRateEstimate(Math.pow(10.0, -cqs / 10.0) / 3);
    }
}
