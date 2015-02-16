package com.milaboratory.oncomigec.core.io.misc;

import com.milaboratory.oncomigec.util.Util;

public class MigReaderParameters {
    private final byte umiQualThreshold;
    private final long limit;
    private final int threads;
    private final boolean verbose;
    private final boolean trimAdapters;

    public static MigReaderParameters DEFAULT = new MigReaderParameters(Util.PH33_LOW_QUAL,
            -1, Runtime.getRuntime().availableProcessors(), true, true);

    public static MigReaderParameters TEST(long limit) {
        return new MigReaderParameters(Util.PH33_LOW_QUAL,
                limit, Runtime.getRuntime().availableProcessors(), true, true);
    }

    public static MigReaderParameters WITH_QUAL(byte umiQualThreshold) {
        return new MigReaderParameters(umiQualThreshold,
                -1, Runtime.getRuntime().availableProcessors(), true, true);
    }

    public static MigReaderParameters IGNORE_QUAL = new MigReaderParameters((byte) 0,
            -1, Runtime.getRuntime().availableProcessors(), true, true);

    public MigReaderParameters(byte umiQualThreshold, long limit, int threads,
                               boolean verbose, boolean trimAdapters) {
        this.umiQualThreshold = umiQualThreshold;
        this.limit = limit;
        this.threads = threads;
        this.verbose = verbose;
        this.trimAdapters = trimAdapters;
    }

    public byte getUmiQualThreshold() {
        return umiQualThreshold;
    }

    public long getLimit() {
        return limit;
    }

    public int getThreads() {
        return threads;
    }

    public boolean verbose() {
        return verbose;
    }

    public boolean trimAdapters() {
        return trimAdapters;
    }
}
