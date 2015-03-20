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
 * Last modified on 20.3.2015 by mikesh
 */

package com.milaboratory.oncomigec.core.consalign.mutations;

import com.milaboratory.core.sequence.mutations.Mutations;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MinorMutationData {
    private final Map<Integer, MinorMutationCounter> counterByMutation = new HashMap<>(),
            counterByPosition = new HashMap<>();
    private int readId = 0;

    public MinorMutationData() {
    }

    public void update(int code) {
        MinorMutationCounter counter = counterByMutation.get(code);

        if (counter == null)
            counterByMutation.put(code, counter = new MinorMutationCounter());

        counter.update();
    }

    public void nextRead() {
        readId++;
    }

    public int getLostReadCount(int code) {
        return counterByMutation.get(code).cumulative;
    }

    public int getGainedReadCount(int pos) {
        MinorMutationCounter counter = counterByPosition.get(pos);
        return counter == null ? 0 : counter.unique;
    }

    public Set<Integer> getCodes() {
        return counterByMutation.keySet();
    }

    public int getMigSize() {
        return readId;
    }

    public void computeCoverage() {
        for (Map.Entry<Integer, MinorMutationCounter> entry : counterByMutation.entrySet()) {
            int pos = Mutations.getPosition(entry.getKey());
            MinorMutationCounter counter = entry.getValue();

            MinorMutationCounter coverage = counterByPosition.get(pos);
            if (coverage == null)
                counterByPosition.put(pos, coverage = new MinorMutationCounter());

            coverage.append(counter);
        }
    }

    private class MinorMutationCounter {
        private int unique = 0, cumulative = 0, currentReadId = -1;

        private void append(MinorMutationCounter other) {
            unique += other.unique;
            cumulative += other.cumulative;
        }

        private void update() {
            cumulative++;
            if (readId != currentReadId) {
                unique++;
                currentReadId = readId;
            }
        }

        public int getUnique() {
            return unique;
        }

        public int getCumulative() {
            return cumulative;
        }
    }
}
