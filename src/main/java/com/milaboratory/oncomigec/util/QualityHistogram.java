/**
 * Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)
 * <p/>
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
 */

package com.milaboratory.oncomigec.util;

import com.milaboratory.core.sequence.quality.SequenceQualityPhred;

import java.util.concurrent.atomic.AtomicLongArray;

import static com.milaboratory.oncomigec.util.Util.PH33_MAX_QUAL;
import static com.milaboratory.oncomigec.util.Util.PH33_MIN_QUAL;

public class QualityHistogram {
    private final static int length = PH33_MAX_QUAL - PH33_MIN_QUAL + 1;
    private final AtomicLongArray hist = new AtomicLongArray(length);

    public QualityHistogram() {

    }

    public void append(QualityHistogram other) {
        for (int i = 0; i < length; i++) {
            hist.addAndGet(i, other.hist.get(i));
        }
    }

    public void append(SequenceQualityPhred qualityPhred) {
        for (byte q : qualityPhred.getInnerData())
            hist.incrementAndGet(q - PH33_MIN_QUAL);
    }

    public long getAt(byte q) {
        if (q < PH33_MIN_QUAL || q > PH33_MAX_QUAL)
            throw new IndexOutOfBoundsException();
        int qq = q - PH33_MIN_QUAL;
        return hist.get(qq);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("qual");
        for (int i = 0; i < length; i++)
            sb.append("\t").append(i + PH33_MIN_QUAL);
        sb.append("\ncount");
        for (byte i = 0; i < length; i++)
            sb.append("\t").append(hist.get(i));
        return sb.toString();
    }
}
