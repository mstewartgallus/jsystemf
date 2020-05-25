package com.sstewartgallus.interpreter;

import java.util.function.Function;

public record ConstantCode<A, B>() implements Code<Function<A, Function<B, A>>> {
    @Override
    public String toString() {
        return "K";
    }

    @Override
    public <X> Interpreter<?, X> execute(Interpreter<Function<A, Function<B, A>>, X> interpreter) {
        return interpreter.pure(x -> y -> x);
    }

    @Override
    public <X> Code<Function<X, Function<A, Function<B, A>>>> pointFree(Id<X> v) {
        return new ApplyCode<>(new ConstantCode<>(), this);
    }
}
