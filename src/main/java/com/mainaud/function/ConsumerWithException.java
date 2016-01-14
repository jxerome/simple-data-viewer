package com.mainaud.function;

/**
 * A consummer which can send exceptions.
 *
 * @param <T> Object type.
 * @param <E> Exception type.
 */
@FunctionalInterface
public interface ConsumerWithException<T, E extends Exception> {
    void accept(T t) throws E;
}
