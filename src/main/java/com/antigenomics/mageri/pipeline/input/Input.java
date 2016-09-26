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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An object representing input metadata that is provided in json + tab delimited format
 */
public class Input implements Serializable {
    protected final String projectName;
    protected final InputStreamWrapper references, bedFile, contigFile;
    protected final Map<String, InputChunk> inputChunksByIndex;

    public Input(String projectName,
                 InputStreamWrapper references, InputStreamWrapper bedFile, InputStreamWrapper contigFile,
                 List<InputChunk> inputChunks) {
        this.projectName = projectName;
        this.references = references;
        this.bedFile = bedFile;
        this.contigFile = contigFile;
        this.inputChunksByIndex = new HashMap<>();
        for (InputChunk inputChunk : inputChunks) {
            inputChunksByIndex.put(inputChunk.getName(), inputChunk);
        }
    }

    public Input(String projectName,
                 InputStreamWrapper references, InputStreamWrapper bedFile, InputStreamWrapper contigFile,
                 InputChunk... inputChunks) {
        this.projectName = projectName;
        this.references = references;
        this.bedFile = bedFile;
        this.contigFile = contigFile;
        this.inputChunksByIndex = new HashMap<>();
        for (InputChunk inputChunk : inputChunks) {
            inputChunksByIndex.put(inputChunk.getName(), inputChunk);
        }
    }

    private Input(String projectName,
                  InputStreamWrapper references, InputStreamWrapper bedFile, InputStreamWrapper contigFile,
                  Map<String, InputChunk> inputChunksByIndex) {
        this.projectName = projectName;
        this.references = references;
        this.bedFile = bedFile;
        this.contigFile = contigFile;
        this.inputChunksByIndex = inputChunksByIndex;
    }

    public Input rename(String newName) {
        return new Input(newName, references, bedFile, contigFile, inputChunksByIndex);
    }

    public String getProjectName() {
        return projectName;
    }

    public InputStreamWrapper getReferences() {
        return references;
    }

    public boolean hasBedInfo() {
        return bedFile != null && contigFile != null;
    }

    public InputStreamWrapper getBedFile() {
        return bedFile;
    }

    public InputStreamWrapper getContigFile() {
        return contigFile;
    }

    public Collection<InputChunk> getInputChunks() {
        return inputChunksByIndex.values();
    }

    public InputChunk getByName(String name) {
        return inputChunksByIndex.get(name);
    }

    @Override
    public String toString() {
        String out = projectName;
        for (InputChunk chunk : inputChunksByIndex.values()) {
            String[] tokens = chunk.toString().split("\n");
            for (String token : tokens) {
                out += "\n-" + token;
            }
        }
        return out;
    }
}

