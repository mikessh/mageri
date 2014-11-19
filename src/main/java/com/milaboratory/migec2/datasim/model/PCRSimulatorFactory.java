package com.milaboratory.migec2.datasim.model;

import java.util.concurrent.Callable;

public interface PCRSimulatorFactory {
    public Callable<PCRResult> create();
}
