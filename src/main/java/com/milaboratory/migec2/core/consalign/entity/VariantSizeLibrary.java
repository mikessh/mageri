package com.milaboratory.migec2.core.consalign.entity;

import com.milaboratory.migec2.core.align.reference.Reference;
import com.milaboratory.migec2.core.align.reference.ReferenceLibrary;
import com.milaboratory.migec2.core.consalign.mutations.MutationQualityPair;
import com.milaboratory.migec2.core.consalign.mutations.VariantSizeStatistics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class VariantSizeLibrary {
    private final Map<Reference, VariantSizeStatistics> variantSizeStatisticsByReference;

    public VariantSizeLibrary(ReferenceLibrary referenceLibrary) {
        variantSizeStatisticsByReference = new HashMap<>();
        for (Reference reference : referenceLibrary.getReferences())
            variantSizeStatisticsByReference.put(reference, new VariantSizeStatistics());
    }

    public void update(Reference reference, Collection<MutationQualityPair> majorData,
                       Map<Integer, Integer> minorData, int migSize) {
        variantSizeStatisticsByReference.get(reference).update(majorData,
                minorData, migSize);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("#" + super.toString());
        for (Map.Entry<Reference, VariantSizeStatistics> variantSizeStatisticsEntry :
                variantSizeStatisticsByReference.entrySet())
            if (variantSizeStatisticsEntry.getValue().updated()) {
                stringBuilder.append('\n').
                        append(variantSizeStatisticsEntry.getKey().toString()).
                        append('\n').
                        append(variantSizeStatisticsEntry.getValue().toString());
            }
        return stringBuilder.toString();
    }
}
