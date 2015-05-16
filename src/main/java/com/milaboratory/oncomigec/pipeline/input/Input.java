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

package com.milaboratory.oncomigec.pipeline.input;

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
    protected final Map<String, InputChunk> inputChunksByIndex = new HashMap<>();

    public Input(String projectName,
                 InputStreamWrapper references, InputStreamWrapper bedFile, InputStreamWrapper contigFile,
                 List<InputChunk> inputChunks) {
        this.projectName = projectName;
        this.references = references;
        this.bedFile = bedFile;
        this.contigFile = contigFile;
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
        for (InputChunk inputChunk : inputChunks) {
            inputChunksByIndex.put(inputChunk.getName(), inputChunk);
        }
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

