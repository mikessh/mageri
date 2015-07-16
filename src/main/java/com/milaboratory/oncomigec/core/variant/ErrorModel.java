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

package com.milaboratory.oncomigec.core.variant;

import java.io.Serializable;

public class ErrorModel implements Serializable {
    public static final int COVERAGE_THRESHOLD = 100;

    private double order;
    private double cycles, lambda;
    private double propagateProb;

    public ErrorModel() {
        this(1.0, 20, 0.95);
    }

    public ErrorModel(double order, double cycles, double lambda) {
        this.order = order;
        this.cycles = cycles;
        this.lambda = lambda;
        calcPropagateProb();
    }

    public double getErrorRate(int minorCount, int total,
                               int from, int to,
                               MinorMatrix minorMatrix) {
        minorCount = minorCount > 0 ? minorCount : 1;

        double rate = Math.max(total < COVERAGE_THRESHOLD ? 1.0 / (double) total : (minorCount / (double) total),
                minorMatrix.getRate(from, to));

        double errorRateBase = Math.pow(1.0 - rate, 1.0 / cycles);

        return (Math.log(lambda) - Math.log((1.0 + lambda) * errorRateBase - 1)) * propagateProb;
    }

    public double getCycles() {
        return cycles;
    }

    public double getLambda() {
        return lambda;
    }

    public void setCycles(double cycles) {
        this.cycles = cycles;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
        calcPropagateProb();
    }

    public double getOrder() {
        return order;
    }

    public void setOrder(double order) {
        this.order = order;
        calcPropagateProb();
    }

    public double getPropagateProb() {
        return propagateProb;
    }

    private void calcPropagateProb() {
        this.propagateProb = Math.pow((1.0 - lambda), order) * Math.pow(lambda, order + 1);
    }
}
