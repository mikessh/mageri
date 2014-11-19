package com.milaboratory.migec2.datasim.model;

import com.milaboratory.migec2.util.ProbabilityHistogram;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PCRSimulatorStatistics {
    private final int nThreads;
    private final ProbabilityHistogram histogram;
    private final PCRSimulatorFactory PCRSimulatorFactory;

    public PCRSimulatorStatistics(PCRSimulatorFactory PCRSimulatorFactory) {
        this(Runtime.getRuntime().availableProcessors(), PCRSimulatorFactory);
    }

    public PCRSimulatorStatistics(int nThreads, PCRSimulatorFactory PCRSimulatorFactory) {
        this.histogram = new ProbabilityHistogram("ErrorRate");
        this.nThreads = nThreads;
        this.PCRSimulatorFactory = PCRSimulatorFactory;
    }

    public void run(int repetitions) throws ExecutionException, InterruptedException {
        final ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        final List<Future> futures = new LinkedList<>();

        System.out.println("[" + new Date().toString() + "] Started simulation..");
        for (int i = 0; i < repetitions; i++) {
            Future future = executor.submit(PCRSimulatorFactory.create());
            futures.add(future);
        }
        System.out.println("[" + new Date().toString() + "] Finished submitting tasks");

        int chunk_sz = Math.max(repetitions / 100, 100000);
        int i = 0;
        for (Future future : futures) {
            PCRResult pcrResult = (PCRResult) future.get();
            if (!pcrResult.failed())
                histogram.append(0, pcrResult.errorRate());

            if (++i % chunk_sz == 0) {
                System.out.println("[" + new Date().toString() + "] Processed " + i +
                        " out of " + repetitions + " simulations");
            }
        }

        executor.shutdown();
    }

    @Override
    public String toString() {
        return histogram.toString();
    }
}