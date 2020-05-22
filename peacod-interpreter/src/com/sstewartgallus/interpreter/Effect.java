package com.sstewartgallus.interpreter;

import java.util.function.Function;

public interface Effect<A> {
    static <A> Effect<Effect<A>> thunk(Effect<A> value) {
        return new ThunkEffect<>(value);
    }

    static <A> Effect<A> pure(A value) {
        return new PureEffect<>(value);
    }

    static <C, X> X load(Interpreter.Equal<X, Effect<C>> witness, Id<X> effectId) {
        return witness.right().to(new LoadEffect<>(witness, effectId));
    }

    <B> Interpreter<?, B> execute(Interpreter<A, B> interpreter);

    default <B> Effect<B> bind(Function<A, Effect<B>> f) {
        return new BindEffect<>(this, f);
    }
}
