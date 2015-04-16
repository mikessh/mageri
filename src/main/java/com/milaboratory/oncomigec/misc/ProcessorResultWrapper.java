package com.milaboratory.oncomigec.misc;

public class ProcessorResultWrapper<T> {
    private final T result;
    private final boolean hasResult;

    private ProcessorResultWrapper() {
        this.result = null;
        this.hasResult = false;
    }

    public ProcessorResultWrapper(T result) {
        this.result = result;
        this.hasResult = true;
    }

    public static ProcessorResultWrapper BLANK = new ProcessorResultWrapper();

    public T getResult() {
        return result;
    }

    public boolean hasResult() {
        return hasResult;
    }
}
