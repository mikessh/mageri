package com.antigenomics.mageri.core.variant.model;


import com.antigenomics.mageri.core.mapping.MutationsTable;
import com.antigenomics.mageri.core.mutations.Mutation;
import com.antigenomics.mageri.core.mutations.Substitution;
import com.antigenomics.mageri.core.variant.VariantCallerParameters;
import com.antigenomics.mageri.misc.AuxiliaryStats;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;

public class PresetErrorModel implements ErrorModel {
    private final double[][] alpha = new double[4][4], beta = new double[4][4];
    private final double propagateProb;
    private final MutationsTable mutationsTable;

    public final static String DEFAULT_VALUES =
            "A_C,T_G:0.9278882:26823.24;" +
                    "A_G,T_C:1.0295689:16394.12;" +
                    "A_T,T_A:1.0838020:38974.43;" +
                    "C_A,G_T:0.9957058:21862.23;" +
                    "C_G,G_C:1.0584582:48029.97;" +
                    "C_T,G_A:2.1314829:24344.82";

    public PresetErrorModel(VariantCallerParameters variantCallerParameters, MutationsTable mutationsTable) {
        String[] lines = variantCallerParameters.getModelPresetString().split(";");

        for (String line : lines) {
            if (line.length() > 0 && !line.startsWith("#")) {
                String[] tokens = line.split(":");
                double alpha = Double.parseDouble(tokens[1]),
                        theta = Double.parseDouble(tokens[2]);

                for (String substitution : tokens[0].split(",")) {
                    String[] fromTo = substitution.split("_");

                    int from = NucleotideAlphabet.INSTANCE.codeFromSymbol(fromTo[0].charAt(0)),
                            to = NucleotideAlphabet.INSTANCE.codeFromSymbol(fromTo[1].charAt(0));

                    this.alpha[from][to] = alpha;
                    this.beta[from][to] = theta;
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
        double a = alpha[from][to],
                b = beta[from][to] / propagateProb;

        return new ErrorRateEstimate(a / (a + b), a, b);
    }

    @Override
    public VariantQuality computeQuality(int majorCount, int coverage, Mutation mutation) {
        ErrorRateEstimate errorRateEstimate = computeErrorRate(mutation);
        double score = computeNegBinomScore(majorCount, coverage,
                errorRateEstimate.getStatistics()[0],
                errorRateEstimate.getStatistics()[1]);

        return new VariantQuality(errorRateEstimate, score);
    }

    @Override
    public VariantQuality computeQuality(int majorCount, int coverage, int pos, int from, int to) {
        ErrorRateEstimate errorRateEstimate = computeErrorRate(pos, from, to);
        double score = computeNegBinomScore(majorCount, coverage,
                errorRateEstimate.getStatistics()[0],
                errorRateEstimate.getStatistics()[1]);

        return new VariantQuality(errorRateEstimate, score);
    }

    private static double computeNegBinomScore(int majorCount, int coverage, double a, double b) {
        if (majorCount == 0) {
            return 0;
        }

        return -10 * Math.log10(AuxiliaryStats.betaBinomialPvalueFast(majorCount, coverage, a, b));
    }
}
