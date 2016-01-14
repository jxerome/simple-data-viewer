package com.mainaud.function;

/**
 * Get a result or an error.
 *
 * @param <T> Result type.
 * @param <E> Error type.
 */
public abstract class Result<T, E> {
    private Result() {
    }

    public static <T, E> Result<T, E> success(T value) {
        return (Result<T, E>) new Success<T>(value);
    }

    public static <T, E> Result<T, E> success() {
        return success(null);
    }

    public static <T, E> Result<T, E> failure(E errror) {
        return (Result<T, E>) new Failure<E>(errror);
    }

    public abstract boolean isSuccess();

    public abstract boolean isFailure();

    public abstract T get();

    public abstract E getError();

    private static final class Success<T> extends Result<T, Void> {
        private T value;

        private Success(T value) {
            this.value = value;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public boolean isFailure() {
            return false;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public Void getError() {
            throw new IllegalStateException("Result in success");
        }
    }

    private static final class Failure<E> extends Result<Void, E> {
        private E error;

        private Failure(E error) {
            this.error = error;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public boolean isFailure() {
            return true;
        }

        @Override
        public Void get() {
            throw new IllegalStateException("Result in error");
        }

        @Override
        public E getError() {
            return error;
        }
    }
}
