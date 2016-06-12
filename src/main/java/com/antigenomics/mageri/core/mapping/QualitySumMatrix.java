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
package com.antigenomics.mageri.core.mapping;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * Singapore-style atomic coverage container.
 */
public final class QualitySumMatrix implements Serializable {
    final AtomicLongArray qualitySum;
    final int size;

    /**
     * Creates nucleotide coverage container with given length.
     *
     * @param size size of container
     */
    public QualitySumMatrix(int size) {
        this.size = size;
        this.qualitySum = new AtomicLongArray(size * 4);
    }

    /**
     * Increments coverage value for a given letter in a given position.
     *
     * @param position position in sequence
     * @param letter   letter
     * @param by       quality value
     */
    public void increaseAt(int position, int letter, byte by) {
        qualitySum.addAndGet(4 * position + letter, by);
    }

    /**
     * Decrements coverage value for a given letter in a given position.
     *
     * @param position position in sequence
     * @param letter   letter
     * @param by       quality value
     */
    public void decreaseAt(int position, int letter, byte by) {
        qualitySum.addAndGet(4 * position + letter, -by);
    }

    /**
     * Returns the coverage value
     *
     * @param position
     * @return
     */
    public long getAt(int position, int letter) {
        return qualitySum.get(4 * position + letter);
    }

    public int size() {
        return size;
    }
}
