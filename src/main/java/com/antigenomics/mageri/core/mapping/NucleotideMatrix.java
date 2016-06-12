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
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Singapore-style atomic coverage container.
 */
public final class NucleotideMatrix implements Serializable {
    final AtomicIntegerArray matrixArray;
    final int size;

    /**
     * Creates nucleotide matrix container with given length.
     *
     * @param size size of container
     */
    public NucleotideMatrix(int size) {
        this.size = size;
        this.matrixArray = new AtomicIntegerArray(size * 4);
    }

    /**
     * Increments count for a given letter in a given position.
     *
     * @param position position in sequence
     * @param letter   letter
     * @return value after increment
     */
    public int incrementAt(int position, int letter) {
        return matrixArray.incrementAndGet(4 * position + letter);
    }

    /**
     * Decrements count for a given letter in a given position.
     *
     * @param position position in sequence
     * @param letter   letter
     * @return value after decrement
     */
    public int decrementAt(int position, int letter) {
        return matrixArray.decrementAndGet(4 * position + letter);
    }

    /**
     * Returns the coverage value
     *
     * @param position
     * @param letter
     * @return
     */
    public int getAt(int position, int letter) {
        return matrixArray.get(4 * position + letter);
    }

    int getSize() {
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NucleotideMatrix)) return false;

        NucleotideMatrix that = (NucleotideMatrix) o;

        if (size != that.size) return false;
        if (!matrixArray.equals(that.matrixArray)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = matrixArray.hashCode();
        result = 31 * result + size;
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("NucleotideMatrix\n");
        for (int l = 0; l < 4; ++l) {
            for (int i = 0; i < size; ++i)
                sb.append(getAt(i, l)).append("\t");
            sb.deleteCharAt(sb.length() - 1); //Removing last "\t"
            sb.append("\n");
        }
        sb.deleteCharAt(sb.length() - 1); //Removing last "\n"
        return sb.toString();
    }
}
