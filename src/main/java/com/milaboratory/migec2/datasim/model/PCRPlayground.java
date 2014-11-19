package com.milaboratory.migec2.datasim.model;

import java.util.concurrent.ExecutionException;

public class PCRPlayground {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        PCRSimulatorFactory PCRSimulatorFactory = new UniformPCRSimulatorFactory(20, 1e-4, 1, 0.85);
        PCRSimulatorStatistics statistics = new PCRSimulatorStatistics(PCRSimulatorFactory);
        statistics.run(1_000_000);
        System.out.println(statistics);
    }
}
