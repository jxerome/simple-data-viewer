package com.mainaud.function;

/**
 * A function which can send exceptions.
 *
 * @param <T> Object type.
 * @param <R> Return type.
 * @param <E> Exception type.
 */
@FunctionalInterface
public interface FunctionWithException<T, R, E extends Exception> {
    R apply(T t) throws E;
}
