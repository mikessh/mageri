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

import com.milaboratory.oncomigec.pipeline.Speaker;
import com.milaboratory.oncomigec.preprocessing.barcode.BarcodeListParser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class InputParser {
    private final IOProvider ioProvider;

    public InputParser(IOProvider ioProvider) {
        this.ioProvider = ioProvider;
    }

    public InputParser() {
        this(new FileIOProvider());
    }

    public Input parseJson(String jsonFilePath) throws IOException {
        String jsonStr = ioProvider.read(jsonFilePath);

        JSONObject rootObject = new JSONObject(jsonStr);

        // Parse project structure
        List<InputChunk> chunks = new ArrayList<>();

        JSONArray structureArr = rootObject.getJSONArray("structure");

        for (int i = 0; i < structureArr.length(); i++) {
            JSONObject structureObject = structureArr.getJSONObject(i);

            // parse json chunks
            if (structureObject.has("byindex")) {
                JSONArray byindex = structureObject.getJSONArray("byindex");
                chunks.addAll(parseChunks(byindex));
            }

            // parse chunks that are listed in tabular format
            if (structureObject.has("tabular")) {
                JSONObject tabular = structureObject.getJSONObject("tabular");
                chunks.addAll(parseChunks(tabular));
            }
        }

        String projectName = rootObject.getString("project"),
                referencesFileName = rootObject.getString("references");
        InputStreamWrapper references = ioProvider.getWrappedStream(referencesFileName),
                bedFile = null, contigFile = null;

        if (rootObject.has("bed") && rootObject.has("contigs")) {
            bedFile = ioProvider.getWrappedStream(rootObject.getString("bed"));
            contigFile = ioProvider.getWrappedStream(rootObject.getString("contigs"));
        } else if (rootObject.has("bed")) {
            Speaker.INSTANCE.sout("BED file is skipped, as it doesn't have contig length", 1);
        }

        return new Input(projectName, references, bedFile, contigFile, chunks);
    }

    private List<InputChunk> parseChunks(JSONObject table) throws IOException {
        List<InputChunk> chunks = new ArrayList<>();

        // index(tab)fastq1(tab)fastq2 table
        String indexTablePath = table.getString("file");

        List<String> rows = ioProvider.readLines(indexTablePath);

        for (int i = 0; i < rows.size(); i++) { // Loop over each each row
            String row = rows.get(i);
            if (!row.startsWith(BarcodeListParser.COMMENT)) {
                String[] tokenized = row.split(BarcodeListParser.SEPARATOR);
                String chunkName = tokenized[0],
                        fastq1FileName = tokenized[1],
                        fastq2FileName = tokenized[2].equals(BarcodeListParser.EMPTY_BARCODE) ?
                                null : tokenized[2];
                boolean paired = fastq2FileName != null;

                // apply rule to each index
                CheckoutRule checkoutRule = getCheckoutRule(chunkName, table, paired);

                if (checkoutRule == null)
                    throw new RuntimeException("No multiplex rule is specified for chunk " + chunkName);

                InputStream fasq1 = ioProvider.getStream(fastq1FileName),
                        fastq2 = paired ? ioProvider.getStream(fastq2FileName) : null;

                chunks.add(new InputChunk(fasq1, fastq2, chunkName, checkoutRule));
            }
        }

        return chunks;
    }

    private List<InputChunk> parseChunks(JSONArray byIndex) throws IOException {
        List<InputChunk> chunks = new ArrayList<>();
        for (int i = 0; i < byIndex.length(); i++) { // Loop over each each row
            JSONObject rule = byIndex.getJSONObject(i); // Get row object

            String chunkName = rule.getString("index"),
                    fastq1FileName = rule.getString("r1"),
                    fastq2FileName = rule.has("r2") ? rule.getString("r2") : null;
            boolean paired = fastq2FileName != null;

            CheckoutRule checkoutRule = getCheckoutRule(chunkName, rule, paired);

            if (checkoutRule == null)
                throw new RuntimeException("No multiplex rule is specified for chunk " + chunkName);

            InputStream fasq1 = ioProvider.getStream(fastq1FileName),
                    fastq2 = paired ? ioProvider.getStream(fastq2FileName) : null;

            chunks.add(new InputChunk(fasq1, fastq2, chunkName, checkoutRule));
        }
        return chunks;
    }

    private CheckoutRule getCheckoutRule(String chunkName, JSONObject rule,
                                         boolean paired) throws IOException {
        CheckoutRule checkoutRule = null;
        if (rule.has("submultiplex")) {
            JSONObject details = rule.getJSONObject("submultiplex");
            String barcodesFileName = details.getString("file");
            checkoutRule = new SubMultiplexRule(chunkName,
                    ioProvider.readLines(barcodesFileName), paired);
        }
        if (rule.has("primer")) {
            if (checkoutRule != null)
                throw new RuntimeException("More than one multiplex rule is specified for chunk " + chunkName);
            JSONObject details = rule.getJSONObject("primer");
            String barcodesFileName = details.getString("file");
            checkoutRule = new PrimerRule(chunkName,
                    ioProvider.readLines(barcodesFileName), paired);
        }
        if (rule.has("positional")) {
            if (checkoutRule != null)
                throw new RuntimeException("More than one multiplex rule is specified for chunk " + chunkName);
            JSONObject details = rule.getJSONObject("positional");
            String mask1 = details.getString("mask1"),
                    mask2 = details.has("mask2") ? details.getString("mask2") : null;

            checkoutRule = new PositionalRule(chunkName, mask1, mask2, paired);
        }
        if (rule.has("preprocessed")) {
            if (checkoutRule != null)
                throw new RuntimeException("More than one multiplex rule is specified for chunk " + chunkName);
            checkoutRule = new PreprocessedRule(chunkName);
        }

        return checkoutRule;
    }
}
