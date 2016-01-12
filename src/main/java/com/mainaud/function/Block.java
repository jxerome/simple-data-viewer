package com.mainaud.function;

/**
 * Interface of a bloc with exception.
 */
@FunctionalInterface
public interface Block {
    void run() throws Throwable;

}
