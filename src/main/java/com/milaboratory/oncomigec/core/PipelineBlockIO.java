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
 * Last modified on 12.3.2015 by mikesh
 */

package com.milaboratory.oncomigec.core;

import com.milaboratory.oncomigec.pipeline.SerializationUtils;

import java.io.File;
import java.io.IOException;

public class PipelineBlockIO<BlockType extends PipelineBlock> {
    private final String name;
    private final int stage;

    public PipelineBlockIO(String name, int stage) {
        this.name = name;
        this.stage = stage;
    }

    protected void writeToTabular(BlockType block, String outputPrefix) throws IOException {
        SerializationUtils.writeStringToFile(new File(outputPrefix + ".txt"), block.toString());
    }

    public void writeTo(BlockType block, String outputPrefix, boolean binaryOnly) throws IOException {
        outputPrefix = outputPrefix + "." + stage + "." + name;
        SerializationUtils.writeObjectToFile(new File(outputPrefix + ".bin"), block);
        if (!binaryOnly)
            writeToTabular(block, outputPrefix);
    }

    @SuppressWarnings("unchecked")
    public BlockType readFrom(String outputPrefix) throws IOException, ClassNotFoundException {
        return (BlockType) SerializationUtils.readObjectFromFile(new File(outputPrefix + "." + stage + "." + name + ".bin"));
    }

    public String getName() {
        return name;
    }

    public int getStage() {
        return stage;
    }
}
