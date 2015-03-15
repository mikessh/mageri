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

package com.milaboratory.oncomigec.pipeline.input;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * An object representing input metadata that is provided in json + tab delimited format
 */
public class Input implements Serializable {
    protected final String projectName;

    protected transient final InputStream references;
    protected final List<InputChunk> inputChunks;

    public static Input fromJson(File jsonFile) throws IOException {
        List<InputChunk> chunks = new ArrayList<>();

        String jsonStr = FileUtils.readFileToString(jsonFile);

        JSONObject rootObject = new JSONObject(jsonStr);

        String projectName = rootObject.getString("project"),
                referencesFileName = rootObject.getString("references");

        JSONArray multiplex = rootObject.getJSONArray("multiplex");

        for (int i = 0; i < multiplex.length(); i++) { // Loop over each each row
            JSONObject rule = multiplex.getJSONObject(i); // Get row object

            String chunkName = rule.getString("index"),
                    fastq1FileName = rule.getString("r1"),
                    fastq2FileName = rule.has("r2") ? rule.getString("r2") : null;
            boolean paired = fastq2FileName != null;

            CheckoutRule checkoutRule = null;
            if (rule.has("submultiplex")) {
                String barcodesFileName = rule.getString("barcodes");
                checkoutRule = new SubMultiplexRule(barcodesFileName, paired);
            }
            if (rule.has("positional")) {
                if (checkoutRule != null)
                    throw new RuntimeException("More than one multiplex rule is specified for chunk " + chunkName);
                String sampleName = rule.getString("sampleName"),
                        mask1 = rule.getString("mask1"),
                        mask2 = rule.has("mask2") ? rule.getString("mask2") : null;

                checkoutRule = new PositionalRule(sampleName, mask1, mask2, paired);
            }
            if (rule.has("preprocessed")) {
                if (checkoutRule != null)
                    throw new RuntimeException("More than one multiplex rule is specified for chunk " + chunkName);
                String sampleName = rule.getString("sampleName");
                checkoutRule = new PreprocessedRule(sampleName);
            }

            if (checkoutRule == null)
                throw new RuntimeException("No multiplex rule is specified for chunk " + chunkName);

            // Take care for gzipped input
            InputStream fasq1 = new FileInputStream(fastq1FileName),
                    fastq2 = paired ? new FileInputStream(fastq2FileName) : null;

            if (fastq1FileName.endsWith(".gz"))
                fasq1 = new GZIPInputStream(fasq1);

            if (paired && fastq2FileName.endsWith(".gz"))
                fastq2 = new GZIPInputStream(fastq2);


            chunks.add(new InputChunk(fasq1, fastq2, chunkName, checkoutRule));
        }

        InputStream references = new FileInputStream(referencesFileName);

        return new Input(projectName, references, chunks);
    }

    public Input(String projectName, InputStream references, List<InputChunk> inputChunks) {
        this.projectName = projectName;
        this.references = references;
        this.inputChunks = inputChunks;
    }

    public String getProjectName() {
        return projectName;
    }

    public InputStream getReferences() {
        return references;
    }

    public List<InputChunk> getInputChunks() {
        return Collections.unmodifiableList(inputChunks);
    }
}

