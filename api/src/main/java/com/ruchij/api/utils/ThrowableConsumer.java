package com.ruchij.api.utils;

@FunctionalInterface
public interface ThrowableConsumer<T, E extends Throwable> {
    void accept(T t) throws E;
}
