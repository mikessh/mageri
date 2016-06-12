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

package com.antigenomics.mageri.pipeline.input;

import com.antigenomics.mageri.core.ReadSpecific;

import java.io.InputStream;
import java.io.Serializable;

public class InputChunk implements ReadSpecific, Serializable {
    protected transient final InputStream inputStream1, inputStream2;
    protected final String name;
    protected final CheckoutRule checkoutRule;

    public InputChunk(InputStream inputStream1, InputStream inputStream2,
                      String name, CheckoutRule checkoutRule) {
        this.inputStream1 = inputStream1;
        this.inputStream2 = inputStream2;
        this.name = name;
        this.checkoutRule = checkoutRule;
    }

    public InputStream getInputStream1() {
        return inputStream1;
    }

    public InputStream getInputStream2() {
        return inputStream2;
    }

    public String getName() {
        return name;
    }

    public CheckoutRule getCheckoutRule() {
        return checkoutRule;
    }

    @Override
    public boolean isPairedEnd() {
        return inputStream2 != null;
    }

    @Override
    public String toString() {
        String out = name + "\n-paired:" + isPairedEnd() + "\n-checkout:";
        String[] tokens = checkoutRule.toString().split("\n");
        for (String token : tokens) {
            out += "\n-" + token;
        }
        return out;
    }
}
