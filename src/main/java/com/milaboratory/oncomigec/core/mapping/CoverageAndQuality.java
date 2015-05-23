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
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * Singapore-style atomic coverage container.
 */
public final class CoverageAndQuality implements Serializable {
    final AtomicLongArray qualitySum;
    final AtomicIntegerArray coverage;
    final int size;

    /**
     * Creates nucleotide coverage container with given length.
     *
     * @param size size of container
     */
    public CoverageAndQuality(int size) {
        this.size = size;
        this.qualitySum = new AtomicLongArray(size * 4);
        this.coverage = new AtomicIntegerArray(size);
    }

    /**
     * Increments coverage value for a given letter in a given position.
     *
     * @param position position in sequence
     * @param letter   letter
     * @param by       quality value
     */
    public void increaseQualityAndCoverage(int position, int letter, byte by) {
        coverage.incrementAndGet(position);
        qualitySum.addAndGet(4 * position + letter, by);
    }

    /**
     * Decrements coverage value for a given letter in a given position.
     *
     * @param position position in sequence
     * @param letter   letter
     * @param by       quality value
     */
    public void decreaseQualityAndCoverage(int position, int letter, byte by) {
        coverage.decrementAndGet(position);
        qualitySum.addAndGet(4 * position + letter, -by);
    }


    /**
     * Returns the coverage value
     *
     * @param position
     * @return
     */
    public int getCoverage(int position) {
        return coverage.get(position);
    }

    /**
     * Returns the coverage value
     *
     * @param position
     * @return
     */
    public double getAverageQuality(int position, int letter) {
        double qualSum = qualitySum.get(4 * position + letter);
        return qualSum / coverage.get(position);
    }
}
