package com.sstewartgallus.interpreter;

import java.util.function.Function;

// fixme.. make a monad..
public interface Effect<A> {
    static <A> Effect<A> pure(A value) {
        return new Pure<>(value);
    }

    <B> Interpreter<?, B> step(Interpreter<A, B> interpreter);

    default <B> Effect<B> bind(Function<A, Effect<B>> f) {
        return new Bind<>(this, f);
    }

    record Pure<A>(A value) implements Effect<A> {
        @Override
        public <B> Interpreter<?, B> step(Interpreter<A, B> interpreter) {
            return interpreter.returnWith(value);
        }
    }

    record Bind<A, B>(Effect<A>x, Function<A, Effect<B>>f) implements Effect<B> {
        @Override
        public <C> Interpreter<?, C> step(Interpreter<B, C> interpreter) {
            return interpreter.bind(x, f);
        }
    }
}
