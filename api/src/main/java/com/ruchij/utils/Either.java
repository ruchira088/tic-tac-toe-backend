package com.ruchij.utils;

import java.util.Optional;
import java.util.function.Function;

public interface Either<L, R> {
    Optional<L> left();

    Optional<R> right();

    <R0> Either<L, R0> map(Function<R, R0> mapper);

    <A> A fold(Function<L, A> leftMapper, Function<R, A> rightMapper);

    static <L, R> Either<L, R> left(L value) {
        return new Left<>(value);
    }

    static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
    }

    record Right<L, R>(R rightValue) implements Either<L, R> {

        @Override
        public Optional<L> left() {
            return Optional.empty();
        }

        @Override
        public Optional<R> right() {
            return Optional.of(rightValue);
        }

        @Override
        public <R0> Either<L, R0> map(Function<R, R0> mapper) {
            return new Right<>(mapper.apply(rightValue));
        }

        @Override
        public <A> A fold(Function<L, A> leftMapper, Function<R, A> rightMapper) {
            return rightMapper.apply(rightValue);
        }
    }

    record Left<L, R>(L leftValue) implements Either<L, R> {
        @Override
        public Optional<L> left() {
            return Optional.of(leftValue);
        }

        @Override
        public Optional<R> right() {
            return Optional.empty();
        }

        @Override
        public <R0> Either<L, R0> map(Function<R, R0> mapper) {
            return new Left<>(leftValue);
        }

        @Override
        public <A> A fold(Function<L, A> leftMapper, Function<R, A> rightMapper) {
            return leftMapper.apply(leftValue);
        }
    }
}
