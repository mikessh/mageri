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
package com.milaboratory.oncomigec.core.align.entity;

public class PAlignmentResult {
    private final SAlignmentResult result1, result2;

    public PAlignmentResult(SAlignmentResult result1, SAlignmentResult result2) {
        this.result1 = result1;
        this.result2 = result2;
    }

    public boolean isChimeric() {
        return !result1.getReference().equals(result2.getReference());
    }

    public SAlignmentResult getResult1() {
        return result1;
    }

    public SAlignmentResult getResult2() {
        return result2;
    }
}
