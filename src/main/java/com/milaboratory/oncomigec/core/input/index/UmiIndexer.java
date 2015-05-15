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

package com.milaboratory.oncomigec.core.input.index;

import cc.redberry.pipe.Processor;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.milaboratory.oncomigec.preprocessing.CheckoutResult;
import com.milaboratory.oncomigec.preprocessing.CheckoutProcessor;
import com.milaboratory.oncomigec.misc.ProcessorResultWrapper;

public class UmiIndexer implements Processor<SequencingRead, ProcessorResultWrapper<IndexingInfo>> {
    private final CheckoutProcessor checkoutProcessor;
    private final byte umiQualityThreshold;
    private final ReadWrappingFactory readWrappingFactory;

    public UmiIndexer(CheckoutProcessor checkoutProcessor,
                      byte umiQualityThreshold,
                      ReadWrappingFactory readWrappingFactory) {
        this.checkoutProcessor = checkoutProcessor;
        this.umiQualityThreshold = umiQualityThreshold;
        this.readWrappingFactory = readWrappingFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ProcessorResultWrapper<IndexingInfo> process(SequencingRead milibRead) {
        CheckoutResult result = checkoutProcessor.checkout(milibRead);

        if (result != null && result.isGood(umiQualityThreshold)) {
            String sampleName = result.getSampleName();
            NucleotideSequence umi = result.getUmi();
            ReadContainer readContainer = readWrappingFactory.wrap(milibRead);

            ReadInfo readInfo = new ReadInfo(readContainer, result);

            return new ProcessorResultWrapper<>(new IndexingInfo(readInfo, sampleName, umi));
        }

        return ProcessorResultWrapper.BLANK;
    }

    public CheckoutProcessor getCheckoutProcessor() {
        return checkoutProcessor;
    }
}
