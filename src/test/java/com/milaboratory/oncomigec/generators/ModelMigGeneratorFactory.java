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

package com.milaboratory.oncomigec.generators;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.variant.ErrorModel;

public class ModelMigGeneratorFactory {
    private double hotSpotPositionRatio = 0.1, pcrPositionRatio = 0.4,
            somaticMutationRatio = 0.1, somaticMutationFreq = 0.0005;
    private ErrorModel errorModel = new ErrorModel();
    private MutationGenerator readErrorGenerator = MutationGenerator.NO_INDEL,
            pcrErrorGenerator = MutationGenerator.NO_INDEL_SKEWED,
            pcrHotSpotErrorGenerator = pcrErrorGenerator.multiply(errorModel.getPropagateProb());

    public ModelMigGenerator create(NucleotideSequence reference) {
        return new ModelMigGenerator(hotSpotPositionRatio, pcrPositionRatio, somaticMutationRatio,
                somaticMutationFreq, errorModel, readErrorGenerator,
                pcrErrorGenerator, pcrHotSpotErrorGenerator, reference);
    }

    public double getHotSpotPositionRatio() {
        return hotSpotPositionRatio;
    }

    public double getPcrPositionRatio() {
        return pcrPositionRatio;
    }

    public double getSomaticMutationRatio() {
        return somaticMutationRatio;
    }

    public double getSomaticMutationFreq() {
        return somaticMutationFreq;
    }

    public ErrorModel getErrorModel() {
        return errorModel;
    }

    public MutationGenerator getReadErrorGenerator() {
        return readErrorGenerator;
    }

    public MutationGenerator getPcrErrorGenerator() {
        return pcrErrorGenerator;
    }

    public void setHotSpotPositionRatio(double hotSpotPositionRatio) {
        this.hotSpotPositionRatio = hotSpotPositionRatio;
    }

    public void setPcrPositionRatio(double pcrPositionRatio) {
        this.pcrPositionRatio = pcrPositionRatio;
    }

    public void setSomaticMutationRatio(double somaticMutationRatio) {
        this.somaticMutationRatio = somaticMutationRatio;
    }

    public void setSomaticMutationFreq(double somaticMutationFreq) {
        this.somaticMutationFreq = somaticMutationFreq;
    }

    public void setErrorModel(ErrorModel errorModel) {
        this.errorModel = errorModel;
        pcrHotSpotErrorGenerator = pcrErrorGenerator.multiply(errorModel.getPropagateProb());
    }

    public void setReadErrorGenerator(MutationGenerator readErrorGenerator) {
        this.readErrorGenerator = readErrorGenerator;
    }

    public void setPcrErrorGenerator(MutationGenerator pcrErrorGenerator) {
        this.pcrErrorGenerator = pcrErrorGenerator;
        pcrHotSpotErrorGenerator = pcrErrorGenerator.multiply(errorModel.getPropagateProb());
    }
}
