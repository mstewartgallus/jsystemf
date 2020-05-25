package com.sstewartgallus.interpreter;


import java.util.function.Function;

public interface Code<A> {
    <X> Interpreter<?, X> execute(Interpreter<A, X> interpreter);

    default <X> Code<Function<X, A>> pointFree(Id<X> v) {
        throw new UnsupportedOperationException(getClass().toString());
    }
}
