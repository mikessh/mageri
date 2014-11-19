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
package com.milaboratory.migec2.core.haplotype.misc;

import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import com.milaboratory.migec2.core.align.reference.Reference;
import com.milaboratory.migec2.core.correct.CorrectorReferenceLibrary;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.BinomialDistribution;
import org.apache.commons.math.distribution.BinomialDistributionImpl;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class SimpleHaplotypeErrorStatistics implements HaplotypeErrorStatistics {
    private final CorrectorReferenceLibrary correctorReferenceLibrary;
    private final boolean symmetricalMatrix;
    private final Map<Reference, double[][]> substitutionProbabilityMap = new HashMap<>();

    public SimpleHaplotypeErrorStatistics(CorrectorReferenceLibrary correctorReferenceLibrary) {
        this(correctorReferenceLibrary, true);
    }

    public SimpleHaplotypeErrorStatistics(CorrectorReferenceLibrary correctorReferenceLibrary,
                                          boolean symmetricalMatrix) {
        this.correctorReferenceLibrary = correctorReferenceLibrary;
        this.symmetricalMatrix = symmetricalMatrix;
    }

    private double[][] getSubstitutionProbabilityMatrix(Reference reference) {
        double[][] substProbMatrix = substitutionProbabilityMap.get(reference);

        if (substProbMatrix == null) { // lazy precompute & store
            substProbMatrix = new double[4][4];
            //final double[][] totalCounts = new double[4][4];
            final double[] letterCounts = new double[4];

            //MutationsAndCoverage mutCov = correctorReferenceLibrary.getAlignerReferenceLibrary().
            //        getMutationsAndCoverage(reference);

            final boolean[] major = new boolean[4];
            int totalTrueMajorCount;

            final int n = reference.getSequence().size();

            for (int i = 0; i < n; i++) {
                // Get major alleles at given position
                totalTrueMajorCount = 0; // full major count - for normalization
                for (int j = 0; j < 4; j++) {
                    int majorCount = correctorReferenceLibrary.getCorrespondingMajorCount(reference, i, j),
                            minorCount = correctorReferenceLibrary.getCorrespondingMinorCount(reference, i, j);
                    letterCounts[j] += majorCount;
                    if (major[j] = correctorReferenceLibrary.isMajor(majorCount, minorCount))
                        totalTrueMajorCount += majorCount;
                }

                // Go through minor alleles
                for (int j = 0; j < 4; j++) {
                    if (!major[j]) {
                        // An error actually has some uncorrected consensuses - this is base frequency of error
                        int majorCountForError = correctorReferenceLibrary.getCorrespondingMajorCount(reference, i, j);

                        // Now go through all majors this error could originate from
                        for (int k = 0; k < 4; k++) {
                            if (major[k]) {
                                double factor = correctorReferenceLibrary.getCorrespondingMajorCount(reference, i, k) /
                                        (double) totalTrueMajorCount;
                                // add number of uncorrected mismatches
                                // to is the nucleotide that at a given position is not a true major overall
                                // all present true major alleles are chosen as from
                                // addition is weighted by true major allele presence
                                substProbMatrix[k][j] += majorCountForError * factor;
                                //totalCounts[k][j] += mutCov.getMajorNucleotideMigCount(i, k) * factor;
                            }
                        }
                    }
                }
            }

            // Each entry of matrix is proportional number of erroneous consensuses by total number of consensuses
            // It is weighted by letter frequency
            for (int i = 0; i < 4; i++)
                for (int j = 0; j < 4; j++) {
                    if (i == j)
                        substProbMatrix[i][j] = Double.NaN;
                    else {
                        if (substProbMatrix[i][j] == 0)
                            substProbMatrix[i][j]++; // regularization

                        substProbMatrix[i][j] *= n / letterCounts[i];// totalCounts[i][j];
                    }
                }

            if (symmetricalMatrix) {
                // Matrix symmetrization according to RC (for PCR)
                // Useful if sequencing only one strand
                double[][] substProbMatrixSymm = new double[4][4];
                for (int i = 0; i < 4; i++)
                    for (int j = 0; j < 4; j++)
                        substProbMatrixSymm[i][j] = 0.5 * (substProbMatrix[i][j] + substProbMatrix[3 - i][3 - j]);
                substProbMatrix = substProbMatrixSymm;
            }

            substitutionProbabilityMap.put(reference, substProbMatrix);
        }

        return substProbMatrix;
    }

    @Override
    public int totalCountForMutation(Reference reference, int mutation) {
        int totalCountForMutation = 0;
        int pos = Mutations.getPosition(mutation);

        switch (Mutations.getType(mutation)) {
            case Substitution:
                totalCountForMutation = correctorReferenceLibrary.
                        getMajorCount(reference, pos,
                                Mutations.getTo(mutation));
                break;
            case Insertion:
                totalCountForMutation = correctorReferenceLibrary.getMajorInsCount(reference, pos);

                break;
            case Deletion:
                totalCountForMutation = correctorReferenceLibrary.getMajorDelCount(reference, pos);
                break;
        }

        return totalCountForMutation;
    }

    @Override
    public double calculatePValue(Reference reference, int mutation, int parentCount, int childCount) throws MathException {
        double p;
        int pos = Mutations.getPosition(mutation);
        if (Mutations.isSubstitution(mutation))
            p = getSubstitutionProbabilityMatrix(reference)[Mutations.getFrom(mutation)][Mutations.getTo(mutation)];
        else
            p = 1.0 / correctorReferenceLibrary.getMajorCount(reference, pos);

        BinomialDistribution binomialDistribution = new BinomialDistributionImpl(parentCount + childCount, p);

        return 1.0 - binomialDistribution.cumulativeProbability(childCount) + 0.5 *
                binomialDistribution.probability(childCount);
    }

    @Override
    public String toString() {
        String formattedString = "#" + super.toString() + "\n";

        NumberFormat formatter = new DecimalFormat("0.0E0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        formatter.setMinimumFractionDigits(2);

        for (Map.Entry<Reference, double[][]> entry : substitutionProbabilityMap.entrySet()) {
            formattedString += entry.getKey().toString() + "\n";
            formattedString += "From\\To";
            for (byte i = 0; i < 4; i++) {
                formattedString += "\t" + NucleotideAlphabet.INSTANCE.symbolFromCode(i);
            }
            formattedString += "\n";
            for (byte i = 0; i < 4; i++) {
                formattedString += NucleotideAlphabet.INSTANCE.symbolFromCode(i);
                for (byte j = 0; j < 4; j++) {
                    formattedString += "\t" + (Double.isNaN(entry.getValue()[i][j]) ? "-" :
                            formatter.format(entry.getValue()[i][j]));
                }
                formattedString += "\n";
            }
            formattedString += "\n";
        }
        return formattedString;
    }
}
