/*
 * Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)
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
package com.milaboratory.oncomigec.core.io.misc;

import com.milaboratory.oncomigec.preproc.demultiplex.entity.CheckoutResult;

public class ReadInfo<SQPairType> {
    private final SQPairType read;
    private final boolean flipMe, rcMe;
    private final CheckoutResult checkoutResult;

    public ReadInfo(SQPairType read, boolean flipMe, boolean rcMe, CheckoutResult checkoutResult) {
        this.read = read;
        this.flipMe = flipMe;
        this.rcMe = rcMe;
        this.checkoutResult = checkoutResult;
    }

    public CheckoutResult getCheckoutResult() {
        return checkoutResult;
    }

    public SQPairType getRead() {
        return read;
    }

    public boolean flipMe() {
        return flipMe;
    }

    public boolean rcMe() {
        return rcMe;
    }
}
