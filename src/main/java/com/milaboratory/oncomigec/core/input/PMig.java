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
 * Last modified on 16.2.2015 by mikesh
 */

package com.milaboratory.oncomigec.core.input;

import com.milaboratory.oncomigec.core.Mig;

public class PMig extends Mig {
    private final SMig mig1, mig2;

    public PMig(SMig mig1, SMig mig2) {
        super(mig1.getSample(), mig1.getUmi());
        this.mig1 = mig1;
        this.mig2 = mig2;
        if (!mig1.getUmi().equals(mig2.getUmi()))
            throw new IllegalArgumentException("MIGs have different UMIs");
        if (!mig1.getSample().equals(mig2.getSample()))
            throw new IllegalArgumentException("MIGs are from different samples");
    }

    @Override
    public int size() {
        return mig1.size();
    }

    public SMig getMig1() {
        return mig1;
    }

    public SMig getMig2() {
        return mig2;
    }

    @Override
    public boolean isPairedEnd() {
        return true;
    }
}
