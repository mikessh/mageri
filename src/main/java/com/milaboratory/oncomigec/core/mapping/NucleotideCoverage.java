/*
 * Copyright (c) 2014-2015, Bolotin Dmitry, Chudakov Dmitry, Shugay Mikhail
 * (here and after addressed as Inventors)
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact the Inventors using one of the following
 * email addresses: chudakovdm@mail.ru, chudakovdm@gmail.com
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */
package com.milaboratory.oncomigec.core.mapping;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Singapore-style atomic coverage container.
 */
public final class NucleotideCoverage implements Serializable {
    final AtomicIntegerArray coverage;
    final int size;

    /**
     * Creates nucleotide coverage container with given length.
     *
     * @param size size of container
     */
    public NucleotideCoverage(int size) {
        this.size = size;
        this.coverage = new AtomicIntegerArray(size * 4);
    }

    /**
     * Increments coverage value for a given letter in a given position.
     *
     * @param position position in sequence
     * @param letter   letter
     * @return value after increment
     */
    public int incrementCoverage(int position, int letter) {
        return coverage.incrementAndGet(4 * position + letter);
    }

    public int incrementCoverage(int position, int letter, int count) {
        return coverage.addAndGet(4 * position + letter, count);
    }

    /**
     * Decrements coverage value for a given letter in a given position.
     *
     * @param position position in sequence
     * @param letter   letter
     * @return value after decrement
     */
    public int decrementCoverage(int position, int letter) {
        return coverage.decrementAndGet(4 * position + letter);
    }

    public int decrementCoverage(int position, int letter, int count) {
        return coverage.addAndGet(4 * position + letter, -count);
    }


    /**
     * Adds given delta to a given letter in a given position.
     *
     * @param position position in sequence
     * @param letter   letter
     * @param delta    delta value
     * @return value after addition
     */
    public int addCoverage(int position, int letter, int delta) {
        return coverage.addAndGet(4 * position + letter, delta);
    }

    /**
     * Returns the coverage value
     *
     * @param position
     * @param letter
     * @return
     */
    public int getCoverage(int position, int letter) {
        return coverage.get(4 * position + letter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NucleotideCoverage)) return false;

        NucleotideCoverage that = (NucleotideCoverage) o;

        if (size != that.size) return false;
        if (!coverage.equals(that.coverage)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = coverage.hashCode();
        result = 31 * result + size;
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("NucleotideCoverage\n");
        for (int l = 0; l < 4; ++l) {
            for (int i = 0; i < size; ++i)
                sb.append(getCoverage(i, l)).append("\t");
            sb.deleteCharAt(sb.length() - 1); //Removing last "\t"
            sb.append("\n");
        }
        sb.deleteCharAt(sb.length() - 1); //Removing last "\n"
        return sb.toString();
    }
}
