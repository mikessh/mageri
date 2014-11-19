package com.milaboratory.migec2.datasim.model;

import java.util.concurrent.Callable;

public class UniformPCRSimulatorFactory implements PCRSimulatorFactory {
    private final int nCycles;
    private final double mu, lambda;
    private final int startingMolecules;

    public UniformPCRSimulatorFactory(int nCycles, double mu, int startingMolecules, double lambda) {
        this.nCycles = nCycles;
        this.mu = mu;
        this.startingMolecules = startingMolecules;
        this.lambda = lambda;
    }

    @Override
    public Callable<PCRResult> create() {
        return new UniformPCRSimulator(mu, lambda, startingMolecules, nCycles);
    }
}
