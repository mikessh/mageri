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

package com.antigenomics.mageri.core;

import com.antigenomics.mageri.pipeline.SerializationUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public abstract class PipelineBlock implements Serializable {
    private final String name;

    protected PipelineBlock(String name) {
        this.name = name;
    }

    public void writePlainText(String pathPrefix) throws IOException {
        SerializationUtils.writeStringToFile(new File(pathPrefix + "." + name + ".txt"), toTabular());
    }

    // R-compatible output
    private String toTabular() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String commentLine : toString().split("\n")) {
            stringBuilder.append("#").append(commentLine).append("\n");
        }
        stringBuilder.append(getHeader());
        stringBuilder.append("\n").append(getBody());
        return stringBuilder.toString();
    }

    public abstract String getHeader();

    public abstract String getBody();

    @Override
    public String toString() {
        return "pipeline_block";
    }
}
