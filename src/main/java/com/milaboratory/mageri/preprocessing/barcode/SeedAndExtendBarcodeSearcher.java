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
package com.milaboratory.mageri.preprocessing.barcode;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.mageri.misc.QualityDefaults;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.milaboratory.mageri.preprocessing.barcode.BarcodeUtil.charToRegex;
import static com.milaboratory.mageri.preprocessing.barcode.BarcodeUtil.compareRedundant;


public class SeedAndExtendBarcodeSearcher implements BarcodeSearcher {
    protected final Pattern seed;
    protected final String barcodeSeqeunce;
    protected int seedPatternStart, seedPatternEnd;
    protected final Set<Integer> umiPositions = new HashSet<>(),
            seedPositions = new HashSet<>();

    protected final int maxTruncations;
    protected final byte lowQualityThreshold;
    protected final int maxGoodMMs, maxLowQualityMMs;

    public SeedAndExtendBarcodeSearcher(String signature) {
        this(signature, 2, 0.05, signature.length(), QualityDefaults.PH33_BAD_QUAL);
    }

    public SeedAndExtendBarcodeSearcher(String signature, int maxTruncations,
                                        double maxGoodMMRatio, double maxLowQualityMMRatio,
                                        byte lowQualityThreshold) {
        this(signature, maxTruncations,
                (int) (signature.length() * maxGoodMMRatio), (int) (signature.length() * maxLowQualityMMRatio),
                lowQualityThreshold);
    }

    public SeedAndExtendBarcodeSearcher(String signature, int maxTruncations,
                                        int maxGoodMMs, int maxLowQualityMMs,
                                        byte lowQualityThreshold) {
        this.maxTruncations = maxTruncations;
        this.maxGoodMMs = maxGoodMMs;
        this.maxLowQualityMMs = maxLowQualityMMs;
        this.lowQualityThreshold = lowQualityThreshold;

        // Generate seed
        String seedRegex = "";
        for (int i = 0; i < signature.length(); i++) {
            if (Character.isUpperCase(signature.charAt(i))) {
                seedPatternStart = i; // inclusive
                break;
            }
        }
        for (int i = signature.length() - 1; i >= 0; i--) {
            if (Character.isUpperCase(signature.charAt(i))) {
                seedPatternEnd = i + 1; // exclusive
                break;
            }
        }

        for (int i = seedPatternStart; i < seedPatternEnd; i++) {
            seedRegex += charToRegex(signature.charAt(i));
        }

        seed = Pattern.compile(seedRegex);

        // For later use
        for (int i = 0; i < signature.length(); i++) {
            if (Character.isUpperCase(signature.charAt(i))) {
                seedPositions.add(i);
                if (signature.charAt(i) == BarcodeUtil.UMI_MARK)
                    umiPositions.add(i);
            }
        }
        barcodeSeqeunce = signature.toUpperCase();
    }

    public BarcodeSearcherResult search(NucleotideSQPair read) {
        String sequence = read.getSequence().toString();

        // Find all matches
        Matcher matcher = seed.matcher(sequence);

        // Iterate
        int seedPosition, position;
        int nLeftTruncations, nRightTruncations, nTruncations, goodMMs, lowQualityMMs;
        boolean found;
        String umi;

        byte umiWorstQual = Byte.MAX_VALUE;
        while (matcher.find()) {
            seedPosition = matcher.start();
            position = seedPosition - seedPatternStart;

            // Calculate number of nts truncated from barcode sequence
            nLeftTruncations = Math.max(0, seedPatternStart - seedPosition);
            nRightTruncations = Math.max(0, ((barcodeSeqeunce.length() - seedPatternEnd) -
                    (sequence.length() - (position + seedPatternEnd))));

            if ((nTruncations = nLeftTruncations + nRightTruncations) <= maxTruncations) {
                umi = "";

                goodMMs = 0;
                lowQualityMMs = 0;
                found = true;

                // Now do fuzzy matching
                for (int i = nLeftTruncations, j; i < barcodeSeqeunce.length() - nRightTruncations; i++) {
                    // in coordinates of seed
                    j = i + position; // in coordinates of read
                    if (seedPositions.contains(i)) {
                        // Seed region was matched
                        if (umiPositions.contains(i)) {
                            // Append UMI
                            umi += sequence.charAt(j);
                            umiWorstQual = (byte) Math.min(umiWorstQual, read.getQuality().value(j));
                        }
                    } else {
                        // Check for mismatche
                        if (!compareRedundant(barcodeSeqeunce.charAt(i), sequence.charAt(j))) {
                            if (read.getQuality().value(j) <= lowQualityThreshold)
                                lowQualityMMs++;
                            else
                                goodMMs++;
                        }

                        // Check for fuzzy conditions to break
                        if (lowQualityMMs > maxLowQualityMMs || goodMMs > maxGoodMMs) {
                            found = false;
                            break;
                        }
                    }
                }

                // Until first good match
                if (found) {
                    return new BarcodeSearcherResult(new NucleotideSequence(umi), umiWorstQual,
                            goodMMs, lowQualityMMs, nTruncations,
                            nLeftTruncations + position,
                            barcodeSeqeunce.length() + position - nRightTruncations);
                }
            }
        }

        return null;
    }
}
