package com.sstewartgallus.interpreter;

import java.util.function.Function;

public interface Effect<A> {
    <B> Interpreter<?, B> execute(Interpreter<A, B> interpreter);

    static <A> Effect<A> pure(A value) {
        return new PureEffect<>(value);
    }
    default <B> Effect<B> bind(Function<A, Effect<B>> f) {
        return new BindEffect<>(this, f);
    }
}
