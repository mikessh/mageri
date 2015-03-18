package com.milaboratory.oncomigec.core.mutations;

import java.io.Serializable;

public interface MutationsCollection extends Serializable {
    public int[] getMutationCodes();

    public int size();

    public int substitutionCount();
}
