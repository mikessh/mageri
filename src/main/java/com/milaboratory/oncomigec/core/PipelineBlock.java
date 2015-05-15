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

package com.milaboratory.oncomigec.core;

import com.milaboratory.oncomigec.pipeline.SerializationUtils;

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
