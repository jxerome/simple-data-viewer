package com.mainaud.function;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;

public interface TryTo {

    static void run(Block block) {
        try {
            block.run();
        } catch (Throwable t) {
            throw new CompletionException(t);
        }
    }

    static <V> V call(Callable<V> block) {
        try {
            return block.call();
        } catch (Exception t) {
            throw new CompletionException(t);
        }
    }

}
