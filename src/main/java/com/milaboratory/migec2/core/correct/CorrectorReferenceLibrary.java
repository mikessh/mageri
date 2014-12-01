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
package com.milaboratory.migec2.core.correct;

import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import com.milaboratory.migec2.core.align.reference.Reference;
import com.milaboratory.migec2.core.consalign.entity.AlignerReferenceLibrary;
import com.milaboratory.migec2.core.consalign.mutations.MutationsAndCoverage;
import com.milaboratory.migec2.model.classifier.ClassifierResult;
import com.milaboratory.migec2.model.classifier.VariantClassifier;
import com.milaboratory.migec2.model.variant.Variant;
import com.milaboratory.migec2.model.variant.VariantContainer;
import com.milaboratory.migec2.model.variant.VariantLibrary;
import org.apache.commons.math.distribution.BinomialDistribution;
import org.apache.commons.math.distribution.BinomialDistributionImpl;

import java.util.*;

public final class CorrectorReferenceLibrary {
    private final HashMap<Reference, MutationFilter> mutationFilterByReference = new HashMap<>();
    private final HashMap<Reference, int[][]> majorSubstitutionCountMap = new HashMap<>();
    private final HashMap<Reference, int[]> majorInsertionCountMap = new HashMap<>(),
            majorDeletionCountMap = new HashMap<>();
    private final HashMap<Reference, double[][]> majorSubstitutionPvalueMap = new HashMap<>();
    private final HashMap<Reference, double[]> majorInsertionPvalueMap = new HashMap<>(),
            majorDeletionPvalueMap = new HashMap<>();

    private final VariantLibrary variantLibrary;
    private final AlignerReferenceLibrary alignerReferenceLibrary;
    private final List<Reference> references;

    private final VariantClassifier variantClassifier;

    // Hot-spot p-value DEPRECATED
    private final double majorPvalueThreshold, pcrEfficiency;
    private final int pcrCycles;

    // Filtering
    private final boolean filterSingleMigs;
    private final int minMigCoverage;
    private final byte minAvgQuality;
    private final double maxBasePairsMaskedRatio;
    private final int minMigCount;

    public CorrectorReferenceLibrary(AlignerReferenceLibrary alignerReferenceLibrary,
                                     CorrectorParameters parameters,
                                     VariantClassifier variantClassifier) {
        // Hot-spot p-value
        this.majorPvalueThreshold = parameters.getMajorPvalueThreshold();
        this.pcrEfficiency = parameters.getPcrEfficiency();
        this.pcrCycles = parameters.getPcrCycles();
        this.variantClassifier = variantClassifier;

        // Filtering
        this.filterSingleMigs = parameters.filterSingleMigs();
        this.minMigCoverage = parameters.getMinMigCoverage();
        this.minAvgQuality = parameters.getMinAvgQuality();
        this.maxBasePairsMaskedRatio = parameters.getMaxBasePairsMaskedRatio();
        this.minMigCount = parameters.getMinMigCount();

        // Store mutations that passed filtration
        // Record if reference at given position also exists
        this.alignerReferenceLibrary = alignerReferenceLibrary;
        this.references = new ArrayList<>(alignerReferenceLibrary.getReferenceLibrary().getReferences());

        this.variantLibrary = new VariantLibrary(alignerReferenceLibrary);
        init();
    }

    private void init() {
        Collection<Reference> skippedReferences = new LinkedList<>();

        for (Reference reference : references) {
            MutationsAndCoverage mutationsAndCoverage =
                    alignerReferenceLibrary.getMutationsAndCoverage(reference);

            if (mutationsAndCoverage.wasUpdated()) {
                int n = reference.getSequence().size();
                int numberOfMigs = mutationsAndCoverage.getMigCount();
                boolean[][] substitutionsByPosition = new boolean[n][4];
                int[][] majorSubstitutionCounts = new int[n][4];
                int[] majorInsertionCounts = new int[n], majorDeletionCounts = new int[n];
                double[][] majorSubstitutionPvalues = new double[n][4];
                double[] majorInsertionPvalues = new double[n], majorDeletionPvalues = new double[n];
                boolean[] referencePresenceByPosition = new boolean[n],
                        coverageFilterByPosition = new boolean[n],
                        qualityFilterByPosition = new boolean[n];

                int nMustHaveMutations = 0, nBadBases = 0;

                final VariantContainer variantContainer = variantLibrary.getVariantContainer(reference);

                // SUBSTITUTIONS

                for (int i = 0; i < n; i++) {
                    // Compute quality and coverage
                    long umiCoverage = mutationsAndCoverage.getReferenceUmiCoverage(i);
                    long avgQual = umiCoverage == 0 ? 0 :
                            mutationsAndCoverage.getReferenceQualitySumCoverage(i) / umiCoverage;

                    coverageFilterByPosition[i] = umiCoverage >= minMigCoverage;
                    qualityFilterByPosition[i] = avgQual >= minAvgQuality;

                    // Not quite sure here, but we'll carry on and do our best
                    if (!coverageFilterByPosition[i] || !qualityFilterByPosition[i])
                        nBadBases++;

                    // here we apply classifier and use variant library for querying
                    // as it has complete stats that could be required by classifier
                    for (byte j = 0; j < 4; j++) {
                        int majorMigCount = mutationsAndCoverage.getMajorNucleotideMigCount(i, j);
                        Variant minorVariant = variantContainer.getAt(i, j);
                        boolean variantExists = false;

                        if (minorVariant != null) {
                            // Classify minor variants
                            ClassifierResult result = variantClassifier.classify(minorVariant);
                            variantExists = result.passed();
                            majorSubstitutionPvalues[i][j] = result.getPValue();
                        } else if (majorMigCount > 0) {
                            // Retain all major variants
                            variantExists = true;
                            majorSubstitutionPvalues[i][j] = 0.0;
                        } else {
                            // Nothing here
                            majorSubstitutionPvalues[i][j] = 1.0;
                        }

                        if (variantExists) {
                            majorSubstitutionCounts[i][j] = majorMigCount;
                            if (reference.getSequence().codeAt(i) == j)
                                referencePresenceByPosition[i] = true;
                            else
                                substitutionsByPosition[i][j] = true;
                        }
                    }

                    // Have a hole in reference
                    if (!referencePresenceByPosition[i])
                        nMustHaveMutations++;
                }

                // Is sufficiently covered?
                boolean good = (nBadBases / (double) n) <= maxBasePairsMaskedRatio &&
                        mutationsAndCoverage.getMigCount() >= minMigCount;

                // Finally deal with INDELS
                // NOTE completely frequency-based for now
                Set<Integer> indels = new HashSet<>();
                for (Integer indel : mutationsAndCoverage.getMajorIndelCodes()) {
                    int majorCount = mutationsAndCoverage.getMajorIndelMigCount(indel);
                    boolean isDeletion = Mutations.isDeletion(indel);
                    int pos = Mutations.getPosition(indel);

                    // todo: re-implement?
                    if (majorCount > 1 && majorCount / (double) numberOfMigs > 1e-3) {
                        indels.add(indel);
                        if (isDeletion)
                            majorDeletionCounts[pos]++;
                        else
                            majorInsertionCounts[pos]++;
                    }

                    if (isDeletion)
                        majorDeletionPvalues[pos] = Double.NaN;
                    else
                        majorInsertionPvalues[pos] = Double.NaN;
                }

                // Mutation filter -

                mutationFilterByReference.put(reference, new MutationFilter(substitutionsByPosition,
                        referencePresenceByPosition, qualityFilterByPosition, coverageFilterByPosition,
                        indels, good, nMustHaveMutations));

                majorSubstitutionCountMap.put(reference, majorSubstitutionCounts);
                majorInsertionCountMap.put(reference, majorInsertionCounts);
                majorDeletionCountMap.put(reference, majorDeletionCounts);

                majorSubstitutionPvalueMap.put(reference, majorSubstitutionPvalues);
                majorInsertionPvalueMap.put(reference, majorInsertionPvalues);
                majorDeletionPvalueMap.put(reference, majorDeletionPvalues);
            } else
                skippedReferences.add(reference);
        }

        this.references.removeAll(skippedReferences);
    }

    @Deprecated
    public double computeMajorPvalue(int majorMigCount, int minorMigCount, int numberOfMigs) {
        if (filterSingleMigs && majorMigCount == 1)
            return 1.0;

        // probability that an error is generated on first cycle
        double errorProb = 1.0 - Math.pow(1.0 - minorMigCount / (double) numberOfMigs, 1.0 / (double) pcrCycles);

        // second template is lost
        errorProb *= (1.0 - pcrEfficiency);

        // todo:
        //double alpha = 1.0 + errorProb * numberOfMigs, beta = 1.0 + (1.0 - errorProb) * numberOfMigs;
        //

        BinomialDistribution binomialDistribution = new BinomialDistributionImpl(numberOfMigs,
                errorProb);

        double pValue = 1.0 + 0.5 * binomialDistribution.probability(majorMigCount);

        try {
            pValue -= binomialDistribution.cumulativeProbability(majorMigCount);
        } catch (Exception e) {
            System.out.println("ERROR cannot compute cumulative probability for Binomial distribution " +
                    "with p=" + errorProb + ", n=" + majorMigCount +
                    ", N=" + numberOfMigs);
            pValue = 0.0;
        }

        return pValue;
    }

    public boolean isMajor(int majorCount, int minorCount) {
        return majorCount / (double) minorCount >= majorPvalueThreshold;
    }

    public MutationFilter getMutationFilter(Reference reference) {
        return mutationFilterByReference.get(reference);
    }

    private Reference currentReference = null;
    private MutationsAndCoverage currentMutationsAndCoverage;

    private synchronized void setCurrentReference(Reference currentReference) {
        this.currentReference = currentReference;
        currentMutationsAndCoverage = alignerReferenceLibrary.getMutationsAndCoverage(currentReference);
    }

    public int getCorrespondingMajorCount(Reference reference, int pos, int nt) {
        if (!reference.equals(currentReference))
            setCurrentReference(reference);
        return currentMutationsAndCoverage.getMajorNucleotideMigCount(pos, nt);
    }

    public int getCorrespondingMinorCount(Reference reference, int pos, int nt) {
        if (!reference.equals(currentReference))
            setCurrentReference(reference);
        return currentMutationsAndCoverage.getMinorNucleotideMigCount(pos, nt);
    }

    public int getMajorCount(Reference reference, int pos, int nt) {
        return majorSubstitutionCountMap.get(reference)[pos][nt];
    }

    public int getMajorCount(Reference reference, int pos) {
        int count = 0;
        for (int i = 0; i < 4; i++)
            count += majorSubstitutionCountMap.get(reference)[pos][i];
        return count;
    }

    public double getPValue(Reference reference, int pos, int nt) {
        return majorSubstitutionPvalueMap.get(reference)[pos][nt];
    }

    public VariantLibrary getVariantLibrary() {
        return variantLibrary;
    }

    public int getMajorInsCount(Reference reference, int pos) {
        return majorInsertionCountMap.get(reference)[pos];
    }

    public int getMajorDelCount(Reference reference, int pos) {
        return majorDeletionCountMap.get(reference)[pos];
    }


    public double getInsPValue(Reference reference, int pos) {
        return majorInsertionPvalueMap.get(reference)[pos];
    }

    public double getDelPValue(Reference reference, int pos) {
        return majorDeletionPvalueMap.get(reference)[pos];
    }

    private static StringBuilder appendNucleotideArrayToStringBuilder(StringBuilder sb, String name,
                                                                      int[][] array, int n) {
        sb.append(name);
        for (int i = 0; i < n; i++)
            sb.append("\t").append(i + 1);

        for (int j = 0; j < 4; j++) {
            sb.append("\n").append(NucleotideAlphabet.INSTANCE.symbolFromCode((byte) j));
            for (int i = 0; i < n; i++)
                sb.append("\t").append(array[i][j]);
        }
        //sb.append("\n");
        return sb;
    }

    private static StringBuilder appendNucleotideArrayToStringBuilder(StringBuilder sb, String name,
                                                                      double[][] array, int n) {
        sb.append(name);
        for (int i = 0; i < n; i++)
            sb.append("\t").append(i + 1);

        for (int j = 0; j < 4; j++) {
            sb.append("\n").append(NucleotideAlphabet.INSTANCE.symbolFromCode((byte) j));
            for (int i = 0; i < n; i++)
                sb.append("\t").append(array[i][j]);
        }
        //sb.append("\n");
        return sb;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("#").append(super.toString()).append("\n");
        for (Reference reference : references) {
            MutationFilter mutationFilter = mutationFilterByReference.get(reference);

            if (mutationFilter.good()) {
                sb.append(reference.toString()).append("\n");

                int[][] majorSubstitutionCounts = majorSubstitutionCountMap.get(reference);
                int[] majorInsertionCounts = majorInsertionCountMap.get(reference),
                        majorDeletionCounts = majorDeletionCountMap.get(reference);

                int n = reference.getSequence().size();

                // Counts
                //
                sb.append("MajorCount\n");

                // Substitutions
                appendNucleotideArrayToStringBuilder(sb, "Substitutions", majorSubstitutionCounts, n).append("\n");

                // Indels
                sb.append("Insertions");
                for (int i = 0; i < n; i++)
                    sb.append("\t").append(majorInsertionCounts[i]);
                sb.append("\n");

                sb.append("Deletions");
                for (int i = 0; i < n; i++)
                    sb.append("\t").append(majorDeletionCounts[i]);
                sb.append("\n");

                // Pvalues
                //
                sb.append("MajorPvalue\n");

                double[][] majorSubstitutionPvalues = majorSubstitutionPvalueMap.get(reference);
                double[] majorInsertionPvalues = majorInsertionPvalueMap.get(reference),
                        majorDeletionPvalues = majorDeletionPvalueMap.get(reference);

                // Substitutions
                appendNucleotideArrayToStringBuilder(sb, "Substitutions", majorSubstitutionPvalues, n).append("\n");

                // Indels
                sb.append("Insertions");
                for (int i = 0; i < n; i++)
                    sb.append("\t").append(majorInsertionPvalues[i]);
                sb.append("\n");

                sb.append("Deletions");
                for (int i = 0; i < n; i++)
                    sb.append("\t").append(majorDeletionPvalues[i]);
                sb.append("\n\n");

                // Coverage
                //
                sb.append("Coverage");
                for (int i = 0; i < n; i++)
                    sb.append("\t").append(i + 1);
                sb.append("\n");

                // Coverage
                sb.append("HasReference");
                for (int i = 0; i < n; i++)
                    sb.append("\t").append(mutationFilter.hasReference(i) ? 1 : 0);
                sb.append("\n");

                sb.append("CoverageFilter");
                for (int i = 0; i < n; i++)
                    sb.append("\t").append(mutationFilter.passedFilter(i) ? 1 : 0);
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
