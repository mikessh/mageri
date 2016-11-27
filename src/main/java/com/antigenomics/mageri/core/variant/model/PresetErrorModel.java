package com.antigenomics.mageri.core.variant.model;


import com.antigenomics.mageri.core.mapping.MutationsTable;
import com.antigenomics.mageri.core.mutations.Mutation;
import com.antigenomics.mageri.core.mutations.Substitution;
import com.antigenomics.mageri.core.variant.VariantCallerParameters;
import com.antigenomics.mageri.misc.AuxiliaryStats;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;

public class PresetErrorModel implements ErrorModel {
    private final double[][] alpha = new double[4][4], theta = new double[4][4];
    private final double propagateProb;
    private final MutationsTable mutationsTable;

    public final static String DEFAULT_VALUES =
            "A>C,T>G:0.9279161:3.727861e-05;" +
                    "A>G,T>C:1.0296172:6.099085e-05;" +
                    "A>T,T>A:1.0838314:2.565636e-05;" +
                    "C>A,G>T:0.9957468:4.573685e-05;" +
                    "C>G,G>C:1.0584827:2.081932e-05;" +
                    "C>T,G>A:2.1316472:4.106974e-05";

    public PresetErrorModel(VariantCallerParameters variantCallerParameters, MutationsTable mutationsTable) {
        String[] lines = variantCallerParameters.getModelPresetString().split(";");

        for (String line : lines) {
            if (line.length() > 0 && !line.startsWith("#")) {
                String[] tokens = line.split(":");
                double alpha = Double.parseDouble(tokens[1]),
                        theta = Double.parseDouble(tokens[2]);

                for (String substitution : tokens[0].split(",")) {
                    String[] fromTo = substitution.split(">");

                    int from = NucleotideAlphabet.INSTANCE.codeFromSymbol(fromTo[0].charAt(0)),
                            to = NucleotideAlphabet.INSTANCE.codeFromSymbol(fromTo[1].charAt(0));

                    this.alpha[from][to] = alpha;
                    this.theta[from][to] = theta;
                }
            }
        }

        this.mutationsTable = mutationsTable;


        double lambda = variantCallerParameters.getModelEfficiency() - 1;
        this.propagateProb = variantCallerParameters.shouldPropagate() ? (1.0 - lambda) * lambda * lambda : 1.0;
    }

    @Override
    public ErrorRateEstimate computeErrorRate(Mutation mutation) {
        int code = ((Substitution) mutation).getCode();

        return computeErrorRate(Mutations.getPosition(code), Mutations.getFrom(code), Mutations.getTo(code));
    }

    @Override
    public ErrorRateEstimate computeErrorRate(int pos, int from, int to) {
        int coverage = mutationsTable.getMigCoverage(pos);
        double r = alpha[from][to],
                p = theta[from][to] * coverage / (1.0 + theta[from][to] * coverage) * propagateProb;

        return new ErrorRateEstimate(p * r / (1 - p), r, p);
    }

    @Override
    public VariantQuality computeQuality(int majorCount, int coverage, Mutation mutation) {
        ErrorRateEstimate errorRateEstimate = computeErrorRate(mutation);
        double score = computeNegBinomScore(majorCount, coverage, errorRateEstimate.getStatistics()[0],
                errorRateEstimate.getStatistics()[1]);

        return new VariantQuality(errorRateEstimate, score);
    }

    @Override
    public VariantQuality computeQuality(int majorCount, int coverage, int pos, int from, int to) {
        ErrorRateEstimate errorRateEstimate = computeErrorRate(pos, from, to);
        double score = computeNegBinomScore(majorCount, coverage, errorRateEstimate.getStatistics()[0],
                errorRateEstimate.getStatistics()[1]);

        return new VariantQuality(errorRateEstimate, score);
    }

    private static double computeNegBinomScore(int majorCount, int coverage, double alpha, double theta) {
        if (majorCount == 0) {
            return 0;
        }

        double p = theta * coverage / (1.0 + theta * coverage);

        if (alpha <= 0 || p >= 1 || p <= 0) {
            return -1.0;
        }

        return -10 * Math.log10(1.0 - AuxiliaryStats.negativeBinomialCdf(majorCount, alpha, p));
    }
}
