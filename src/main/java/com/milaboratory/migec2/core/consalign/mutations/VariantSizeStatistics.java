package com.milaboratory.migec2.core.consalign.mutations;

import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.migec2.util.ProbabilityHistogram;
import com.milaboratory.migec2.util.Util;

import java.util.Collection;
import java.util.Map;

public class VariantSizeStatistics {
    private boolean updated = false;

    private final ProbabilityHistogram overallHistogram;

    public VariantSizeStatistics() {
        this.overallHistogram = new ProbabilityHistogram("MajorRatio-MIGs", "MinorRatio-MIGs",
                "MajorRatio-Reads", "MinorRatio-Reads");
    }

    public void updateMajor(int majorCode, byte majorCqsScore, int migSize) {
        double percentage = Util.cqsToPercentage(majorCqsScore);
        overallHistogram.append(0, percentage);
        overallHistogram.append(2, percentage, (int) (migSize * percentage));

        // todo: nt-specific
    }

    public void updateMinor(int minorCode, int minorCount, int migSize) {
        double percentage = minorCount / (double) migSize;
        overallHistogram.append(1, percentage);
        overallHistogram.append(3, percentage, minorCount);

        // todo: nt-specific
    }

    public void update(Collection<MutationQualityPair> majorData,
                       Map<Integer, Integer> minorData, int migSize) {
        updated = true;

        for (MutationQualityPair mutationQualityPair : majorData)
            // Filter from indels
            if (Mutations.isSubstitution(mutationQualityPair.getMutationCode()))
                updateMajor(mutationQualityPair.getMutationCode(), mutationQualityPair.getQuality(), migSize);

        for (Map.Entry<Integer, Integer> entry : minorData.entrySet()) {
            int code = entry.getKey();
            // Filter from indels
            if (Mutations.isSubstitution(code))
                updateMinor(code, entry.getValue(), migSize);
        }
    }

    public boolean updated() {
        return updated;
    }

    @Override
    public String toString() {
        return overallHistogram.toString();
    }
}
