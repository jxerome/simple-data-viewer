package com.mainaud.function;

import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Function;

public interface TryTo {

    static <T, E extends Exception> Consumer<T> accept(ConsumerWithException<T, E> block) {
        return a -> {
            try {
                block.accept(a);
            } catch (Exception t) {
                throw new CompletionException(t);
            }
        };
    }

    static <T, R, E extends Exception> Function<T, R> apply(FunctionWithException<T, R, E> block) {
        return a -> {
            try {
                return block.apply(a);
            } catch (Exception t) {
                throw new CompletionException(t);
            }
        };
    }

}
